package com.example.data.model

enum class AspectRatio(val label: String, val ratioWidth: Float, val ratioHeight: Float, val iconDescription: String) {
    LANDSCAPE_16_9("16:9 (YouTube)", 16f, 9f, "YouTube & Widescreen"),
    VERTICAL_9_16("9:16 (Shorts/Reels)", 9f, 16f, "TikTok, Reels & Shorts"),
    SQUARE_1_1("1:1 (Instagram)", 1f, 1f, "Square Post"),
    PORTRAIT_4_5("4:5 (Feed)", 4f, 5f, "Social Feed Portrait"),
    CINEMATIC_21_9("21:9 (Cinema)", 21f, 9f, "Ultra-widescreen Movie");

    val aspectRatio: Float get() = ratioWidth / ratioHeight
}

enum class VideoFilter(val displayName: String, val category: String) {
    NONE("Original", "Basic"),
    CINEMATIC("Teal & Orange", "Film"),
    CYBERPUNK("Cyberpunk", "Stylized"),
    FILM_NOIR("Film Noir B&W", "Vintage"),
    GOLDEN_HOUR("Golden Hour", "Mood"),
    EMERALD("Emerald City", "Film"),
    VINTAGE_VHS("Retro VHS", "Vintage"),
    WARM_DRAMA("Warm Drama", "Mood"),
    COLD_NORDIC("Nordic Chill", "Mood"),
    SEPIA("Sepia Classic", "Vintage"),
    HIGH_CONTRAST("Pop Contrast", "Vibrant"),
    PASTEL("Pastel Dream", "Stylized")
}

enum class TransitionType(val displayName: String, val category: String, val description: String) {
    NONE("None", "Basic", "Cut immediately with no transition"),
    CROSSFADE("Crossfade", "Dissolve", "Smooth opacity dissolve between adjacent clips"),
    DIP_TO_BLACK("Dip to Black", "Color Fade", "Fades through solid black darkness"),
    DIP_TO_WHITE("Dip to White", "Color Fade", "High-energy flash through white light"),
    WIPE_LEFT("Wipe Left", "Wipe", "Horizontal linear edge wipe from right to left"),
    WIPE_RIGHT("Wipe Right", "Wipe", "Horizontal linear edge wipe from left to right"),
    WIPE_UP("Wipe Up", "Wipe", "Vertical curtain wipe moving upwards"),
    WIPE_DOWN("Wipe Down", "Wipe", "Vertical curtain wipe moving downwards"),
    SLIDE_LEFT("Slide Left", "Slide", "Incoming clip slides across from right to left"),
    SLIDE_RIGHT("Slide Right", "Slide", "Incoming clip slides across from left to right"),
    SLIDE_UP("Slide Up", "Slide", "Incoming clip slides up from bottom"),
    SLIDE_DOWN("Slide Down", "Slide", "Incoming clip slides down from top"),
    ZOOM_IN("Zoom In", "Zoom", "Dynamic camera forward punch zoom into clip"),
    ZOOM_OUT("Zoom Out", "Zoom", "Cinematic scale out revealing new clip"),
    IRIS_CIRCLE("Iris Circle", "Shape", "Expanding circular aperture lens reveal"),
    GLITCH_FLASH("Glitch Flash", "Stylized", "High-tech chromatic shift RGB burst")
}

enum class TextStylePreset(val styleName: String) {
    CLASSIC("Classic Bold"),
    SUBTITLE("Clean Subtitle"),
    NEON_GLOW("Neon Glow"),
    CINEMA_TITLE("Cinema Headline"),
    RETRO_VHS("Retro VHS"),
    OUTLINE("Comic Outline")
}

enum class AudioCategory {
    MUSIC,
    SOUND_EFFECT,
    VOICEOVER,
    DIALOGUE
}

enum class ExportResolution(val label: String, val width: Int, val height: Int, val bitrateMbps: Int) {
    RES_720P("720p HD", 1280, 720, 5),
    RES_1080P("1080p Full HD", 1920, 1080, 10),
    RES_4K("4K Ultra HD", 3840, 2160, 25)
}

enum class TrackType {
    VIDEO,
    OVERLAY,
    AUDIO,
    TEXT,
    SHAPE
}
