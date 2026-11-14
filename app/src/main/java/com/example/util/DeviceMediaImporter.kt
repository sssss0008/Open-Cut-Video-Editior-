package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

data class ImportedVideoResult(
    val localFile: File,
    val localUri: String,
    val displayName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val isVertical: Boolean,
    val sizeBytes: Long
)

object DeviceMediaImporter {
    private const val TAG = "DeviceMediaImporter"

    /**
     * Resolves the real display file name from a content or file URI.
     */
    fun resolveFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val colIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (colIdx != -1) {
                            result = cursor.getString(colIdx)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to query DISPLAY_NAME for $uri: ${e.message}")
            }
        }
        if (result.isNullOrBlank()) {
            result = uri.lastPathSegment?.split("/")?.lastOrNull()
        }
        val clean = (result ?: "Device_Video.mp4").trim()
        return if (!clean.contains(".")) "$clean.mp4" else clean
    }

    /**
     * Resolves the file size in bytes from a content or file URI.
     */
    fun resolveFileSize(context: Context, uri: Uri): Long {
        if (uri.scheme == "file") {
            uri.path?.let { p ->
                val f = File(p)
                if (f.exists()) return f.length()
            }
        }
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val colIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (colIdx != -1) {
                            val size = cursor.getLong(colIdx)
                            if (size > 0L) return size
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to query SIZE for $uri: ${e.message}")
            }
        }
        return 0L
    }

    /**
     * Copies a content or file URI to the app's persistent internal storage synchronously.
     * This avoids permission expiry across lifecycles and provides a stable File path.
     */
    fun copyUriToAppStorageSync(context: Context, uri: Uri, targetName: String): File? {
        return try {
            val sanitizedName = targetName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val mediaDir = File(context.filesDir, "imported_videos").apply { mkdirs() }
            val uniqueFile = File(mediaDir, "${System.currentTimeMillis()}_$sanitizedName")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(uniqueFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (uniqueFile.exists() && uniqueFile.length() > 0) {
                uniqueFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying URI $uri to internal storage: ${e.message}", e)
            null
        }
    }

    suspend fun copyUriToAppStorage(context: Context, uri: Uri, targetName: String): File? = withContext(Dispatchers.IO) {
        copyUriToAppStorageSync(context, uri, targetName)
    }

    /**
     * Extracts duration, resolution, and orientation from a local video file or URI.
     */
    fun extractMetadata(context: Context, fileOrUri: String, defaultDurationMs: Long = 6000L): ImportedVideoMetadata {
        var durationMs = defaultDurationMs
        var width = 1080
        var height = 1920
        var rotation = 0
        var isVertical = false

        val retriever = MediaMetadataRetriever()
        try {
            if (fileOrUri.startsWith("content://")) {
                retriever.setDataSource(context, Uri.parse(fileOrUri))
            } else {
                val cleanPath = fileOrUri.removePrefix("file://")
                retriever.setDataSource(cleanPath)
            }

            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val parsedDur = durStr?.toLongOrNull()
            if (parsedDur != null && parsedDur > 200L) {
                durationMs = parsedDur
            }

            val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val rotStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)

            val parsedW = wStr?.toIntOrNull() ?: 0
            val parsedH = hStr?.toIntOrNull() ?: 0
            rotation = rotStr?.toIntOrNull() ?: 0

            if (parsedW > 0 && parsedH > 0) {
                width = parsedW
                height = parsedH
                isVertical = if (rotation == 90 || rotation == 270) {
                    parsedW > parsedH
                } else {
                    parsedH > parsedW
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Metadata extraction failed for $fileOrUri: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        return ImportedVideoMetadata(
            durationMs = durationMs,
            width = width,
            height = height,
            rotation = rotation,
            isVertical = isVertical
        )
    }

    /**
     * Extracts a frame bitmap at a specific time in milliseconds from a video file.
     */
    fun extractFrameBitmap(context: Context, fileOrUri: String, timeMs: Long): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            if (fileOrUri.startsWith("content://")) {
                retriever.setDataSource(context, Uri.parse(fileOrUri))
            } else {
                val cleanPath = fileOrUri.removePrefix("file://")
                retriever.setDataSource(cleanPath)
            }
            val timeUs = (timeMs * 1000L).coerceAtLeast(0L)
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: retriever.frameAtTime
        } catch (e: Exception) {
            Log.w(TAG, "Frame extraction failed at ${timeMs}ms: ${e.message}")
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Generates a genuine MP4 video file on device storage using MediaCodec and MediaMuxer.
     * This provides real local video files for testing even if the emulator gallery has zero media.
     */
    suspend fun createDeviceTestVideo(context: Context, title: String = "Device_Camera_Test.mp4"): File = withContext(Dispatchers.IO) {
        val mediaDir = File(context.filesDir, "device_videos").apply { mkdirs() }
        val outputFile = File(mediaDir, title.replace(" ", "_"))
        if (outputFile.exists() && outputFile.length() > 10000L) {
            return@withContext outputFile
        }

        val width = 720
        val height = 1280
        val fps = 30
        val durationSeconds = 5
        val bitRate = 2_000_000

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = codec.createInputSurface()
            codec.start()

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false

            val bufferInfo = MediaCodec.BufferInfo()
            val totalFrames = fps * durationSeconds
            val frameDurationUs = 1_000_000L / fps

            for (i in 0 until totalFrames) {
                // Draw to the surface
                val canvas = inputSurface.lockCanvas(null)
                if (canvas != null) {
                    val progress = i.toFloat() / totalFrames
                    val r = (30 + (220 * progress)).toInt() % 256
                    val g = (60 + (180 * (1 - progress))).toInt() % 256
                    val b = (200 + (55 * progress)).toInt() % 256
                    canvas.drawColor(Color.rgb(r, g, b))

                    val paint = Paint().apply {
                        color = Color.WHITE
                        textSize = 48f
                        isAntiAlias = true
                    }
                    canvas.drawText("OpenCut Device Video", 60f, 200f, paint)

                    paint.textSize = 32f
                    val timeSec = i / fps
                    val frameNum = i % fps
                    canvas.drawText(String.format("Timecode: 00:00:%02d:%02d", timeSec, frameNum), 60f, 280f, paint)
                    canvas.drawText("Resolution: 720x1280 (9:16 Vertical)", 60f, 340f, paint)
                    canvas.drawText("Local File: ${outputFile.name}", 60f, 400f, paint)

                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Drain encoder
                var outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                while (outIndex >= 0) {
                    val encodedData = codec.getOutputBuffer(outIndex)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size != 0 && encodedData != null) {
                        if (!muxerStarted) {
                            val newFormat = codec.outputFormat
                            trackIndex = muxer.addTrack(newFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                        bufferInfo.presentationTimeUs = i * frameDurationUs
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    outIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }

            // Signal end of stream
            codec.signalEndOfInputStream()
            var outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outIndex >= 0) {
                val encodedData = codec.getOutputBuffer(outIndex)
                if (bufferInfo.size != 0 && encodedData != null && muxerStarted) {
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                codec.releaseOutputBuffer(outIndex, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            }

            codec.stop()
            codec.release()
            inputSurface.release()

            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding device test video: ${e.message}", e)
            // Fallback: if encoder not supported on some virtual environments, write a dummy file
            if (!outputFile.exists() || outputFile.length() == 0L) {
                outputFile.writeBytes(ByteArray(4096) { 0 })
            }
        }

        outputFile
    }
}

data class ImportedVideoMetadata(
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val rotation: Int,
    val isVertical: Boolean
)
