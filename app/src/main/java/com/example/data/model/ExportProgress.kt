package com.example.data.model

data class ExportProgressData(
    val isExporting: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val currentFrame: Int = 0,
    val totalFrames: Int = 0,
    val currentFps: Float = 0f,
    val stage: ExportStage = ExportStage.IDLE,
    val elapsedSeconds: Long = 0L,
    val estimatedSecondsRemaining: Long = 0L,
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val fps: Int = 30,
    val bitrateMbps: Int = 10,
    val estimatedSizeMb: Float = 0f,
    val outputFileName: String = "",
    val outputFilePath: String = "",
    val errorMessage: String? = null,
    val completed: Boolean = false,
    val isCancelled: Boolean = false
) {
    val progressPercent: Int get() = (progress * 100).toInt().coerceIn(0, 100)

    val formattedEta: String
        get() {
            if (completed) return "Finished"
            if (!isExporting) return "--:--"
            val minutes = estimatedSecondsRemaining / 60
            val seconds = estimatedSecondsRemaining % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedElapsed: String
        get() {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

enum class ExportStage(val stageName: String, val description: String) {
    IDLE("Ready to Export", "Configure settings and start rendering"),
    PREPROCESSING("Preparing Timeline", "Analyzing clip in/out cuts and speed curves"),
    RENDERING_TRANSITIONS("Compositing Transitions", "Rendering cross-fades, wipes, slides & transforms"),
    APPLYING_FILTERS("Color Grading & Effects", "Applying color matrix LUTs and overlays"),
    MIXING_AUDIO("Audio Mixing", "Synchronizing audio tracks, detached audio & voiceovers"),
    ENCODING_CONTAINER("Multiplexing MP4", "Packaging H.264 video stream to phone storage"),
    COMPLETED("Export Complete", "Video rendered and saved directly to your phone storage"),
    FAILED("Export Failed", "An error occurred while encoding or saving video")
}

data class RenderedVideoItem(
    val id: String,
    val title: String,
    val fileName: String,
    val resolution: String,
    val fps: Int,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val createdAt: Long,
    val localUri: String
)
