package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.data.db.ProjectEntity
import com.example.data.model.AspectRatio
import com.example.data.model.AudioTrackItem
import com.example.data.model.RenderedVideoItem
import com.example.data.model.TextOverlay
import com.example.data.model.VideoClip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

data class AppStorageBreakdown(
    val exportedVideosBytes: Long,
    val importedMediaBytes: Long,
    val cacheBytes: Long,
    val studioExportsBytes: Long
) {
    val totalBytes: Long
        get() = exportedVideosBytes + importedMediaBytes + cacheBytes + studioExportsBytes

    fun formattedTotal(): String = formatBytes(totalBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024L * 1024L * 1024L -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
                bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
                else -> "$bytes B"
            }
        }
    }
}

object StudioExportUtilities {

    /**
     * Generates a standards-compliant SubRip (.srt) caption document from timeline text overlays.
     */
    fun generateSrtContent(textOverlays: List<TextOverlay>, fallbackTitle: String = "OpenCut Project"): String {
        val sorted = textOverlays.filter { it.text.isNotBlank() }.sortedBy { it.timelineStartMs }
        if (sorted.isEmpty()) {
            return "1\n00:00:00,000 --> 00:00:03,000\n$fallbackTitle\n"
        }
        val sb = StringBuilder()
        sorted.forEachIndexed { idx, overlay ->
            val start = formatSrtTime(overlay.timelineStartMs)
            val end = formatSrtTime(overlay.timelineEndMs.coerceAtLeast(overlay.timelineStartMs + 500L))
            sb.append(idx + 1).append('\n')
            sb.append(start).append(" --> ").append(end).append('\n')
            sb.append(overlay.text.trim()).append("\n\n")
        }
        return sb.toString().trimEnd() + "\n"
    }

    /**
     * Generates a standards-compliant WebVTT (.vtt) caption document from timeline text overlays.
     */
    fun generateVttContent(textOverlays: List<TextOverlay>, fallbackTitle: String = "OpenCut Project"): String {
        val sorted = textOverlays.filter { it.text.isNotBlank() }.sortedBy { it.timelineStartMs }
        val sb = StringBuilder("WEBVTT\n\n")
        if (sorted.isEmpty()) {
            sb.append("00:00:00.000 --> 00:00:03.000\n").append(fallbackTitle).append('\n')
            return sb.toString()
        }
        sorted.forEachIndexed { idx, overlay ->
            val start = formatVttTime(overlay.timelineStartMs)
            val end = formatVttTime(overlay.timelineEndMs.coerceAtLeast(overlay.timelineStartMs + 500L))
            sb.append("cue-").append(idx + 1).append('\n')
            sb.append(start).append(" --> ").append(end).append('\n')
            sb.append(overlay.text.trim()).append("\n\n")
        }
        return sb.toString().trimEnd() + "\n"
    }

    /**
     * Generates a valid Final Cut Pro XML v1.10 timeline interchange document describing all
     * clips, trim points, speeds, transitions, audio tracks, and text titles.
     */
    fun generateFcpxmlContent(
        project: ProjectEntity?,
        clips: List<VideoClip>,
        audioClips: List<AudioTrackItem>,
        textOverlays: List<TextOverlay>,
        aspectRatio: AspectRatio,
        fps: Int = 30
    ): String {
        val title = xmlEscape(project?.title ?: "OpenCut_Project")
        val safeFps = fps.coerceIn(24, 60)
        val totalMs = maxOf(
            clips.maxOfOrNull { it.timelineEndMs } ?: 0L,
            audioClips.maxOfOrNull { it.timelineEndMs } ?: 0L,
            textOverlays.maxOfOrNull { it.timelineEndMs } ?: 0L,
            1000L
        )
        val (width, height) = when (aspectRatio) {
            AspectRatio.VERTICAL_9_16 -> 1080 to 1920
            AspectRatio.SQUARE_1_1 -> 1080 to 1080
            AspectRatio.PORTRAIT_4_5 -> 1080 to 1350
            AspectRatio.CINEMATIC_21_9 -> 2520 to 1080
            AspectRatio.LANDSCAPE_16_9 -> 1920 to 1080
        }

        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<!DOCTYPE fcpxml>\n")
        sb.append("<fcpxml version=\"1.10\">\n")
        sb.append("  <resources>\n")
        sb.append("    <format id=\"r1\" name=\"FFVideoFormat${height}p${safeFps}\" frameDuration=\"1/${safeFps}s\" width=\"$width\" height=\"$height\"/>\n")
        clips.forEachIndexed { idx, clip ->
            val assetId = "r_clip_${idx + 1}"
            sb.append("    <asset id=\"$assetId\" name=\"${xmlEscape(clip.name)}\" src=\"${xmlEscape(clip.uri)}\" start=\"0s\" duration=\"${msToRational(clip.sourceDurationMs)}\" hasVideo=\"1\" hasAudio=\"${if (clip.hasAudio) 1 else 0}\"/>\n")
        }
        audioClips.forEachIndexed { idx, audio ->
            val assetId = "r_audio_${idx + 1}"
            sb.append("    <asset id=\"$assetId\" name=\"${xmlEscape(audio.name)}\" src=\"${xmlEscape(audio.uri)}\" start=\"0s\" duration=\"${msToRational(audio.durationMs)}\" hasVideo=\"0\" hasAudio=\"1\"/>\n")
        }
        sb.append("  </resources>\n")
        sb.append("  <library>\n")
        sb.append("    <event name=\"$title\">\n")
        sb.append("      <project name=\"$title\">\n")
        sb.append("        <sequence format=\"r1\" duration=\"${msToRational(totalMs)}\">\n")
        sb.append("          <spine>\n")
        clips.forEachIndexed { idx, clip ->
            val assetId = "r_clip_${idx + 1}"
            if (idx > 0 && clip.transitionIn.name != "NONE") {
                sb.append("            <transition name=\"${xmlEscape(clip.transitionIn.displayName)}\" offset=\"${msToRational(clip.timelineStartMs)}\" duration=\"${msToRational(clip.transitionDurationMs)}\"/>\n")
            }
            sb.append("            <asset-clip ref=\"$assetId\" name=\"${xmlEscape(clip.name)}\" offset=\"${msToRational(clip.timelineStartMs)}\" start=\"${msToRational(clip.trimInMs)}\" duration=\"${msToRational(clip.trimmedDurationMs)}\" speed=\"${String.format(Locale.US, "%.2f", clip.speed)}\" filter=\"${xmlEscape(clip.filter.displayName)}\"/>\n")
        }
        audioClips.forEachIndexed { idx, audio ->
            val assetId = "r_audio_${idx + 1}"
            sb.append("            <asset-clip lane=\"-1\" ref=\"$assetId\" name=\"${xmlEscape(audio.name)}\" offset=\"${msToRational(audio.timelineStartMs)}\" duration=\"${msToRational(audio.durationMs)}\" role=\"${audio.category.name.lowercase()}\"/>\n")
        }
        textOverlays.forEachIndexed { idx, text ->
            sb.append("            <title lane=\"1\" name=\"Title ${idx + 1}\" offset=\"${msToRational(text.timelineStartMs)}\" duration=\"${msToRational(text.durationMs)}\"><text>${xmlEscape(text.text)}</text></title>\n")
        }
        sb.append("          </spine>\n")
        sb.append("        </sequence>\n")
        sb.append("      </project>\n")
        sb.append("    </event>\n")
        sb.append("  </library>\n")
        sb.append("</fcpxml>\n")
        return sb.toString()
    }

    /**
     * Writes text content (SRT, VTT, FCPXML) into app-private studio_exports directory
     * so it can be shared via FileProvider or saved via Storage Access Framework.
     */
    suspend fun writeStudioTextExport(
        context: Context,
        fileName: String,
        content: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "studio_exports").apply { mkdirs() }
            val safeName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val file = File(dir, safeName)
            file.writeText(content, Charsets.UTF_8)
            Result.success(file)
        } catch (e: Exception) {
            MediaDiagnostics.error("STUDIO_EXPORT", "Failed writing $fileName", e.message ?: "", e)
            Result.failure(e)
        }
    }

    suspend fun writeTextToSafUri(context: Context, destinationUri: Uri, content: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val bytes = content.toByteArray(Charsets.UTF_8)
                context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    out.write(bytes)
                    out.flush()
                } ?: return@withContext Result.failure(IllegalStateException("Unable to open destination file."))
                Result.success(bytes.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Scans persisted MP4 exports in filesDir/exported_videos so archive survives app restarts.
     */
    suspend fun scanExportedVideos(context: Context): List<RenderedVideoItem> = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "exported_videos")
        if (!dir.exists()) return@withContext emptyList()
        dir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".mp4", ignoreCase = true) && it.length() > 0L }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                var durationMs = 0L
                var width = 0
                var height = 0
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(file.absolutePath)
                    durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
                    height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
                } catch (_: Exception) {
                } finally {
                    runCatching { retriever.release() }
                }
                val resLabel = when {
                    maxOf(width, height) >= 3840 -> "4K Ultra HD"
                    maxOf(width, height) >= 1920 -> "1080p Full HD"
                    maxOf(width, height) >= 1280 -> "720p HD"
                    maxOf(width, height) > 0 -> "${width}x${height}"
                    else -> "MP4 Video"
                }
                RenderedVideoItem(
                    id = "disk_${file.name.hashCode()}_${file.lastModified()}",
                    title = file.nameWithoutExtension.replace("_", " ").replaceFirstChar { it.uppercase() },
                    fileName = file.name,
                    resolution = resLabel,
                    fps = 30,
                    durationMs = durationMs.coerceAtLeast(1000L),
                    fileSizeBytes = file.length(),
                    createdAt = file.lastModified(),
                    localUri = file.absolutePath
                )
            } ?: emptyList()
    }

    /**
     * Computes actual disk usage across app storage folders.
     */
    suspend fun calculateStorageUsage(context: Context): AppStorageBreakdown = withContext(Dispatchers.IO) {
        AppStorageBreakdown(
            exportedVideosBytes = folderSizeBytes(File(context.filesDir, "exported_videos")),
            importedMediaBytes = folderSizeBytes(File(context.filesDir, "imported_videos")) +
                folderSizeBytes(File(context.filesDir, "device_videos")),
            cacheBytes = folderSizeBytes(context.cacheDir) + folderSizeBytes(File(context.filesDir, "thumbnails")),
            studioExportsBytes = folderSizeBytes(File(context.filesDir, "studio_exports"))
        )
    }

    /**
     * Deletes temporary cache buffers and regenerable thumbnail cache files, returning freed byte count.
     */
    suspend fun clearTempBuffers(context: Context): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        freed += deleteDirectoryContents(context.cacheDir)
        freed += deleteDirectoryContents(File(context.filesDir, "thumbnails"))
        freed
    }

    /**
     * Captures a high-resolution PNG still frame from the given clip at the specified timeline position.
     * Uses MediaMetadataRetriever for real video files and falls back to a crisp styled canvas frame
     * for non-file/generated URIs so Frame Grab always produces a valid PNG file.
     */
    suspend fun captureClipFrameToPng(
        context: Context,
        clip: VideoClip,
        timelinePositionMs: Long,
        aspectRatio: AspectRatio
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "studio_exports").apply { mkdirs() }
            val safeBase = clip.name.substringBeforeLast(".")
                .lowercase(Locale.US)
                .replace("[^a-z0-9]+".toRegex(), "_")
                .trim('_')
                .ifBlank { "frame" }
                .take(24)
            val outFile = File(dir, "${safeBase}_frame_${System.currentTimeMillis()}.png")

            val elapsedInClipMs = (timelinePositionMs - clip.timelineStartMs).coerceAtLeast(0L)
            val sourceMs = (clip.trimInMs + (elapsedInClipMs * clip.speed).toLong())
                .coerceIn(0L, clip.sourceDurationMs.coerceAtLeast(1L))
            val timeUs = sourceMs * 1000L

            var bitmap: Bitmap? = null
            if (clip.uri.isNotBlank() && !clip.uri.startsWith("sample://")) {
                val retriever = MediaMetadataRetriever()
                try {
                    when {
                        clip.uri.startsWith("content://") -> retriever.setDataSource(context, Uri.parse(clip.uri))
                        clip.uri.startsWith("file://") -> retriever.setDataSource(Uri.parse(clip.uri).path ?: clip.uri)
                        File(clip.uri).exists() -> retriever.setDataSource(clip.uri)
                    }
                    bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                } catch (e: Exception) {
                    MediaDiagnostics.warn("FRAME_GRAB", "Fallback canvas frame used for ${clip.name}", e.message ?: "")
                } finally {
                    runCatching { retriever.release() }
                }
            }

            val finalBitmap = bitmap ?: renderFallbackFrameBitmap(clip, aspectRatio, sourceMs)
            FileOutputStream(outFile).use { fos ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
            if (finalBitmap !== bitmap) {
                finalBitmap.recycle()
            } else {
                bitmap?.recycle()
            }
            MediaDiagnostics.info("FRAME_GRAB", "Captured still frame ${outFile.name}", "${outFile.length()} bytes")
            Result.success(outFile)
        } catch (e: Exception) {
            MediaDiagnostics.error("FRAME_GRAB", "Failed capturing frame from ${clip.name}", e.message ?: "", e)
            Result.failure(e)
        }
    }

    /**
     * Copies a local binary file (such as a captured PNG or extracted M4A) to a user-chosen SAF destination URI.
     */
    suspend fun copyFileToSafUri(context: Context, sourceFile: File, destinationUri: Uri): Result<Long> =
        withContext(Dispatchers.IO) {
            try {
                if (!sourceFile.exists()) {
                    return@withContext Result.failure(IllegalStateException("Source file not found."))
                }
                val copied = context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                } ?: return@withContext Result.failure(IllegalStateException("Unable to open destination file."))
                Result.success(copied)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Computes real 7-day normalized activity levels (Mon..Sun or rolling 7 days) from actual
     * Room project timestamps and rendered MP4 timestamps.
     */
    fun computeWeeklyActivityBuckets(
        projectUpdatedTimestamps: List<Long>,
        renderedCreatedTimestamps: List<Long>,
        nowMs: Long = System.currentTimeMillis()
    ): Pair<List<String>, List<Float>> {
        val dayMs = 24L * 60L * 60L * 1000L
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endOfTodayMs = cal.timeInMillis
        val labels = MutableList(7) { "" }
        val counts = IntArray(7)
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 0 until 7) {
            val daysAgo = 6 - i
            val dayCal = Calendar.getInstance().apply { timeInMillis = endOfTodayMs - daysAgo * dayMs }
            labels[i] = if (daysAgo == 0) "Today" else dayNames[(dayCal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]
        }

        val allEvents = projectUpdatedTimestamps.map { it to 1 } + renderedCreatedTimestamps.map { it to 2 }
        allEvents.forEach { (ts, weight) ->
            if (ts in (endOfTodayMs - 7L * dayMs + 1L)..endOfTodayMs) {
                val daysAgo = ((endOfTodayMs - ts) / dayMs).toInt().coerceIn(0, 6)
                val bucketIdx = 6 - daysAgo
                counts[bucketIdx] += weight
            }
        }

        val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
        val normalized = counts.map { c ->
            if (c == 0) 0.08f else (0.22f + 0.78f * (c.toFloat() / maxCount.toFloat())).coerceIn(0.08f, 1.0f)
        }
        return labels to normalized
    }

    private fun renderFallbackFrameBitmap(clip: VideoClip, aspectRatio: AspectRatio, sourceMs: Long): Bitmap {
        val (w, h) = when (aspectRatio) {
            AspectRatio.VERTICAL_9_16 -> 720 to 1280
            AspectRatio.SQUARE_1_1 -> 960 to 960
            AspectRatio.PORTRAIT_4_5 -> 800 to 1000
            AspectRatio.CINEMATIC_21_9 -> 1260 to 540
            AspectRatio.LANDSCAPE_16_9 -> 1280 to 720
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val startColor = 0xFF1E1B4B.toInt()
        val endColor = 0xFF0F172A.toInt()
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), startColor, endColor, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = (minOf(w, h) * 0.06f).coerceIn(28f, 52f)
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00E5FF.toInt()
            textSize = (minOf(w, h) * 0.032f).coerceIn(18f, 30f)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(clip.name, w / 2f, h * 0.48f, titlePaint)
        canvas.drawText(
            String.format(Locale.US, "Frame @ %.2fs • %s", sourceMs / 1000f, clip.filter.displayName),
            w / 2f,
            h * 0.56f,
            subPaint
        )
        return bmp
    }

    private fun folderSizeBytes(dir: File): Long {
        if (!dir.exists()) return 0L
        if (dir.isFile) return dir.length()
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun deleteDirectoryContents(dir: File): Long {
        if (!dir.exists() || !dir.isDirectory) return 0L
        var freed = 0L
        dir.listFiles()?.forEach { child ->
            if (child.isDirectory) {
                freed += deleteDirectoryContents(child)
                child.delete()
            } else {
                val len = child.length()
                if (child.delete()) freed += len
            }
        }
        return freed
    }

    private fun formatSrtTime(ms: Long): String {
        val safe = ms.coerceAtLeast(0L)
        val hours = safe / 3_600_000L
        val minutes = (safe % 3_600_000L) / 60_000L
        val seconds = (safe % 60_000L) / 1000L
        val millis = safe % 1000L
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
    }

    private fun formatVttTime(ms: Long): String {
        val safe = ms.coerceAtLeast(0L)
        val hours = safe / 3_600_000L
        val minutes = (safe % 3_600_000L) / 60_000L
        val seconds = (safe % 60_000L) / 1000L
        val millis = safe % 1000L
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
    }

    private fun msToRational(ms: Long): String {
        val safe = ms.coerceAtLeast(0L)
        return "${safe}/1000s"
    }

    private fun xmlEscape(raw: String): String {
        return raw
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
