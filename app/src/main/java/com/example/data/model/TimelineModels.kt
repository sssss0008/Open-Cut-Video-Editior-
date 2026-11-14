package com.example.data.model

import java.util.UUID

enum class CanvasBgMode(val displayName: String) {
    BLUR("Gaussian Blur"),
    BLACK("Solid Black"),
    GRADIENT("Neon Studio Gradient"),
    DARK_VIOLET("Deep Violet")
}

enum class StabilizationMode(val displayName: String, val description: String) {
    SMOOTH_MOTION("Smooth Handheld", "Compensates for walking & handheld micro-jitter"),
    TRIPOD_LOCK("Tripod Horizon Lock", "Locks horizon and removes angular rotation"),
    HIGH_ACTION("High Action Sports", "Aggressive multi-axis trajectory damping for running & action")
}

enum class LutPreset(val displayName: String, val category: String, val description: String) {
    NONE("Original Profile", "Rec.709", "Standard natural dynamic range"),
    TEAL_ORANGE("Hollywood Teal & Orange", "Cinema", "Complementary teal shadows & warm skin tones"),
    BLEACH_BYPASS("Bleach Bypass Silver", "Stylized", "High contrast desaturated gritty action look"),
    CYBERPUNK_NEON("Cyberpunk Neon 2077", "Creative", "Electric cyan shadows & magenta highlight wash"),
    FILM_NOIR("Film Noir Contrast B&W", "Vintage", "Deep rich silver monochrome with soft rolloff"),
    VINTAGE_KODAK("Vintage Kodachrome 64", "Retro", "Nostalgic 1970s warm reds and pastel skies"),
    GOLDEN_HOUR("Golden Hour Glow", "Mood", "Golden California afternoon sun radiance"),
    MATRIX_GREEN("Phosphor Matrix Green", "Sci-Fi", "Classic dark green tint digital dystopia"),
    CINE_LOG_FLAT("Cinematic Log Profile", "Professional", "Ultra-wide dynamic range flat curve for grading")
}

data class ColorGradingSettings(
    val exposure: Float = 0f, // -2.0 EV to +2.0 EV
    val temperature: Float = 0f, // -1.0 (Cool Blue) to +1.0 (Warm Amber)
    val tint: Float = 0f, // -1.0 (Green) to +1.0 (Magenta)
    val vibrance: Float = 0f, // -1.0 to +1.0
    val lut: LutPreset = LutPreset.NONE,
    // Lift / Gamma / Gain Color Wheels
    val liftR: Float = 0f, val liftG: Float = 0f, val liftB: Float = 0f, // Shadows (-0.4 to 0.4)
    val gammaR: Float = 0f, val gammaG: Float = 0f, val gammaB: Float = 0f, // Midtones (-0.4 to 0.4)
    val gainR: Float = 0f, val gainG: Float = 0f, val gainB: Float = 0f, // Highlights (-0.4 to 0.4)
    val liftPedestal: Float = 0f, // Master Shadow level
    val gammaPedestal: Float = 0f, // Master Midtone level
    val gainPedestal: Float = 0f // Master Highlight level
)

enum class EditorWorkspace(val title: String, val subtitle: String, val iconTag: String) {
    EDIT("Edit", "Timeline & Trimmer", "workspace_edit"),
    COLOR("Color", "Wheels & Scopes", "workspace_color"),
    AUDIO("Audio", "Mixer & Ducking", "workspace_audio"),
    MOTION_VFX("Motion", "VFX & Speed", "workspace_motion"),
    AI_MAGIC("AI Magic", "Copilot & Auto", "workspace_ai"),
    DELIVER("Deliver", "Render & Export", "workspace_deliver")
}

enum class ProxyQualityMode(val label: String, val shortBadge: String, val resolutionScale: Float) {
    FULL("Full Resolution (100%)", "FULL", 1.0f),
    HALF_PROXY("Smart Half Proxy (50%)", "1/2 PROXY", 0.5f),
    QUARTER_FAST("Quarter Fast Proxy (25%)", "1/4 FAST", 0.25f)
}

enum class SpeedRampPreset(val displayName: String, val description: String, val curvePoints: List<Float>, val targetSpeed: Float) {
    NORMAL("Constant 1.0x", "Standard real-time playback speed", listOf(1f, 1f, 1f, 1f, 1f), 1.0f),
    MONTAGE_RAMP("Montage Speed Ramp", "Fast entry (2.5x) -> Slow-mo hero moment (0.4x) -> Fast exit (2.5x)", listOf(2.4f, 1.5f, 0.4f, 1.5f, 2.4f), 1.25f),
    BULLET_TIME("Bullet Time 0.25x", "Ultra-slow optical flow matrix hero drop", listOf(1.2f, 0.25f, 0.25f, 0.25f, 1.2f), 0.25f),
    FLASH_IN("Flash In Punch", "Rapid 3x zoom-in ramp decelerating to 1x", listOf(3.0f, 2.0f, 1.2f, 1.0f, 1.0f), 1.5f),
    FLASH_OUT("Whip Flash Out", "Smooth 1x accelerating to 3x whip transition", listOf(1.0f, 1.0f, 1.3f, 2.2f, 3.0f), 1.5f),
    HYPERLAPSE("Hyperlapse 4.0x", "High-energy stabilized time-lapse surge", listOf(2.0f, 4.0f, 4.0f, 4.0f, 2.0f), 4.0f)
}

enum class FrameInterpolationMode(val displayName: String, val description: String) {
    STANDARD("Nearest Frame", "Fastest hardware frame sampling"),
    FRAME_BLENDING("Frame Blending", "Cross-dissolves adjacent frames for smooth motion"),
    OPTICAL_FLOW("Optical Flow", "Motion-vector estimation for fluid slow motion"),
    AI_SLOW_MO("AI Neural Slow-Mo", "Synthesizes intermediate frames using AI motion vectors")
}

enum class VfxParticlePreset(val displayName: String, val badgeEmoji: String, val category: String, val colorHex: Long) {
    NEON_SPARKS("Cyber Neon Sparks", "✨", "Particles", 0xFF00E5FF),
    ANAMORPHIC_FLARE("Anamorphic Lens Flare", "🌟", "Light", 0xFF38BDF8),
    CINEMA_FOG("Atmospheric Smoke & Fog", "🌫️", "Atmosphere", 0xFF94A3B8),
    FILM_BURN("16mm Vintage Film Burn", "🔥", "Film", 0xFFF97316),
    CYBER_RAIN("Matrix Digital Rain", "🌧️", "Sci-Fi", 0xFF10B981),
    GOLDEN_BOKEH("Golden Hour Bokeh Dust", "💫", "Light", 0xFFFBBF24)
}

data class AiSilenceSegment(
    val startMs: Long,
    val endMs: Long,
    val durationMs: Long = endMs - startMs
)

data class AiCaptionItem(
    val id: String = UUID.randomUUID().toString(),
    val startMs: Long,
    val durationMs: Long,
    val text: String,
    val stylePreset: TextStylePreset = TextStylePreset.CLASSIC
)

data class StabilizationSettings(
    val isEnabled: Boolean = false,
    val mode: StabilizationMode = StabilizationMode.SMOOTH_MOTION,
    val smoothness: Float = 0.65f, // 0.0f to 1.0f
    val cropMarginPercent: Float = 0.12f, // 0.0f to 0.30f
    val reduceRollingShutter: Boolean = true
)

enum class ShapeType(val displayName: String, val iconDescription: String) {
    RECTANGLE("Rounded Card", "Standard banner or caption backdrop"),
    CIRCLE("Target Circle", "Focus indicator or circular frame"),
    POINTER_ARROW("Pointer Arrow", "Guides viewer attention to subject"),
    CALLOUT_BANNER("Callout Bubble", "Dialogue or comic speech bubble"),
    SPOTLIGHT_FRAME("Spotlight Frame", "Border highlight frame around subject"),
    STAR_BADGE("Star Badge", "Rating or feature highlight badge")
}

enum class ShapeAnimation(val displayName: String, val description: String) {
    NONE("Static", "No motion"),
    POP_IN("Pop In", "Quick scale overshoot bounce"),
    SLIDE_UP("Slide Up", "Smooth vertical reveal"),
    PULSE_GLOW("Pulse Glow", "Rhythmic opacity & scale glow"),
    GLITCH_SHIFT("Glitch Shift", "Chromatic RGB stutter"),
    FADE_IN("Gentle Fade", "Smooth alpha dissolve")
}

data class ShapeOverlay(
    val id: String = UUID.randomUUID().toString(),
    val shapeType: ShapeType = ShapeType.RECTANGLE,
    val fillColorHex: Long = 0xCC1E293B,
    val strokeColorHex: Long = 0xFF00E5FF,
    val strokeWidthDp: Float = 2.5f,
    val isFilled: Boolean = true,
    val cornerRadiusDp: Float = 12f,
    val rotation: Float = 0f,
    val scale: Float = 1.0f,
    val positionX: Float = 0.5f, // 0..1 fraction of canvas
    val positionY: Float = 0.5f, // 0..1 fraction of canvas
    val timelineStartMs: Long = 0L,
    val durationMs: Long = 3000L,
    val animation: ShapeAnimation = ShapeAnimation.POP_IN
) {
    val timelineEndMs: Long
        get() = timelineStartMs + durationMs
}

enum class VideoEffectType(val displayName: String, val category: String, val description: String) {
    COLOR_SHIFT("Color Shift / Hue", "Color", "Rotate hue angles across the 360° color spectrum"),
    NEGATIVE("Invert / Negative", "Color", "Invert all RGB color channels for classic negative look"),
    PIXELATE("Pixelate / Mosaic", "Stylize", "Censor or retro-pixelate video with block quantization"),
    VIGNETTE("Cinema Vignette", "Lens", "Darken frame edges with soft radial focal falloff"),
    LETTERBOX_BARS("Letterbox Matte (2.35:1)", "Layout", "Cinematic ultra-widescreen anamorphic black bars"),
    WAVE_DISTORT("Liquid Wave", "Distort", "Dynamic sine-wave liquid ripple distortion"),
    BLUR_BOX("Gaussian Soft Blur", "Blur", "Variable radius soft optical defocus"),
    SEPIA_GRAIN("Sepia & Film Grain", "Vintage", "Warm nostalgic monochrome with cinematic grain texture")
}

data class VideoEffectItem(
    val id: String = UUID.randomUUID().toString(),
    val type: VideoEffectType = VideoEffectType.VIGNETTE,
    val intensity: Float = 1.0f, // 0.0f to 1.0f or effect-specific parameter
    val secondaryParam: Float = 0.5f,
    val isEnabled: Boolean = true
)

enum class KeyframeCurve(val displayName: String, val description: String) {
    LINEAR("Linear", "Constant uniform rate of change"),
    BEZIER("Bezier (Ease In/Out)", "Smooth organic acceleration and deceleration"),
    EASE_IN("Ease In", "Accelerates into next keyframe"),
    EASE_OUT("Ease Out", "Decelerates into keyframe"),
    CONSTANT("Constant (Hold)", "Holds value until next keyframe threshold")
}

data class OpenShotKeyframe(
    val id: String = UUID.randomUUID().toString(),
    val timeOffsetMs: Long = 0L, // time relative to clip start
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val alpha: Float = 1.0f,
    val positionX: Float = 0f, // -1.0 to 1.0 (relative canvas displacement)
    val positionY: Float = 0f, // -1.0 to 1.0
    val curve: KeyframeCurve = KeyframeCurve.BEZIER
)

enum class AudioFadeType(val displayName: String) {
    NONE("No Fade"),
    FAST_1S("Fast (1 sec)"),
    SLOW_3S("Slow (3 sec)"),
    CUSTOM("Custom Curve")
}

data class VideoClip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uri: String,
    val sourceDurationMs: Long,
    val trimInMs: Long = 0L,
    val trimOutMs: Long = sourceDurationMs,
    val timelineStartMs: Long = 0L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val filter: VideoFilter = VideoFilter.NONE,
    val filterIntensity: Float = 1.0f,
    val brightness: Float = 0f, // -0.5f to 0.5f
    val contrast: Float = 1.0f, // 0.5f to 1.5f
    val saturation: Float = 1.0f, // 0f to 2.0f
    val transitionIn: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 600L,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val isReversed: Boolean = false,
    val isFreezeFrame: Boolean = false,
    val hasKeyframe: Boolean = false,
    val keyframeScale: Float = 1.0f,
    val canvasBgMode: CanvasBgMode = CanvasBgMode.BLUR,
    val accentColorHex: Long = 0xFF3B82F6,
    // Chroma Key / Green Screen Background Replacement
    val isChromaKeyEnabled: Boolean = false,
    val chromaKeyColorHex: Long = 0xFF00FF00, // Green Screen (#00FF00) or Blue Screen (#0000FF)
    val chromaKeyTolerance: Float = 0.45f, // 0.1f to 0.9f
    // Hardware-Accelerated Video Stabilization
    val stabilization: StabilizationSettings = StabilizationSettings(),
    // OpenShot Video Effects Engine (Hue shift, Negative, Pixelate, Vignette, Letterbox, Wave)
    val effects: List<VideoEffectItem> = emptyList(),
    // OpenShot Keyframe Animation Curve Engine
    val keyframes: List<OpenShotKeyframe> = emptyList(),
    val alpha: Float = 1.0f, // 0.0f to 1.0f
    val positionX: Float = 0f, // -1.0 to 1.0 offset
    val positionY: Float = 0f, // -1.0 to 1.0 offset
    // Canvas Background Processor properties (for vertical video on horizontal canvas)
    val isVertical: Boolean = false,
    val canvasBlurRadiusDp: Float = 25f, // Gaussian blur radius in dp
    val canvasBgBrightness: Float = 0.65f, // Ambient brightness multiplier (0.2 to 1.0)
    // Speech & Voiceover detection for automatic audio ducking
    val hasSpeech: Boolean = false,
    // Professional DaVinci-style Color Grading (Lift/Gamma/Gain, Temp/Tint, LUTs)
    val colorGrading: ColorGradingSettings = ColorGradingSettings()
) {
    // True if this clip is a vertical video (9:16 format)
    val isVerticalVideo: Boolean
        get() = isVertical || name.contains("Vertical", ignoreCase = true) || name.contains("Reel", ignoreCase = true) || name.contains("Short", ignoreCase = true) || name.contains("TikTok", ignoreCase = true) || name.contains("Espresso", ignoreCase = true)

    // Effective playback duration on the timeline based on trim and speed
    val trimmedDurationMs: Long
        get() = ((trimOutMs - trimInMs).coerceAtLeast(100L) / speed).toLong()

    val timelineEndMs: Long
        get() = timelineStartMs + trimmedDurationMs
}

data class AudioTrackItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val uri: String = "",
    val timelineStartMs: Long = 0L,
    val durationMs: Long = 5000L,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val category: AudioCategory = AudioCategory.MUSIC,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val isDuckingEnabled: Boolean = false,
    val accentColorHex: Long = 0xFFF59E0B,
    // Auto Beat Syncing & Waveform Beat Markers
    val beatMarkersMs: List<Long> = emptyList(),
    val isBeatSyncEnabled: Boolean = false,
    // OpenShot Pan & Fade Curve Settings
    val pan: Float = 0f, // -1.0f (Left) to +1.0f (Right)
    val fadeType: AudioFadeType = AudioFadeType.NONE
) {
    val timelineEndMs: Long
        get() = timelineStartMs + durationMs
}

data class TextOverlay(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "Title Text",
    val timelineStartMs: Long = 0L,
    val durationMs: Long = 3000L,
    val positionX: Float = 0.5f, // 0..1 (fraction of canvas width)
    val positionY: Float = 0.8f, // 0..1 (fraction of canvas height)
    val fontSizeSp: Int = 24,
    val textColorHex: Long = 0xFFFFFFFF,
    val bgColorHex: Long = 0xAA000000,
    val preset: TextStylePreset = TextStylePreset.CLASSIC,
    // OpenShot Title & 3D Animated Title properties
    val subtitle: String = "",
    val borderWidthDp: Float = 0f,
    val borderColorHex: Long = 0xFF000000,
    val is3D: Boolean = false,
    val template: String = "CLASSIC"
) {
    val timelineEndMs: Long
        get() = timelineStartMs + durationMs
}

data class OverlaySticker(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val emojiOrIcon: String,
    val timelineStartMs: Long = 0L,
    val durationMs: Long = 2500L,
    val positionX: Float = 0.5f,
    val positionY: Float = 0.3f,
    val scale: Float = 1.0f,
    // Lottie & Vector Animated Overlays
    val isAnimated: Boolean = false,
    val animationType: String = "STATIC", // "SUBSCRIBE_BADGE", "PULSE_HEART", "EQUALIZER_BEAT", "LIVE_STREAM", "NEON_STAR", "WATERMARK_BADGE"
    val loopAnimation: Boolean = true
) {
    val timelineEndMs: Long
        get() = timelineStartMs + durationMs
}

data class TimelineCueMarker(
    val id: String = UUID.randomUUID().toString(),
    val timeMs: Long,
    val label: String,
    val colorHex: Long = 0xFF00E5FF
)

enum class SnapTargetType {
    PLAYHEAD,
    CUT_POINT,
    CUE_MARKER,
    SECOND_GRID,
    START_ZERO
}

data class VisualSnapTarget(
    val timeMs: Long,
    val label: String,
    val type: SnapTargetType,
    val colorHex: Long = 0xFF00E5FF
)

