package com.example.data.model

data class SampleVideo(
    val name: String,
    val description: String,
    val durationMs: Long,
    val category: String,
    val colorGradientHex: Pair<Long, Long>,
    val tags: List<String>,
    val isVertical: Boolean = false
)

data class SampleAudio(
    val name: String,
    val artist: String,
    val durationMs: Long,
    val category: AudioCategory,
    val tempoBpm: Int,
    val wavePattern: List<Float>
)

data class SampleStickerItem(
    val name: String,
    val emoji: String,
    val category: String
)

object SampleMediaRepository {
    val sampleVideos = listOf(
        SampleVideo(
            name = "Cyberpunk Neon Drive",
            description = "Futuristic city streets drenched in cyan & neon rain reflections",
            durationMs = 8000L,
            category = "Cinematic",
            colorGradientHex = Pair(0xFF0F172A, 0xFF00E5FF),
            tags = listOf("Cyberpunk", "City", "Night", "Neon"),
            isVertical = false
        ),
        SampleVideo(
            name = "Pacific Golden Waves",
            description = "Rolling golden sunset ocean surf splashing across shoreline",
            durationMs = 6000L,
            category = "Nature",
            colorGradientHex = Pair(0xFF1E3A8A, 0xFFF59E0B),
            tags = listOf("Ocean", "Sunset", "Golden Hour", "Calm"),
            isVertical = false
        ),
        SampleVideo(
            name = "Artisan Espresso Pour",
            description = "Slow motion macro pull of rich crema espresso over crystal glass",
            durationMs = 5000L,
            category = "Lifestyle",
            colorGradientHex = Pair(0xFF3B1D11, 0xFFD97706),
            tags = listOf("Coffee", "Macro", "SlowMo", "Aesthetic", "Vertical"),
            isVertical = true
        ),
        SampleVideo(
            name = "Alpine Peak Drone",
            description = "Majestic snow-capped mountain ridge under pristine morning sky",
            durationMs = 7000L,
            category = "Travel",
            colorGradientHex = Pair(0xFF1E293B, 0xFF38BDF8),
            tags = listOf("Mountains", "Drone", "Landscape", "Epic"),
            isVertical = false
        ),
        SampleVideo(
            name = "Tokyo Shinjuku Crossing",
            description = "High energy bustling Shibuya & Shinjuku pedestrian motion blur",
            durationMs = 6000L,
            category = "Urban",
            colorGradientHex = Pair(0xFF4C0519, 0xFFE11D48),
            tags = listOf("Urban", "Motion", "Timelapse", "Crowd", "Reel"),
            isVertical = true
        ),
        SampleVideo(
            name = "Minimalist Gradient Glow",
            description = "Fluid morphing iridescent pastel orbs and ambient light waves",
            durationMs = 10000L,
            category = "Abstract",
            colorGradientHex = Pair(0xFF4C1D95, 0xFFEC4899),
            tags = listOf("Abstract", "Motion Graphics", "Background", "Glow")
        )
    )

    val sampleAudioTracks = listOf(
        SampleAudio(
            name = "Lofi Midnight Study",
            artist = "ChillHop Collective",
            durationMs = 12000L,
            category = AudioCategory.MUSIC,
            tempoBpm = 84,
            wavePattern = listOf(0.3f, 0.5f, 0.8f, 0.6f, 0.9f, 0.4f, 0.7f, 0.85f, 0.5f, 0.65f, 0.95f, 0.7f, 0.4f, 0.8f, 0.6f)
        ),
        SampleAudio(
            name = "Neon Horizon Synthwave",
            artist = "RetroWave Lab",
            durationMs = 15000L,
            category = AudioCategory.MUSIC,
            tempoBpm = 128,
            wavePattern = listOf(0.5f, 0.9f, 0.8f, 0.95f, 0.7f, 0.85f, 0.6f, 0.9f, 0.95f, 0.8f, 0.7f, 0.9f, 0.85f, 0.6f, 0.75f)
        ),
        SampleAudio(
            name = "Acoustic Sunbeam",
            artist = "Indie Strings",
            durationMs = 10000L,
            category = AudioCategory.MUSIC,
            tempoBpm = 100,
            wavePattern = listOf(0.4f, 0.6f, 0.7f, 0.5f, 0.65f, 0.8f, 0.55f, 0.7f, 0.6f, 0.5f, 0.75f, 0.8f, 0.6f, 0.45f, 0.6f)
        ),
        SampleAudio(
            name = "Cinematic Whoosh",
            artist = "OpenCut SFX",
            durationMs = 1500L,
            category = AudioCategory.SOUND_EFFECT,
            tempoBpm = 0,
            wavePattern = listOf(0.1f, 0.2f, 0.5f, 0.95f, 0.8f, 0.4f, 0.1f)
        ),
        SampleAudio(
            name = "Camera Shutter Click",
            artist = "OpenCut SFX",
            durationMs = 800L,
            category = AudioCategory.SOUND_EFFECT,
            tempoBpm = 0,
            wavePattern = listOf(0.2f, 0.95f, 0.8f, 0.3f, 0.05f)
        ),
        SampleAudio(
            name = "Bell Notification Ping",
            artist = "OpenCut SFX",
            durationMs = 1200L,
            category = AudioCategory.SOUND_EFFECT,
            tempoBpm = 0,
            wavePattern = listOf(0.95f, 0.8f, 0.6f, 0.4f, 0.2f, 0.1f)
        ),
        SampleAudio(
            name = "Documentary Voiceover",
            artist = "Studio Narrator",
            durationMs = 8000L,
            category = AudioCategory.VOICEOVER,
            tempoBpm = 0,
            wavePattern = listOf(0.2f, 0.7f, 0.85f, 0.9f, 0.8f, 0.75f, 0.4f, 0.85f, 0.9f, 0.6f)
        ),
        SampleAudio(
            name = "Host & Guest Dialogue",
            artist = "Podcast Studio",
            durationMs = 10000L,
            category = AudioCategory.DIALOGUE,
            tempoBpm = 0,
            wavePattern = listOf(0.3f, 0.8f, 0.95f, 0.7f, 0.85f, 0.9f, 0.4f, 0.7f, 0.85f, 0.6f)
        )
    )

    val sampleStickers = listOf(
        SampleStickerItem("Like Heart", "❤️", "Reaction"),
        SampleStickerItem("Fire Spark", "🔥", "Reaction"),
        SampleStickerItem("Clapper Slate", "🎬", "Video"),
        SampleStickerItem("Star Highlight", "✨", "Effect"),
        SampleStickerItem("Play Button", "▶️", "Media"),
        SampleStickerItem("Audio Wave", "🎵", "Audio"),
        SampleStickerItem("Warning Red", "🚨", "Alert"),
        SampleStickerItem("Check Mark", "✅", "Badge"),
        SampleStickerItem("Lightning Flash", "⚡", "Energy"),
        SampleStickerItem("Camera Lens", "📸", "Photo")
    )
}
