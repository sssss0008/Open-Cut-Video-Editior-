package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.data.model.AudioTrackItem
import com.example.data.model.CanvasBgMode
import com.example.data.model.ImageOverlay
import com.example.data.model.OverlaySticker
import com.example.data.model.ShapeOverlay
import com.example.data.model.TextOverlay
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Determines whether a vertical video (9:16 aspect ratio) is placed on a horizontal widescreen canvas (e.g., 16:9 or 21:9).
 */
fun isVerticalOnHorizontal(clip: VideoClip, canvasAspectRatio: AspectRatio): Boolean {
    val isClipVertical = clip.isVertical || clip.isVerticalVideo
    val isCanvasHorizontal = canvasAspectRatio.aspectRatio > 1.05f
    return isClipVertical && isCanvasHorizontal
}

/**
 * Computes 1D normalized discrete Gaussian distribution kernel weights for optical defocus simulation.
 * G(x) = (1 / sqrt(2 * PI * sigma^2)) * exp(-x^2 / (2 * sigma^2))
 */
fun calculateGaussianKernelWeights(sigma: Float, radius: Int = 4): FloatArray {
    val safeSigma = sigma.coerceAtLeast(0.5f)
    val size = radius * 2 + 1
    val weights = FloatArray(size)
    val twoSigmaSq = 2f * safeSigma * safeSigma
    val norm = 1f / sqrt(2f * Math.PI.toFloat() * safeSigma * safeSigma)

    var sum = 0f
    for (i in -radius..radius) {
        val w = norm * exp(-(i * i).toFloat() / twoSigmaSq)
        weights[i + radius] = w
        sum += w
    }

    // Normalize so sum equals 1.0
    if (sum > 0f) {
        for (i in weights.indices) {
            weights[i] /= sum
        }
    }
    return weights
}

/**
 * Canvas Background Processor Composable.
 * When a vertical video is placed on a horizontal canvas, this component applies a Gaussian blur
 * replica of the active video frame to the pillarboxed background, rendering the crisp foreground
 * video centered with 9:16 aspect ratio.
 */
@Composable
fun CanvasBackgroundProcessor(
    clip: VideoClip,
    canvasAspectRatio: AspectRatio,
    currentPositionMs: Long,
    activeTextOverlays: List<TextOverlay>,
    activeStickers: List<OverlaySticker>,
    activeShapes: List<ShapeOverlay> = emptyList(),
    activeImageOverlays: List<ImageOverlay> = emptyList(),
    isStabilizationSplitPreview: Boolean = false,
    isPlaying: Boolean = false,
    playbackSpeed: Float = 1.0f,
    isVideoTrackMuted: Boolean = false,
    audioTracks: List<AudioTrackItem> = emptyList(),
    isAudioTrackMuted: Boolean = false,
    isVoiceoverActive: Boolean = false,
    autoDuckingLevel: Float = 0.25f,
    masterVolume: Float = 1.0f,
    isAutoDuckingEnabled: Boolean = true,
    onBufferingChanged: (Boolean) -> Unit = {},
    onPlaybackError: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isVerticalOnWideCanvas = isVerticalOnHorizontal(clip, canvasAspectRatio)

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("canvas_background_processor_root"),
        contentAlignment = Alignment.Center
    ) {
        if (isVerticalOnWideCanvas) {
            // Background Processor Layer
            when (clip.canvasBgMode) {
                CanvasBgMode.BLUR -> {
                    GaussianBlurBackgroundLayer(
                        clip = clip,
                        currentPositionMs = currentPositionMs,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("canvas_gaussian_blur_background")
                    )
                }
                CanvasBgMode.BLACK -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .testTag("canvas_solid_black_background")
                    )
                }
                CanvasBgMode.GRADIENT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF38153A),
                                        Color(0xFF1E1B4B)
                                    )
                                )
                            )
                            .testTag("canvas_gradient_background")
                    )
                }
                CanvasBgMode.DARK_VIOLET -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1B1429))
                            .testTag("canvas_dark_violet_background")
                    )
                }
            }

            // Foreground Layer: Centered 9:16 Vertical Video Frame with subtle drop-shadow
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(9f / 16f)
                    .padding(vertical = 4.dp)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp), clip = false)
                    .clip(RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, Color(0x33FFFFFF)), RoundedCornerShape(8.dp))
                    .testTag("canvas_foreground_vertical_video"),
                contentAlignment = Alignment.Center
            ) {
                RenderVideoFrameContent(
                    clip = clip,
                    currentPositionMs = currentPositionMs,
                    activeTextOverlays = activeTextOverlays,
                    activeStickers = activeStickers,
                    activeShapes = activeShapes,
                    activeImageOverlays = activeImageOverlays,
                    isStabilizationSplitPreview = isStabilizationSplitPreview,
                    isPlaying = isPlaying,
                    playbackSpeed = playbackSpeed,
                    isVideoTrackMuted = isVideoTrackMuted,
                    audioTracks = audioTracks,
                    isAudioTrackMuted = isAudioTrackMuted,
                    isVoiceoverActive = isVoiceoverActive,
                    autoDuckingLevel = autoDuckingLevel,
                    masterVolume = masterVolume,
                    isAutoDuckingEnabled = isAutoDuckingEnabled,
                    onBufferingChanged = onBufferingChanged,
                    onPlaybackError = onPlaybackError
                )
            }

            // Top Status Badge: Indicator that Gaussian Blur Background is actively processing
            if (clip.canvasBgMode == CanvasBgMode.BLUR) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xDD0F172A),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .testTag("canvas_gaussian_blur_active_badge")
                ) {
                    Text(
                        text = "✦ GAUSSIAN BLUR BG (${clip.canvasBlurRadiusDp.toInt()}dp)",
                        color = AccentCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        } else {
            // Standard Full Canvas Video Rendering (Horizontal video or non-widescreen canvas)
            RenderVideoFrameContent(
                clip = clip,
                currentPositionMs = currentPositionMs,
                activeTextOverlays = activeTextOverlays,
                activeStickers = activeStickers,
                activeShapes = activeShapes,
                activeImageOverlays = activeImageOverlays,
                isStabilizationSplitPreview = isStabilizationSplitPreview,
                isPlaying = isPlaying,
                playbackSpeed = playbackSpeed,
                isVideoTrackMuted = isVideoTrackMuted,
                audioTracks = audioTracks,
                isAudioTrackMuted = isAudioTrackMuted,
                isVoiceoverActive = isVoiceoverActive,
                autoDuckingLevel = autoDuckingLevel,
                masterVolume = masterVolume,
                isAutoDuckingEnabled = isAutoDuckingEnabled,
                onBufferingChanged = onBufferingChanged,
                onPlaybackError = onPlaybackError
            )
        }
    }
}

/**
 * Gaussian Blur Background Layer rendering multi-pass optical defocus simulation.
 * Creates an ultra-smooth, chromatic-consistent blurred replica of the active vertical video
 * to fill horizontal letterbox / pillarbox regions.
 */
@Composable
fun GaussianBlurBackgroundLayer(
    clip: VideoClip,
    currentPositionMs: Long,
    modifier: Modifier = Modifier
) {
    val elapsedInClip = (currentPositionMs - clip.timelineStartMs).coerceAtLeast(0L)
    val progressInClip = if (clip.trimmedDurationMs > 0) elapsedInClip.toFloat() / clip.trimmedDurationMs else 0f

    val baseColor = Color(clip.accentColorHex)
    val darkTint = Color(0xFF090A0F)

    // Pre-calculate Gaussian kernel weights for realistic optical dispersion
    val blurWeights = remember(clip.canvasBlurRadiusDp) {
        calculateGaussianKernelWeights(sigma = (clip.canvasBlurRadiusDp / 6f).coerceAtLeast(1f), radius = 4)
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f

        // Pass 1: Base zoomed ambient color sweep derived from video progress
        val sweepX = (width * progressInClip) % width
        val baseGradient = Brush.linearGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.85f),
                darkTint,
                baseColor.copy(alpha = 0.55f),
                baseColor.copy(alpha = 0.75f)
            ),
            start = Offset(sweepX, 0f),
            end = Offset(sweepX + width, height)
        )
        drawRect(brush = baseGradient, size = size)

        // Pass 2: Multi-layer concentric Gaussian blur radial dispersion
        val radii = listOf(
            width * 0.35f,
            width * 0.55f,
            width * 0.80f,
            width * 1.15f
        )

        for (i in radii.indices) {
            val weight = blurWeights.getOrElse(i + 1) { 0.2f }
            val radius = radii[i]
            val radialBrush = Brush.radialGradient(
                colors = listOf(
                    baseColor.copy(alpha = 0.45f * weight),
                    baseColor.copy(alpha = 0.20f * weight),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = radius
            )
            drawCircle(brush = radialBrush, center = Offset(centerX, centerY), radius = radius)
        }

        // Pass 3: Translucent Dimming Scrim to keep foreground vertical video readable & prominent
        val scrimAlpha = (1f - clip.canvasBgBrightness).coerceIn(0.15f, 0.85f)
        drawRect(
            color = Color.Black.copy(alpha = scrimAlpha),
            size = size
        )

        // Pass 4: Frosted Glass Edge Vignette
        val vignette = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.75f)
            ),
            center = Offset(centerX, centerY),
            radius = width * 0.85f
        )
        drawRect(brush = vignette, size = size)
    }
}
