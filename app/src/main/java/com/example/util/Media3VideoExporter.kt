package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.media3.common.C
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.audio.SonicAudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.Contrast
import androidx.media3.effect.Crop
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.example.data.model.AspectRatio
import com.example.data.model.AudioCategory
import com.example.data.model.AudioTrackItem
import com.example.data.model.ExportResolution
import com.example.data.model.ExportVideoCodec
import com.example.data.model.ImageOverlay
import com.example.data.model.TextOverlay as AppTextOverlay
import com.example.data.model.VideoClip
import com.example.data.model.VideoFilter
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.resume
import kotlin.math.min

/**
 * Pure PCM 16-bit AudioProcessor applying track/clip volume, fade-in, fade-out,
 * and automatic background ducking attenuation during speech/voiceover windows.
 */
@OptIn(UnstableApi::class)
class DuckingAndEnvelopeAudioProcessor(
    private val baseVolume: Float,
    private val isMuted: Boolean,
    private val trackStartTimelineMs: Long,
    private val trackDurationMs: Long,
    private val fadeInMs: Long,
    private val fadeOutMs: Long,
    private val applyDucking: Boolean,
    private val duckLevel: Float,
    private val speechWindowsMs: List<Pair<Long, Long>>
) : BaseAudioProcessor() {

    private var processedFrames: Long = 0L

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT &&
            inputAudioFormat.encoding != C.ENCODING_INVALID
        ) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val output = replaceOutputBuffer(remaining)
        val channels = inputAudioFormat.channelCount.coerceAtLeast(1)
        val sampleRate = inputAudioFormat.sampleRate.coerceAtLeast(1)
        val bytesPerFrame = channels * 2

        val readBuffer = inputBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        output.order(ByteOrder.LITTLE_ENDIAN)

        val frameCount = remaining / bytesPerFrame
        for (frame in 0 until frameCount) {
            val localTimeMs = (processedFrames * 1000L) / sampleRate
            val timelineMs = trackStartTimelineMs + localTimeMs
            val gain = computeEffectiveGainAtTime(
                baseVolume = baseVolume,
                isMuted = isMuted,
                localTimeMs = localTimeMs,
                trackDurationMs = trackDurationMs,
                fadeInMs = fadeInMs,
                fadeOutMs = fadeOutMs,
                applyDucking = applyDucking,
                duckLevel = duckLevel,
                timelineTimeMs = timelineMs,
                speechWindowsMs = speechWindowsMs
            )
            for (ch in 0 until channels) {
                val sample = readBuffer.short
                val scaled = (sample * gain).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                output.putShort(scaled.toShort())
            }
            processedFrames++
        }

        // Copy any trailing odd bytes unchanged if present
        val leftover = remaining - (frameCount * bytesPerFrame)
        if (leftover > 0) {
            readBuffer.position(inputBuffer.position() + frameCount * bytesPerFrame)
            for (i in 0 until leftover) {
                output.put(readBuffer.get())
            }
        }

        inputBuffer.position(inputBuffer.limit())
        output.flip()
    }

    override fun onFlush() {
        processedFrames = 0L
    }

    override fun onReset() {
        processedFrames = 0L
    }

    companion object {
        fun computeEffectiveGainAtTime(
            baseVolume: Float,
            isMuted: Boolean,
            localTimeMs: Long,
            trackDurationMs: Long,
            fadeInMs: Long,
            fadeOutMs: Long,
            applyDucking: Boolean,
            duckLevel: Float,
            timelineTimeMs: Long,
            speechWindowsMs: List<Pair<Long, Long>>
        ): Float {
            if (isMuted || baseVolume <= 0.001f) return 0f
            var gain = baseVolume.coerceIn(0f, 2f)

            if (fadeInMs > 0L && localTimeMs < fadeInMs) {
                gain *= (localTimeMs.toFloat() / fadeInMs.toFloat()).coerceIn(0f, 1f)
            }
            if (fadeOutMs > 0L && trackDurationMs > fadeOutMs) {
                val remainingMs = (trackDurationMs - localTimeMs).coerceAtLeast(0L)
                if (remainingMs < fadeOutMs) {
                    gain *= (remainingMs.toFloat() / fadeOutMs.toFloat()).coerceIn(0f, 1f)
                }
            }
            if (applyDucking && speechWindowsMs.any { (start, end) -> timelineTimeMs in start..end }) {
                gain *= duckLevel.coerceIn(0.05f, 1f)
            }
            return gain.coerceIn(0f, 2f)
        }
    }
}

data class Media3ExportOutcome(
    val outputFile: File,
    val mediaStoreUri: String?,
    val durationMs: Long,
    val fileSizeBytes: Long
)

@OptIn(UnstableApi::class)
object Media3VideoExporter {
    const val EXPORT_NOTIFICATION_CHANNEL_ID = "opencut_exports"
    private const val EXPORT_NOTIFICATION_ID = 4102

    /**
     * Checks device hardware/encoder capability for 4K UHD (3840x2160) encoding.
     */
    fun is4kHardwareEncodingSupported(): Boolean {
        return try {
            val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            list.codecInfos.any { info ->
                if (!info.isEncoder) return@any false
                val avcOrHevc = info.supportedTypes.any {
                    it.equals(MediaFormat.MIMETYPE_VIDEO_AVC, ignoreCase = true) ||
                        it.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true)
                }
                if (!avcOrHevc) return@any false
                val mime = if (info.supportedTypes.any { it.equals(MediaFormat.MIMETYPE_VIDEO_AVC, true) }) {
                    MediaFormat.MIMETYPE_VIDEO_AVC
                } else MediaFormat.MIMETYPE_VIDEO_HEVC
                val caps = info.getCapabilitiesForType(mime).videoCapabilities
                caps?.isSizeSupported(3840, 2160) == true || caps?.isSizeSupported(2160, 3840) == true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks device hardware/encoder capability for HEVC (H.265) encoding.
     */
    fun isHevcHardwareEncodingSupported(): Boolean {
        return try {
            val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            list.codecInfos.any { info ->
                info.isEncoder && info.supportedTypes.any {
                    it.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Resolves target export pixel dimensions preserving project aspect ratio.
     */
    fun resolveTargetDimensions(resolution: ExportResolution, aspectRatio: AspectRatio): Pair<Int, Int> {
        val shortEdge = resolution.height
        val ratio = aspectRatio.aspectRatio
        val rawW: Int
        val rawH: Int
        if (ratio >= 1f) {
            rawH = shortEdge
            rawW = (shortEdge * ratio).toInt()
        } else {
            rawW = shortEdge
            rawH = (shortEdge / ratio).toInt()
        }
        // Align to multiples of 16 for strict hardware encoder compatibility
        val alignedW = ((rawW + 8) / 16) * 16
        val alignedH = ((rawH + 8) / 16) * 16
        return alignedW.coerceAtLeast(160) to alignedH.coerceAtLeast(160)
    }

    fun collectSpeechWindows(
        clips: List<VideoClip>,
        audioClips: List<AudioTrackItem>,
        isVideoMuted: Boolean,
        isAudioMuted: Boolean
    ): List<Pair<Long, Long>> {
        val windows = mutableListOf<Pair<Long, Long>>()
        if (!isVideoMuted) {
            clips.filter {
                !it.isMuted && it.volume > 0.05f && (
                    it.hasSpeech ||
                        it.name.contains("Speech", ignoreCase = true) ||
                        it.name.contains("Voice", ignoreCase = true) ||
                        it.name.contains("Dialogue", ignoreCase = true) ||
                        it.name.contains("Interview", ignoreCase = true) ||
                        it.name.contains("Host", ignoreCase = true) ||
                        it.name.contains("Podcast", ignoreCase = true)
                    )
            }.forEach { windows.add(it.timelineStartMs to it.timelineEndMs) }
        }
        if (!isAudioMuted) {
            audioClips.filter {
                !it.isMuted && it.volume > 0.05f &&
                    (it.category == AudioCategory.VOICEOVER || it.category == AudioCategory.DIALOGUE)
            }.forEach { windows.add(it.timelineStartMs to it.timelineEndMs) }
        }
        return windows
    }

    @Volatile
    private var activeTransformer: Transformer? = null

    fun cancelActiveExport() {
        val transformer = activeTransformer
        if (transformer != null) {
            Handler(Looper.getMainLooper()).post {
                try {
                    transformer.cancel()
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun exportTimelineToMp4(
        context: Context,
        clips: List<VideoClip>,
        audioClips: List<AudioTrackItem>,
        textOverlays: List<AppTextOverlay>,
        imageOverlays: List<ImageOverlay>,
        aspectRatio: AspectRatio,
        resolution: ExportResolution,
        fps: Int,
        bitrateMbps: Int,
        outputFileName: String,
        isVideoTrackMuted: Boolean,
        isAudioTrackMuted: Boolean,
        isAutoDuckingMasterEnabled: Boolean,
        autoDuckingLevel: Float,
        videoCodec: ExportVideoCodec = ExportVideoCodec.H264,
        onProgress: (Float) -> Unit
    ): Result<Media3ExportOutcome> {
        if (clips.isEmpty()) {
            return Result.failure(IllegalStateException("Timeline has no video clips to export."))
        }

        if (resolution == ExportResolution.RES_4K && !is4kHardwareEncodingSupported()) {
            MediaDiagnostics.warn("EXPORT", "4K requested on device without 4K hardware encoder profile; attempting fallback check")
        }

        val exportDir = File(context.filesDir, "exported_videos").apply { mkdirs() }
        val outputFile = File(exportDir, outputFileName)
        if (outputFile.exists()) outputFile.delete()

        val (targetW, targetH) = resolveTargetDimensions(resolution, aspectRatio)
        val speechWindows = collectSpeechWindows(clips, audioClips, isVideoTrackMuted, isAudioTrackMuted)

        return try {
            // Prepare edited media items on IO thread (load any bitmap overlays cleanly)
            val composition = withContext(Dispatchers.IO) {
                buildComposition(
                    context = context,
                    clips = clips,
                    audioClips = audioClips,
                    textOverlays = textOverlays,
                    imageOverlays = imageOverlays,
                    targetWidth = targetW,
                    targetHeight = targetH,
                    fps = fps,
                    isVideoTrackMuted = isVideoTrackMuted,
                    isAudioTrackMuted = isAudioTrackMuted,
                    isAutoDuckingMasterEnabled = isAutoDuckingMasterEnabled,
                    autoDuckingLevel = autoDuckingLevel,
                    speechWindows = speechWindows
                )
            }

            val resolvedVideoMime = if (videoCodec == ExportVideoCodec.HEVC && isHevcHardwareEncodingSupported()) {
                MimeTypes.VIDEO_H265
            } else {
                if (videoCodec == ExportVideoCodec.HEVC) {
                    MediaDiagnostics.warn("EXPORT", "HEVC encoder not available on device; falling back to H.264")
                }
                MimeTypes.VIDEO_H264
            }

            // Transformer must run on Main Looper
            val exportResult = runTransformerOnMainThread(
                context = context,
                composition = composition,
                outputFile = outputFile,
                bitrateMbps = bitrateMbps,
                videoMimeType = resolvedVideoMime,
                onProgress = onProgress
            )

            if (exportResult.isFailure) {
                outputFile.delete()
                return Result.failure(exportResult.exceptionOrNull() ?: IllegalStateException("Export failed"))
            }

            if (!outputFile.exists() || outputFile.length() <= 0L) {
                return Result.failure(IllegalStateException("Export completed without producing an MP4 file."))
            }

            // Publish copy to Android Movies/OpenCut via Scoped Storage MediaStore
            val mediaStoreUri = withContext(Dispatchers.IO) {
                saveMp4ToMediaStore(context, outputFile, outputFileName)
            }

            val totalDurationMs = clips.sumOf { it.trimmedDurationMs }
            postExportCompletionNotification(context, outputFileName, totalDurationMs)

            MediaDiagnostics.info(
                "EXPORT",
                "Exported MP4 successfully",
                "${outputFile.name} (${outputFile.length()} bytes) ${targetW}x${targetH}@${fps}fps"
            )

            Result.success(
                Media3ExportOutcome(
                    outputFile = outputFile,
                    mediaStoreUri = mediaStoreUri,
                    durationMs = totalDurationMs,
                    fileSizeBytes = outputFile.length()
                )
            )
        } catch (ce: CancellationException) {
            cancelActiveExport()
            outputFile.delete()
            MediaDiagnostics.warn("EXPORT", "Export cancelled by user")
            throw ce
        } catch (e: Exception) {
            outputFile.delete()
            MediaDiagnostics.error("EXPORT", "MP4 Export failed", e.message ?: "", e)
            Result.failure(e)
        }
    }

    /**
     * Extracts audio track(s) from the active video clips into a standalone AAC (.m4a) file
     * in app-private studio_exports/ storage using Media3 Transformer.
     */
    suspend fun extractAudioToM4a(
        context: Context,
        clips: List<VideoClip>,
        outputFileName: String
    ): Result<File> {
        if (clips.isEmpty()) {
            return Result.failure(IllegalStateException("Add a video clip before extracting audio."))
        }
        val outDir = File(context.filesDir, "studio_exports").apply { mkdirs() }
        val safeName = if (outputFileName.endsWith(".m4a", ignoreCase = true)) outputFileName else "$outputFileName.m4a"
        val outFile = File(outDir, safeName.replace("[^a-zA-Z0-9._-]".toRegex(), "_"))
        if (outFile.exists()) outFile.delete()

        return try {
            val audioItems = withContext(Dispatchers.IO) {
                clips.map { clip ->
                    val uri = resolvePlayableUriForExport(context, clip)
                        ?: throw IllegalStateException("Source file unreadable for '${clip.name}'.")
                    val mediaItem = MediaItem.Builder()
                        .setUri(uri)
                        .setClippingConfiguration(
                            MediaItem.ClippingConfiguration.Builder()
                                .setStartPositionMs(clip.trimInMs.coerceAtLeast(0L))
                                .setEndPositionMs(clip.trimOutMs.coerceAtLeast(clip.trimInMs + 200L))
                                .build()
                        )
                        .build()
                    EditedMediaItem.Builder(mediaItem)
                        .setRemoveVideo(true)
                        .build()
                }
            }
            val composition = Composition.Builder(
                listOf(EditedMediaItemSequence.Builder(audioItems).build())
            ).build()

            val res = withContext(Dispatchers.Main) {
                suspendCancellableCoroutine<Result<File>> { cont ->
                    var finished = false
                    val transformer = Transformer.Builder(context)
                        .setAudioMimeType(MimeTypes.AUDIO_AAC)
                        .build()
                    transformer.addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            if (finished) return
                            finished = true
                            if (cont.isActive) cont.resume(Result.success(outFile))
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            if (finished) return
                            finished = true
                            if (cont.isActive) {
                                cont.resume(
                                    Result.failure(
                                        IllegalStateException("Audio extraction failed: ${exportException.message}", exportException)
                                    )
                                )
                            }
                        }
                    })
                    cont.invokeOnCancellation {
                        finished = true
                        runCatching { transformer.cancel() }
                    }
                    transformer.start(composition, outFile.absolutePath)
                }
            }
            res.onFailure { outFile.delete() }
            res
        } catch (e: Exception) {
            outFile.delete()
            MediaDiagnostics.error("AUDIO_EXTRACT", "Failed extracting M4A audio", e.message ?: "", e)
            Result.failure(e)
        }
    }

    private suspend fun buildComposition(
        context: Context,
        clips: List<VideoClip>,
        audioClips: List<AudioTrackItem>,
        textOverlays: List<AppTextOverlay>,
        imageOverlays: List<ImageOverlay>,
        targetWidth: Int,
        targetHeight: Int,
        fps: Int,
        isVideoTrackMuted: Boolean,
        isAudioTrackMuted: Boolean,
        isAutoDuckingMasterEnabled: Boolean,
        autoDuckingLevel: Float,
        speechWindows: List<Pair<Long, Long>>
    ): Composition {
        val editedVideoItems = mutableListOf<EditedMediaItem>()
        for (clip in clips) {
            val resolvedUri = resolvePlayableUriForExport(context, clip)
                ?: throw IllegalStateException("Source file missing or unreadable for clip '${clip.name}'.")

            val mediaItem = MediaItem.Builder()
                .setUri(resolvedUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.trimInMs.coerceAtLeast(0L))
                        .setEndPositionMs(clip.trimOutMs.coerceAtLeast(clip.trimInMs + 100L))
                        .build()
                )
                .build()

            val videoEffects = buildVideoEffectsForClip(
                context = context,
                clip = clip,
                textOverlays = textOverlays,
                imageOverlays = imageOverlays,
                targetWidth = targetWidth,
                targetHeight = targetHeight
            )

            val audioProcessors = mutableListOf<AudioProcessor>()
            if (clip.speed != 1.0f) {
                audioProcessors.add(SonicAudioProcessor().apply { setSpeed(clip.speed.coerceIn(0.25f, 4f)) })
            }
            audioProcessors.add(
                DuckingAndEnvelopeAudioProcessor(
                    baseVolume = clip.volume,
                    isMuted = clip.isMuted || isVideoTrackMuted || !clip.hasAudio,
                    trackStartTimelineMs = clip.timelineStartMs,
                    trackDurationMs = clip.trimmedDurationMs,
                    fadeInMs = clip.fadeInMs,
                    fadeOutMs = clip.fadeOutMs,
                    applyDucking = false,
                    duckLevel = 1f,
                    speechWindowsMs = speechWindows
                )
            )

            val editedItem = EditedMediaItem.Builder(mediaItem)
                .setEffects(Effects(audioProcessors, videoEffects))
                .setFrameRate(fps)
                .build()
            editedVideoItems.add(editedItem)
        }

        val sequences = mutableListOf<EditedMediaItemSequence>()
        val hasAudioTracksWithRealUris = audioClips.any {
            it.uri.isNotBlank() && !it.uri.startsWith("sample://") && File(it.uri.removePrefix("file://")).exists()
        }
        val videoSeq = EditedMediaItemSequence.Builder(editedVideoItems).build()
        sequences.add(videoSeq)

        if (hasAudioTracksWithRealUris && !isAudioTrackMuted) {
            val audioItems = audioClips.mapNotNull { track ->
                val uriStr = track.uri
                if (uriStr.isBlank() || uriStr.startsWith("sample://")) return@mapNotNull null
                val audioMediaItem = MediaItem.Builder()
                    .setUri(uriStr)
                    .setClippingConfiguration(
                        MediaItem.ClippingConfiguration.Builder()
                            .setStartPositionMs(0L)
                            .setEndPositionMs(track.durationMs.coerceAtLeast(200L))
                            .build()
                    )
                    .build()

                val processor = DuckingAndEnvelopeAudioProcessor(
                    baseVolume = track.volume,
                    isMuted = track.isMuted,
                    trackStartTimelineMs = track.timelineStartMs,
                    trackDurationMs = track.durationMs,
                    fadeInMs = track.fadeInMs,
                    fadeOutMs = track.fadeOutMs,
                    applyDucking = isAutoDuckingMasterEnabled && (track.isDuckingEnabled || track.category == AudioCategory.MUSIC),
                    duckLevel = autoDuckingLevel,
                    speechWindowsMs = speechWindows
                )
                EditedMediaItem.Builder(audioMediaItem)
                    .setRemoveVideo(true)
                    .setEffects(Effects(listOf(processor), emptyList()))
                    .build()
            }
            if (audioItems.isNotEmpty()) {
                sequences.add(EditedMediaItemSequence.Builder(audioItems).build())
            }
        }

        return Composition.Builder(sequences).build()
    }

    private suspend fun resolvePlayableUriForExport(context: Context, clip: VideoClip): Uri? {
        val raw = clip.uri
        if (raw.isBlank()) return null
        if (raw.startsWith("content://")) return Uri.parse(raw)
        if (raw.startsWith("sample://")) {
            // If user explicitly added a curated sample clip, create/reuse a real MP4 file on storage so Transformer can mux it cleanly
            val sampleFile = DeviceMediaImporter.createDeviceTestVideo(context, "${clip.name}.mp4")
            return Uri.fromFile(sampleFile)
        }
        val file = File(raw.removePrefix("file://"))
        return if (file.exists() && file.length() > 0L) Uri.fromFile(file) else null
    }

    private fun buildVideoEffectsForClip(
        context: Context,
        clip: VideoClip,
        textOverlays: List<AppTextOverlay>,
        imageOverlays: List<ImageOverlay>,
        targetWidth: Int,
        targetHeight: Int
    ): List<Effect> {
        val effects = mutableListOf<Effect>()

        // 1. Normalized Crop Rect
        if (clip.hasCrop) {
            val ndcLeft = (-1f + clip.cropLeft * 2f).coerceIn(-1f, 0.9f)
            val ndcRight = (-1f + clip.cropRight * 2f).coerceIn(ndcLeft + 0.05f, 1f)
            val ndcTop = (1f - clip.cropTop * 2f).coerceIn(-0.9f, 1f)
            val ndcBottom = (1f - clip.cropBottom * 2f).coerceIn(-1f, ndcTop - 0.05f)
            effects.add(Crop(ndcLeft, ndcRight, ndcBottom, ndcTop))
        }

        // 2. Scale, Flip, and Rotation
        val scaleX = (if (clip.flipHorizontal) -clip.scale else clip.scale).coerceIn(-4f, 4f)
        val scaleY = clip.scale.coerceIn(0.1f, 4f)
        if (scaleX != 1f || scaleY != 1f || clip.rotation != 0f) {
            effects.add(
                ScaleAndRotateTransformation.Builder()
                    .setScale(scaleX, scaleY)
                    .setRotationDegrees((-clip.rotation) % 360f)
                    .build()
            )
        }

        // 3. Resize / Output Presentation
        val outW = if (clip.resizeWidth > 0) clip.resizeWidth else targetWidth
        val outH = if (clip.resizeHeight > 0) clip.resizeHeight else targetHeight
        effects.add(
            Presentation.createForWidthAndHeight(
                outW,
                outH,
                Presentation.LAYOUT_SCALE_TO_FIT
            )
        )

        // 4. Brightness, Contrast, Saturation & Filters
        val brightnessGain = (1f + clip.brightness).coerceIn(0.3f, 2f)
        val (filterR, filterG, filterB, filterSatAdjust) = when (clip.filter) {
            VideoFilter.CINEMATIC -> arrayOf(1.05f, 1.0f, 1.12f, 10f)
            VideoFilter.GOLDEN_HOUR -> arrayOf(1.18f, 1.04f, 0.85f, 15f)
            VideoFilter.COLD_NORDIC -> arrayOf(0.88f, 0.98f, 1.18f, -10f)
            VideoFilter.SEPIA -> arrayOf(1.15f, 0.98f, 0.78f, -65f)
            VideoFilter.FILM_NOIR -> arrayOf(1.0f, 1.0f, 1.0f, -100f)
            VideoFilter.CYBERPUNK -> arrayOf(1.12f, 0.88f, 1.22f, 25f)
            else -> arrayOf(1f, 1f, 1f, 0f)
        }
        val rgbBuilder = RgbAdjustment.Builder()
            .setRedScale((brightnessGain * filterR).coerceIn(0.2f, 3f))
            .setGreenScale((brightnessGain * filterG).coerceIn(0.2f, 3f))
            .setBlueScale((brightnessGain * filterB).coerceIn(0.2f, 3f))
        effects.add(rgbBuilder.build())

        if (clip.contrast != 1.0f) {
            effects.add(Contrast((clip.contrast - 1.0f).coerceIn(-0.8f, 0.8f)))
        }
        val satDelta = ((clip.saturation - 1.0f) * 100f + filterSatAdjust).coerceIn(-100f, 100f)
        if (satDelta != 0f) {
            effects.add(HslAdjustment.Builder().adjustSaturation(satDelta).build())
        }

        // 5. Overlays (Text + Image Overlays overlapping this clip's timeline window)
        val textureOverlays = mutableListOf<TextureOverlay>()
        val clipStart = clip.timelineStartMs
        val clipEnd = clip.timelineEndMs

        textOverlays.filter { it.timelineEndMs > clipStart && it.timelineStartMs < clipEnd }.forEach { overlay ->
            val spannable = SpannableString(overlay.text).apply {
                setSpan(
                    ForegroundColorSpan(overlay.textColorHex.toInt()),
                    0,
                    length,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            val ndcX = ((overlay.positionX - 0.5f) * 1.6f).coerceIn(-0.9f, 0.9f)
            val ndcY = ((0.5f - overlay.positionY) * 1.6f).coerceIn(-0.9f, 0.9f)
            val settings = StaticOverlaySettings.Builder()
                .setBackgroundFrameAnchor(ndcX, ndcY)
                .setAlphaScale(1f)
                .build()
            textureOverlays.add(TextOverlay.createStaticTextOverlay(spannable, settings))
        }

        imageOverlays.filter { it.timelineEndMs > clipStart && it.timelineStartMs < clipEnd }.forEach { img ->
            loadScaledOverlayBitmap(context, img.uri, maxDim = 640)?.let { bmp ->
                val ndcX = ((img.positionX - 0.5f) * 1.8f).coerceIn(-0.9f, 0.9f)
                val ndcY = ((0.5f - img.positionY) * 1.8f).coerceIn(-0.9f, 0.9f)
                val settings = StaticOverlaySettings.Builder()
                    .setBackgroundFrameAnchor(ndcX, ndcY)
                    .setScale(img.scale.coerceIn(0.1f, 2f), img.scale.coerceIn(0.1f, 2f))
                    .setRotationDegrees(-img.rotation)
                    .setAlphaScale(img.alpha.coerceIn(0f, 1f))
                    .build()
                textureOverlays.add(BitmapOverlay.createStaticBitmapOverlay(bmp, settings))
            }
        }

        if (textureOverlays.isNotEmpty()) {
            effects.add(OverlayEffect(ImmutableList.copyOf(textureOverlays)))
        }

        return effects
    }

    private fun loadScaledOverlayBitmap(context: Context, uriStr: String, maxDim: Int): Bitmap? {
        return try {
            val raw = if (uriStr.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { BitmapFactory.decodeStream(it) }
            } else {
                val file = File(uriStr.removePrefix("file://"))
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } ?: return null

            val ratio = min(maxDim.toFloat() / raw.width.coerceAtLeast(1), maxDim.toFloat() / raw.height.coerceAtLeast(1))
            if (ratio < 1f) {
                val scaled = Bitmap.createScaledBitmap(
                    raw,
                    (raw.width * ratio).toInt().coerceAtLeast(1),
                    (raw.height * ratio).toInt().coerceAtLeast(1),
                    true
                )
                raw.recycle()
                scaled
            } else raw
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun runTransformerOnMainThread(
        context: Context,
        composition: Composition,
        outputFile: File,
        bitrateMbps: Int,
        videoMimeType: String = MimeTypes.VIDEO_H264,
        onProgress: (Float) -> Unit
    ): Result<ExportResult> = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            val handler = Handler(Looper.getMainLooper())
            val progressHolder = ProgressHolder()
            var finished = false

            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setRequestedVideoEncoderSettings(
                    VideoEncoderSettings.Builder()
                        .setBitrate((bitrateMbps.coerceIn(1, 50)) * 1_000_000)
                        .build()
                )
                .build()

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(videoMimeType)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .setEncoderFactory(encoderFactory)
                .build()

            activeTransformer = transformer

            val progressPoller = object : Runnable {
                override fun run() {
                    if (finished) return
                    val state = transformer.getProgress(progressHolder)
                    if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                        onProgress((progressHolder.progress / 100f).coerceIn(0f, 0.99f))
                    }
                    if (!finished && state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                        handler.postDelayed(this, 250L)
                    } else if (!finished) {
                        handler.postDelayed(this, 250L)
                    }
                }
            }

            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    if (finished) return
                    finished = true
                    handler.removeCallbacks(progressPoller)
                    activeTransformer = null
                    onProgress(1f)
                    if (cont.isActive) cont.resume(Result.success(exportResult))
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    if (finished) return
                    finished = true
                    handler.removeCallbacks(progressPoller)
                    activeTransformer = null
                    if (cont.isActive) {
                        cont.resume(
                            Result.failure(
                                IllegalStateException(
                                    "Video export failed (${exportException.errorCodeName}): ${exportException.message}",
                                    exportException
                                )
                            )
                        )
                    }
                }
            })

            cont.invokeOnCancellation {
                finished = true
                handler.removeCallbacks(progressPoller)
                try {
                    transformer.cancel()
                } catch (_: Exception) {}
                activeTransformer = null
            }

            transformer.start(composition, outputFile.absolutePath)
            handler.postDelayed(progressPoller, 250L)
        }
    }

    /**
     * Publishes an exported MP4 into shared system Movies/OpenCut folder via MediaStore (no storage permission required on Android 10+).
     */
    fun saveMp4ToMediaStore(context: Context, sourceFile: File, displayName: String): String? {
        if (!sourceFile.exists() || sourceFile.length() <= 0L) return null
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/OpenCut")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(out)
                }
                out.flush()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val doneValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                resolver.update(uri, doneValues, null, null)
            }
            uri.toString()
        } catch (e: Exception) {
            MediaDiagnostics.warn("EXPORT_MEDIASTORE", "Could not publish to MediaStore Movies", e.message ?: "")
            null
        }
    }

    /**
     * Copies an exported MP4 to a user-selected Storage Access Framework document URI.
     */
    suspend fun copyExportedFileToUri(context: Context, sourcePath: String, destinationUri: Uri): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                val src = File(sourcePath.removePrefix("file://"))
                if (!src.exists()) return@withContext Result.failure(IllegalStateException("Exported file no longer exists."))
                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    src.inputStream().use { input ->
                        input.copyTo(out)
                    }
                    out.flush()
                } ?: return@withContext Result.failure(IllegalStateException("Cannot write to selected folder."))
                Result.success(src.length())
            } catch (e: Exception) {
                MediaDiagnostics.error("EXPORT_SAF", "Failed writing to user destination", destinationUri.toString(), e)
                Result.failure(e)
            }
        }

    fun getShareableFileUri(context: Context, filePath: String): Uri? {
        val file = File(filePath.removePrefix("file://"))
        if (!file.exists()) return null
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }
    }

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (mgr.getNotificationChannel(EXPORT_NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    EXPORT_NOTIFICATION_CHANNEL_ID,
                    "Video Exports",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications when MP4 exports complete"
                }
                mgr.createNotificationChannel(channel)
            }
        }
    }

    fun postExportCompletionNotification(context: Context, fileName: String, durationMs: Long): Boolean {
        ensureNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return try {
            val notification = NotificationCompat.Builder(context, EXPORT_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("MP4 Export Complete")
                .setContentText("Saved $fileName (${durationMs / 1000}s) to Movies/OpenCut")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(EXPORT_NOTIFICATION_ID, notification)
            true
        } catch (_: Exception) {
            false
        }
    }
}
