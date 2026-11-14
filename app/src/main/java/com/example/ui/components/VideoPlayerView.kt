package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.data.model.OverlaySticker
import com.example.data.model.KeyframeCurve
import com.example.data.model.OpenShotKeyframe
import com.example.data.model.ShapeAnimation
import com.example.data.model.ShapeOverlay
import com.example.data.model.ShapeType
import com.example.data.model.TextOverlay
import com.example.data.model.TextStylePreset
import com.example.data.model.TransitionType
import com.example.data.model.VideoClip
import com.example.data.model.VideoEffectItem
import com.example.data.model.VideoEffectType
import com.example.data.model.VideoFilter
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PlayheadRed
import com.example.ui.theme.StudioDarkBg
import com.example.viewmodel.VideoEditorViewModel

fun formatSmpteTimecode(ms: Long, fps: Int = 30): String {
    val totalFrames = (ms * fps / 1000).coerceAtLeast(0)
    val frames = (totalFrames % fps)
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, frames)
}

@Composable
fun VideoPlayerView(
    activeClip: VideoClip?,
    aspectRatio: AspectRatio,
    currentPositionMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    activeTextOverlays: List<TextOverlay>,
    activeStickers: List<OverlaySticker>,
    activeShapes: List<ShapeOverlay> = emptyList(),
    isStabilizationSplitPreview: Boolean = false,
    isKeyframeOverlayActive: Boolean = false,
    onToggleKeyframeOverlay: (() -> Unit)? = null,
    isScrubbing: Boolean = false,
    playbackSpeed: Float = 1.0f,
    onSetPlaybackSpeed: ((Float) -> Unit)? = null,
    onJumpToStart: (() -> Unit)? = null,
    onJumpToEnd: (() -> Unit)? = null,
    onTogglePlay: () -> Unit,
    onStepFrame: (Boolean) -> Unit,
    onJumpCut: (Boolean) -> Unit,
    onChangeAspectRatio: () -> Unit,
    onImportVideoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Viewport Container with strict aspect ratio bounds
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val canvasRatio = aspectRatio.aspectRatio
                Box(
                    modifier = Modifier
                        .aspectRatio(canvasRatio, matchHeightConstraintsFirst = maxHeight < maxWidth * (1 / canvasRatio))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090A0F))
                        .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { onTogglePlay() })
                        }
                        .testTag("video_player_canvas"),
                    contentAlignment = Alignment.Center
                ) {
                    if (activeClip != null) {
                        CanvasBackgroundProcessor(
                            clip = activeClip,
                            canvasAspectRatio = aspectRatio,
                            currentPositionMs = currentPositionMs,
                            activeTextOverlays = activeTextOverlays,
                            activeStickers = activeStickers,
                            activeShapes = activeShapes,
                            isStabilizationSplitPreview = isStabilizationSplitPreview
                        )
                    } else {
                        // Empty timeline placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = "Empty Timeline",
                                tint = AccentCyan,
                                modifier = Modifier.size(42.dp)
                            )
                            Text(
                                text = "Timeline is empty",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "Import a video from your device to start editing",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            if (onImportVideoClick != null) {
                                Button(
                                    onClick = onImportVideoClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("empty_timeline_import_button")
                                ) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import Video from Device", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Top Zone Frosted-Glass Overlay: Clean Timecode Pill (Top-Left) & Aspect Ratio (Top-Right)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top-Left: Clean Semi-Transparent Timecode Pill (00:00:12:04 SMPTE frame accurate)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xCC090A0F),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.7f)),
                            modifier = Modifier.testTag("timecode_pill")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            when {
                                                isScrubbing -> AccentCyan
                                                isPlaying -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = formatSmpteTimecode(currentPositionMs),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                if (isScrubbing) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SCRUB",
                                        color = AccentCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        // Top-Right: Status Badges (Chroma Key & Aspect Ratio)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (activeClip?.isChromaKeyEnabled == true) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xCC003822),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "KEY ON",
                                        color = Color(0xFF10B981),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (activeClip?.stabilization?.isEnabled == true) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xCC002B36),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                                    modifier = Modifier.testTag("stabilization_active_badge")
                                ) {
                                    Text(
                                        text = "STAB ON",
                                        color = AccentCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (activeClip != null && onToggleKeyframeOverlay != null) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isKeyframeOverlayActive) AccentCyan.copy(alpha = 0.25f) else Color(0xCC1F2937),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isKeyframeOverlayActive) AccentCyan else Color(0xFF374151).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { onToggleKeyframeOverlay() }
                                        .testTag("toggle_keyframe_overlay_pill")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = "Keyframes",
                                            tint = if (isKeyframeOverlayActive) AccentCyan else Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Keyframes",
                                            color = if (isKeyframeOverlayActive) AccentCyan else Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xCC1F2937),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { onChangeAspectRatio() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Aspect Ratio",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = " ${aspectRatio.label.split(" ").first()}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Floating Center Play/Pause Indicator when paused
                    if (!isPlaying && activeClip != null) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .border(1.5.dp, AccentCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // Control Transport Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onJumpCut(false) },
                modifier = Modifier.size(40.dp).testTag("jump_prev_cut")
            ) {
                Icon(Icons.Default.SkipPrevious, "Previous Cut", tint = Color.LightGray)
            }

            IconButton(
                onClick = { onStepFrame(false) },
                modifier = Modifier.size(40.dp).testTag("step_frame_back")
            ) {
                Icon(Icons.Default.FastRewind, "-1 Frame", tint = Color.LightGray)
            }

            // Primary Play / Pause Button
            Surface(
                shape = CircleShape,
                color = if (isPlaying) PlayheadRed else AccentCyan,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onTogglePlay() }
                    .testTag("play_pause_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            IconButton(
                onClick = { onStepFrame(true) },
                modifier = Modifier.size(40.dp).testTag("step_frame_forward")
            ) {
                Icon(Icons.Default.FastForward, "+1 Frame", tint = Color.LightGray)
            }

            IconButton(
                onClick = { onJumpCut(true) },
                modifier = Modifier.size(40.dp).testTag("jump_next_cut")
            ) {
                Icon(Icons.Default.SkipNext, "Next Cut", tint = Color.LightGray)
            }

            // Quick Playback Speed Toggle (0.5x, 1.0x, 1.5x, 2.0x)
            if (onSetPlaybackSpeed != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (playbackSpeed != 1.0f) AccentCyan.copy(alpha = 0.2f) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (playbackSpeed != 1.0f) AccentCyan else Color(0xFF334155)),
                    modifier = Modifier
                        .clickable {
                            val next = when (playbackSpeed) {
                                0.5f -> 1.0f
                                1.0f -> 1.5f
                                1.5f -> 2.0f
                                else -> 0.5f
                            }
                            onSetPlaybackSpeed(next)
                        }
                        .testTag("player_speed_toggle")
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        color = if (playbackSpeed != 1.0f) AccentCyan else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RenderVideoFrame(
    clip: VideoClip,
    currentPositionMs: Long,
    activeTextOverlays: List<TextOverlay>,
    activeStickers: List<OverlaySticker>,
    activeShapes: List<ShapeOverlay> = emptyList(),
    isStabilizationSplitPreview: Boolean = false,
    canvasAspectRatio: AspectRatio = AspectRatio.VERTICAL_9_16
) {
    CanvasBackgroundProcessor(
        clip = clip,
        canvasAspectRatio = canvasAspectRatio,
        currentPositionMs = currentPositionMs,
        activeTextOverlays = activeTextOverlays,
        activeStickers = activeStickers,
        activeShapes = activeShapes,
        isStabilizationSplitPreview = isStabilizationSplitPreview
    )
}

@Composable
fun RenderVideoFrameContent(
    clip: VideoClip,
    currentPositionMs: Long,
    activeTextOverlays: List<TextOverlay>,
    activeStickers: List<OverlaySticker>,
    activeShapes: List<ShapeOverlay> = emptyList(),
    isStabilizationSplitPreview: Boolean = false
) {
    // Calculate clip internal progress
    val elapsedInClip = (currentPositionMs - clip.timelineStartMs).coerceAtLeast(0L)
    val progressInClip = if (clip.trimmedDurationMs > 0) elapsedInClip.toFloat() / clip.trimmedDurationMs else 0f

    // Calculate Transition overlay if within transition window
    val isInTransition = clip.transitionIn != TransitionType.NONE && elapsedInClip < clip.transitionDurationMs
    val transitionProgress = if (isInTransition) elapsedInClip.toFloat() / clip.transitionDurationMs else 1f

    // Evaluate OpenShot Keyframe Animation Curves
    val kf = interpolateKeyframes(
        keyframes = clip.keyframes,
        elapsedInClipMs = elapsedInClip,
        defaultScale = clip.scale,
        defaultRotation = clip.rotation,
        defaultAlpha = clip.alpha,
        defaultPosX = clip.positionX,
        defaultPosY = clip.positionY
    )

    // Hardware Stabilization: Auto-crop margin scale to eliminate counter-rotation border artifacts
    val stabCropScale = if (clip.stabilization.isEnabled) (1.0f + clip.stabilization.cropMarginPercent) else 1.0f
    val effectiveScale = kf.scale * stabCropScale

    // Generate dynamic color matrix for selected filter, color grading, and OpenShot video effects
    val colorMatrix = createFilterColorMatrix(
        filter = clip.filter,
        brightness = clip.brightness,
        contrast = clip.contrast,
        saturation = clip.saturation,
        effects = clip.effects,
        colorGrading = clip.colorGrading
    )

    val context = LocalContext.current
    val frameBitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = clip.uri,
        key2 = (elapsedInClip / 120L)
    ) {
        val uriStr = clip.uri
        if (!uriStr.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                value = com.example.util.DeviceMediaImporter.extractFrameBitmap(context, uriStr, elapsedInClip)
            }
        } else {
            value = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = (kf.positionX * 36).dp, y = (kf.positionY * 36).dp)
            .rotate(kf.rotation)
            .scale(
                scaleX = (if (clip.flipHorizontal) -effectiveScale else effectiveScale),
                scaleY = effectiveScale
            )
            .alpha(kf.alpha),
        contentAlignment = Alignment.Center
    ) {
        if (frameBitmap != null) {
            Image(
                bitmap = frameBitmap!!.asImageBitmap(),
                contentDescription = clip.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(colorMatrix)
            )
        }

        // Overlay canvas rendering video effects, vignettes, letterboxing, and fallback gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (frameBitmap == null) {
                // Base gradient from clip colors when frame bitmap is not available
                val startColor = Color(clip.accentColorHex)
                val endColor = Color(clip.accentColorHex).copy(alpha = 0.4f)

                // Dynamic wave sweep based on progress
                val sweepX = width * progressInClip
                val brush = Brush.linearGradient(
                    colors = listOf(
                        startColor,
                        Color(0xFF0F172A),
                        endColor,
                        startColor
                    ),
                    start = Offset(sweepX, 0f),
                    end = Offset(sweepX + width, height)
                )

                drawRect(
                    brush = brush,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix)
                )
            }

            // OpenShot Wave Distort / Ripple Effect
            val waveEffect = clip.effects.find { it.isEnabled && it.type == VideoEffectType.WAVE_DISTORT }
            if (waveEffect != null) {
                val waveCount = 7
                val amp = 14f * waveEffect.intensity
                for (w in 0..waveCount) {
                    val waveY = (height / waveCount) * w
                    val offsetPhase = kotlin.math.sin(progressInClip * 6.28f + w).toFloat() * amp
                    drawLine(
                        color = Color(0x3300E5FF),
                        start = Offset(0f, waveY + offsetPhase),
                        end = Offset(width, waveY - offsetPhase),
                        strokeWidth = 2f
                    )
                }
            }

            // Subtle film grain / scanline lines for realistic editor preview
            val lineSpacing = 16f
            var y = 0f
            while (y < height) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += lineSpacing
            }

            // OpenShot Vignette Effect (or default lens vignette)
            val vignetteEffect = clip.effects.find { it.isEnabled && it.type == VideoEffectType.VIGNETTE }
            val vigAlpha = if (vignetteEffect != null) (0.85f * vignetteEffect.intensity).coerceIn(0.1f, 0.95f) else 0.6f
            val vigRadius = if (vignetteEffect != null) maxOf(width, height) * (0.8f - 0.25f * vignetteEffect.intensity).coerceAtLeast(0.3f) else maxOf(width, height) * 0.7f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = vigAlpha)),
                    center = Offset(width / 2, height / 2),
                    radius = vigRadius
                )
            )

            // OpenShot Letterbox Bars (Anamorphic 2.35:1 Matte)
            val letterboxEffect = clip.effects.find { it.isEnabled && it.type == VideoEffectType.LETTERBOX_BARS }
            if (letterboxEffect != null) {
                val barHeight = height * (0.13f * letterboxEffect.intensity).coerceIn(0.02f, 0.35f)
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, 0f),
                    size = Size(width, barHeight)
                )
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, height - barHeight),
                    size = Size(width, barHeight)
                )
            }

            // Clip Name watermarked on frame
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = 4f,
                center = Offset(24f, 24f)
            )
        }

        // Split-Screen Stabilization Comparison Overlay
        if (isStabilizationSplitPreview && clip.stabilization.isEnabled) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midX = size.width / 2f
                drawLine(
                    color = AccentCyan,
                    start = Offset(midX, 0f),
                    end = Offset(midX, size.height),
                    strokeWidth = 2.5f
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 36.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "RAW (UNSTABILIZED)", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "STABILIZED (${clip.stabilization.mode.displayName})", color = AccentCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Clip title watermarked in corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${clip.name} • ${clip.filter.displayName}${if (clip.stabilization.isEnabled) " • Stabilized" else ""}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Active Transition Effect Graphic
        if (isInTransition) {
            when (clip.transitionIn) {
                TransitionType.DIP_TO_BLACK -> {
                    val alpha = (1f - transitionProgress).coerceIn(0f, 1f)
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = alpha)))
                }
                TransitionType.DIP_TO_WHITE -> {
                    val alpha = (1f - transitionProgress).coerceIn(0f, 1f)
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = alpha)))
                }
                TransitionType.CROSSFADE -> {
                    val alpha = (1f - transitionProgress) * 0.75f
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1435).copy(alpha = alpha)))
                }
                TransitionType.WIPE_LEFT -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val wipeW = size.width * (1f - transitionProgress)
                        drawRect(
                            color = Color.Black.copy(alpha = 0.9f),
                            topLeft = Offset(size.width - wipeW, 0f),
                            size = androidx.compose.ui.geometry.Size(wipeW, size.height)
                        )
                        drawLine(
                            color = Color(0xFF00E5FF),
                            start = Offset(size.width - wipeW, 0f),
                            end = Offset(size.width - wipeW, size.height),
                            strokeWidth = 3f
                        )
                    }
                }
                TransitionType.WIPE_RIGHT -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val wipeW = size.width * (1f - transitionProgress)
                        drawRect(
                            color = Color.Black.copy(alpha = 0.9f),
                            topLeft = Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(wipeW, size.height)
                        )
                        drawLine(
                            color = Color(0xFF00E5FF),
                            start = Offset(wipeW, 0f),
                            end = Offset(wipeW, size.height),
                            strokeWidth = 3f
                        )
                    }
                }
                TransitionType.WIPE_UP -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val wipeH = size.height * (1f - transitionProgress)
                        drawRect(
                            color = Color.Black.copy(alpha = 0.9f),
                            topLeft = Offset(0f, size.height - wipeH),
                            size = androidx.compose.ui.geometry.Size(size.width, wipeH)
                        )
                        drawLine(
                            color = Color(0xFFFF4081),
                            start = Offset(0f, size.height - wipeH),
                            end = Offset(size.width, size.height - wipeH),
                            strokeWidth = 3f
                        )
                    }
                }
                TransitionType.WIPE_DOWN -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val wipeH = size.height * (1f - transitionProgress)
                        drawRect(
                            color = Color.Black.copy(alpha = 0.9f),
                            topLeft = Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(size.width, wipeH)
                        )
                        drawLine(
                            color = Color(0xFFFF4081),
                            start = Offset(0f, wipeH),
                            end = Offset(size.width, wipeH),
                            strokeWidth = 3f
                        )
                    }
                }
                TransitionType.SLIDE_LEFT -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val slideX = size.width * (1f - transitionProgress)
                        drawRect(
                            color = Color(0xFF130E26).copy(alpha = 0.85f),
                            topLeft = Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(slideX, size.height)
                        )
                    }
                }
                TransitionType.SLIDE_RIGHT -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val slideX = size.width * (1f - transitionProgress)
                        drawRect(
                            color = Color(0xFF130E26).copy(alpha = 0.85f),
                            topLeft = Offset(size.width - slideX, 0f),
                            size = androidx.compose.ui.geometry.Size(slideX, size.height)
                        )
                    }
                }
                TransitionType.SLIDE_UP -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val slideY = size.height * (1f - transitionProgress)
                        drawRect(
                            color = Color(0xFF130E26).copy(alpha = 0.85f),
                            topLeft = Offset(0f, 0f),
                            size = androidx.compose.ui.geometry.Size(size.width, slideY)
                        )
                    }
                }
                TransitionType.SLIDE_DOWN -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val slideY = size.height * (1f - transitionProgress)
                        drawRect(
                            color = Color(0xFF130E26).copy(alpha = 0.85f),
                            topLeft = Offset(0f, size.height - slideY),
                            size = androidx.compose.ui.geometry.Size(size.width, slideY)
                        )
                    }
                }
                TransitionType.ZOOM_IN -> {
                    val scale = 0.4f + 0.6f * transitionProgress
                    Box(modifier = Modifier.fillMaxSize().scale(scale))
                }
                TransitionType.ZOOM_OUT -> {
                    val scale = 1.5f - 0.5f * transitionProgress
                    Box(modifier = Modifier.fillMaxSize().scale(scale))
                }
                TransitionType.IRIS_CIRCLE -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val maxR = Math.hypot(size.width.toDouble() / 2, size.height.toDouble() / 2).toFloat()
                        val currentR = maxR * (1f - transitionProgress)
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.85f),
                            radius = currentR,
                            center = Offset(size.width / 2, size.height / 2),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                        )
                    }
                }
                TransitionType.GLITCH_FLASH -> {
                    if (transitionProgress < 0.6f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(color = Color.Red.copy(alpha = 0.25f), topLeft = Offset(0f, size.height * 0.25f), size = androidx.compose.ui.geometry.Size(size.width, 16f))
                            drawRect(color = Color.Cyan.copy(alpha = 0.3f), topLeft = Offset(0f, size.height * 0.65f), size = androidx.compose.ui.geometry.Size(size.width, 24f))
                        }
                    }
                }
                TransitionType.NONE -> {}
            }
        }

        // Render Active Shape Overlays
        activeShapes.forEach { shape ->
            RenderShapeOverlay(shape, currentPositionMs)
        }

        // Render Active Text Overlays
        activeTextOverlays.forEach { textOverlay ->
            RenderTextOverlay(textOverlay)
        }

        // Render Active Stickers
        activeStickers.forEach { sticker ->
            RenderSticker(sticker)
        }
    }
}

@Composable
fun RenderShapeOverlay(shape: ShapeOverlay, currentPositionMs: Long) {
    val elapsed = (currentPositionMs - shape.timelineStartMs).coerceAtLeast(0L)
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    // Calculate dynamic animation values based on keyframe motion preset
    val animScale: Float
    val animOffsetY: Float
    val animAlpha: Float

    when (shape.animation) {
        ShapeAnimation.POP_IN -> {
            val progress = (elapsed / 300f).coerceIn(0f, 1f)
            // Overshoot bounce easing
            animScale = if (progress < 0.7f) {
                (progress / 0.7f) * 1.15f
            } else {
                1.15f - ((progress - 0.7f) / 0.3f) * 0.15f
            } * shape.scale
            animOffsetY = 0f
            animAlpha = progress.coerceIn(0f, 1f)
        }
        ShapeAnimation.SLIDE_UP -> {
            val progress = (elapsed / 300f).coerceIn(0f, 1f)
            animScale = shape.scale
            animOffsetY = (1f - progress) * 50f
            animAlpha = progress.coerceIn(0f, 1f)
        }
        ShapeAnimation.PULSE_GLOW -> {
            val cycle = (elapsed % 1200L) / 1200f
            val pulse = 1f + 0.08f * kotlin.math.sin(cycle * 2 * Math.PI).toFloat()
            animScale = shape.scale * pulse
            animOffsetY = 0f
            animAlpha = (0.85f + 0.15f * kotlin.math.cos(cycle * 2 * Math.PI).toFloat()).coerceIn(0f, 1f)
        }
        ShapeAnimation.GLITCH_SHIFT -> {
            val cycle = (elapsed % 800L)
            val jitter = if (cycle in 200L..280L) ((cycle % 10) - 5).toFloat() else 0f
            animScale = shape.scale
            animOffsetY = jitter
            animAlpha = 1.0f
        }
        ShapeAnimation.FADE_IN -> {
            val progress = (elapsed / 400f).coerceIn(0f, 1f)
            animScale = shape.scale
            animOffsetY = 0f
            animAlpha = progress
        }
        ShapeAnimation.NONE -> {
            animScale = shape.scale
            animOffsetY = 0f
            animAlpha = 1.0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = when {
            shape.positionY < 0.35f && shape.positionX < 0.35f -> Alignment.TopStart
            shape.positionY < 0.35f && shape.positionX > 0.65f -> Alignment.TopEnd
            shape.positionY < 0.35f -> Alignment.TopCenter
            shape.positionY > 0.65f && shape.positionX < 0.35f -> Alignment.BottomStart
            shape.positionY > 0.65f && shape.positionX > 0.65f -> Alignment.BottomEnd
            shape.positionY > 0.65f -> Alignment.BottomCenter
            shape.positionX < 0.35f -> Alignment.CenterStart
            shape.positionX > 0.65f -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Canvas(
            modifier = Modifier
                .size(100.dp)
                .offset(y = animOffsetY.dp)
                .rotate(shape.rotation)
                .scale(animScale)
        ) {
            val fillC = Color(shape.fillColorHex)
            val strokeC = Color(shape.strokeColorHex)
            drawShapeGeometry(
                shapeType = shape.shapeType,
                isFilled = shape.isFilled,
                fillColor = fillC.copy(alpha = (fillC.alpha * animAlpha).coerceIn(0f, 1f)),
                strokeColor = strokeC.copy(alpha = (strokeC.alpha * animAlpha).coerceIn(0f, 1f)),
                strokeWidthPx = shape.strokeWidthDp * density,
                cornerRadiusPx = shape.cornerRadiusDp * density
            )
        }
    }
}

@Composable
fun RenderTextOverlay(textOverlay: TextOverlay) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = when {
            textOverlay.positionY < 0.35f -> Alignment.TopCenter
            textOverlay.positionY > 0.65f -> Alignment.BottomCenter
            else -> Alignment.Center
        }
    ) {
        if (textOverlay.is3D || textOverlay.template != null) {
            // OpenShot 3D Title Kinetic Animation & Lighting
            val infiniteTransition = rememberInfiniteTransition(label = "title_3d_anim")
            val kineticGlow by infiniteTransition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "kinetic_pulse"
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(textOverlay.bgColorHex),
                shadowElevation = 14.dp,
                border = androidx.compose.foundation.BorderStroke(
                    textOverlay.borderWidthDp.coerceAtLeast(1.5f).dp,
                    Color(textOverlay.borderColorHex)
                ),
                modifier = Modifier
                    .scale(kineticGlow)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = textOverlay.text,
                        color = Color(textOverlay.textColorHex),
                        fontSize = textOverlay.fontSizeSp.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = if (textOverlay.is3D) 2.5.sp else 1.sp,
                        textAlign = TextAlign.Center
                    )
                    if (textOverlay.subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = textOverlay.subtitle,
                            color = Color(textOverlay.borderColorHex).copy(alpha = 0.9f),
                            fontSize = (textOverlay.fontSizeSp * 0.55f).coerceAtLeast(10f).sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val borderModifier = if (textOverlay.borderWidthDp > 0f) {
                Modifier.border(
                    textOverlay.borderWidthDp.dp,
                    Color(textOverlay.borderColorHex),
                    RoundedCornerShape(6.dp)
                )
            } else {
                Modifier
            }

            when (textOverlay.preset) {
                TextStylePreset.NEON_GLOW -> {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF9C27B0).copy(alpha = 0.5f), Color(0xFF00E5FF).copy(alpha = 0.5f))
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.5.dp, AccentCyan, RoundedCornerShape(8.dp))
                            .then(borderModifier)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = textOverlay.text,
                                color = Color.White,
                                fontSize = textOverlay.fontSizeSp.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                            if (textOverlay.subtitle.isNotBlank()) {
                                Text(
                                    text = textOverlay.subtitle,
                                    color = AccentCyan,
                                    fontSize = (textOverlay.fontSizeSp * 0.6f).sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                TextStylePreset.CINEMA_TITLE -> {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .then(borderModifier)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = textOverlay.text.uppercase(),
                                color = Color(0xFFFFD700),
                                fontSize = (textOverlay.fontSizeSp + 4).sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                textAlign = TextAlign.Center
                            )
                            if (textOverlay.subtitle.isNotBlank()) {
                                Text(
                                    text = textOverlay.subtitle,
                                    color = Color.LightGray,
                                    fontSize = (textOverlay.fontSizeSp * 0.6f).sp,
                                    letterSpacing = 1.5.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                TextStylePreset.SUBTITLE -> {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                            .then(borderModifier)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = textOverlay.text,
                                color = Color.White,
                                fontSize = textOverlay.fontSizeSp.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            if (textOverlay.subtitle.isNotBlank()) {
                                Text(
                                    text = textOverlay.subtitle,
                                    color = Color(0xFFD1D5DB),
                                    fontSize = (textOverlay.fontSizeSp * 0.7f).sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                TextStylePreset.RETRO_VHS -> {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E0826).copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0xFFFF4081), RoundedCornerShape(2.dp))
                            .then(borderModifier)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = textOverlay.text,
                                color = Color(0xFF00E5FF),
                                fontSize = textOverlay.fontSizeSp.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (textOverlay.subtitle.isNotBlank()) {
                                Text(
                                    text = textOverlay.subtitle,
                                    color = Color(0xFFFF4081),
                                    fontSize = (textOverlay.fontSizeSp * 0.6f).sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .background(Color(textOverlay.bgColorHex), RoundedCornerShape(6.dp))
                            .then(borderModifier)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = textOverlay.text,
                                color = Color(textOverlay.textColorHex),
                                fontSize = textOverlay.fontSizeSp.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (textOverlay.subtitle.isNotBlank()) {
                                Text(
                                    text = textOverlay.subtitle,
                                    color = Color(textOverlay.textColorHex).copy(alpha = 0.8f),
                                    fontSize = (textOverlay.fontSizeSp * 0.6f).sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderSticker(sticker: OverlaySticker) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = when {
            sticker.positionX > 0.6f && sticker.positionY < 0.4f -> Alignment.TopEnd
            sticker.positionX < 0.4f && sticker.positionY < 0.4f -> Alignment.TopStart
            sticker.positionX > 0.6f && sticker.positionY > 0.6f -> Alignment.BottomEnd
            sticker.positionX < 0.4f && sticker.positionY > 0.6f -> Alignment.BottomStart
            else -> Alignment.Center
        }
    ) {
        if (sticker.isAnimated) {
            val infiniteTransition = rememberInfiniteTransition(label = "lottie_vector_anim")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(650, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )
            val rotationDeg by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            val bellRotate by infiniteTransition.animateFloat(
                initialValue = -14f,
                targetValue = 14f,
                animationSpec = infiniteRepeatable(
                    animation = tween(220, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bell"
            )

            when (sticker.animationType) {
                "SUBSCRIBE_BADGE" -> {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xDDCC0000),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .scale(pulseScale * sticker.scale)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔔",
                                fontSize = 16.sp,
                                modifier = Modifier.rotate(bellRotate)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUBSCRIBE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                "LIVE_STREAM" -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xDD0B0F19),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0055)),
                        modifier = Modifier.scale(sticker.scale)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .scale(pulseScale)
                                    .background(Color(0xFFFF0055), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
                "EQUALIZER_BEAT" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xDD1F2937),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.scale(sticker.scale)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Canvas(modifier = Modifier.size(24.dp, 16.dp)) {
                                val barW = 4f
                                val h1 = size.height * (0.3f + 0.6f * ((pulseScale - 0.94f) / 0.12f))
                                val h2 = size.height * (0.9f - 0.6f * ((pulseScale - 0.94f) / 0.12f))
                                val h3 = size.height * (0.5f + 0.4f * ((pulseScale - 0.94f) / 0.12f))
                                drawRect(Color(0xFFF59E0B), Offset(0f, size.height - h1), androidx.compose.ui.geometry.Size(barW, h1))
                                drawRect(Color(0xFFF59E0B), Offset(7f, size.height - h2), androidx.compose.ui.geometry.Size(barW, h2))
                                drawRect(Color(0xFFF59E0B), Offset(14f, size.height - h3), androidx.compose.ui.geometry.Size(barW, h3))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BEAT SYNC",
                                color = Color(0xFFF59E0B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                "NEON_STAR" -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .scale(pulseScale * sticker.scale)
                            .rotate(rotationDeg)
                    ) {
                        Text(text = "⭐", fontSize = 32.sp)
                    }
                }
                "WATERMARK_BADGE" -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC06B6D4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier.scale(sticker.scale)
                    ) {
                        Text(
                            text = "⚡ 4K PRO",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                else -> {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size((48 * sticker.scale).dp)
                            .scale(pulseScale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = sticker.emojiOrIcon,
                                fontSize = (26 * sticker.scale).sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                shadowElevation = 6.dp,
                modifier = Modifier.size((48 * sticker.scale).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sticker.emojiOrIcon,
                        fontSize = (26 * sticker.scale).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun createFilterColorMatrix(
    filter: VideoFilter,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    effects: List<VideoEffectItem> = emptyList(),
    colorGrading: com.example.data.model.ColorGradingSettings = com.example.data.model.ColorGradingSettings()
): ColorMatrix {
    val cm = ColorMatrix()

    // Base Saturation & Vibrance
    val effectiveSat = (saturation + colorGrading.vibrance * 0.5f).coerceIn(0f, 3f)
    cm.setToSaturation(effectiveSat)

    // Apply 3D LUT Profiles
    when (colorGrading.lut) {
        com.example.data.model.LutPreset.TEAL_ORANGE -> {
            val lutMatrix = floatArrayOf(
                1.2f, 0f, 0f, 0f, 15f,
                0f, 1.0f, 0.1f, 0f, 5f,
                0f, 0.15f, 1.35f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.timesAssign(ColorMatrix(lutMatrix))
        }
        com.example.data.model.LutPreset.BLEACH_BYPASS -> {
            val lutMatrix = floatArrayOf(
                1.4f, 0f, 0f, 0f, -10f,
                0f, 1.4f, 0f, 0f, -10f,
                0f, 0f, 1.4f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.timesAssign(ColorMatrix(lutMatrix))
        }
        com.example.data.model.LutPreset.CYBERPUNK_NEON -> {
            val lutMatrix = floatArrayOf(
                1.35f, 0f, 0.25f, 0f, 25f,
                0f, 0.85f, 0f, 0f, -10f,
                0.2f, 0f, 1.45f, 0f, 35f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.timesAssign(ColorMatrix(lutMatrix))
        }
        com.example.data.model.LutPreset.FILM_NOIR -> {
            cm.setToSaturation(0f)
            val lutMatrix = floatArrayOf(
                1.35f, 0f, 0f, 0f, -15f,
                0f, 1.35f, 0f, 0f, -15f,
                0f, 0f, 1.35f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.timesAssign(ColorMatrix(lutMatrix))
        }
        com.example.data.model.LutPreset.GOLDEN_HOUR -> {
            val lutMatrix = floatArrayOf(
                1.25f, 0f, 0f, 0f, 30f,
                0f, 1.15f, 0f, 0f, 15f,
                0f, 0f, 0.85f, 0f, -20f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.timesAssign(ColorMatrix(lutMatrix))
        }
        com.example.data.model.LutPreset.MATRIX_GREEN -> {
            val lutMatrix = floatArrayOf(
                0.85f, 0f, 0f, 0f, -15f,
                0.15f, 1.35f, 0.15f, 0f, 30f,
                0f, 0f, 0.85f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.timesAssign(ColorMatrix(lutMatrix))
        }
        else -> {}
    }

    // Color Grading Primary Balance (Exposure, Temp, Tint, Lift/Gain)
    val expMul = Math.pow(2.0, colorGrading.exposure.toDouble()).toFloat()
    val tempR = colorGrading.temperature * 28f
    val tempB = -colorGrading.temperature * 28f
    val tintG = -colorGrading.tint * 22f
    val tintM = colorGrading.tint * 15f

    val shadowR = (colorGrading.liftR + colorGrading.liftPedestal) * 45f
    val shadowG = colorGrading.liftPedestal * 45f
    val shadowB = (colorGrading.liftB + colorGrading.liftPedestal) * 45f

    val highR = (colorGrading.gainR + colorGrading.gainPedestal) * 0.4f
    val highG = colorGrading.gainPedestal * 0.4f
    val highB = (colorGrading.gainB + colorGrading.gainPedestal) * 0.4f

    val gradeMatrix = floatArrayOf(
        expMul + highR, 0f, 0f, 0f, tempR + tintM + shadowR,
        0f, expMul + highG, 0f, 0f, tintG + shadowG,
        0f, 0f, expMul + highB, 0f, tempB + tintM + shadowB,
        0f, 0f, 0f, 1f, 0f
    )
    cm.timesAssign(ColorMatrix(gradeMatrix))

    // Filter color variations
    when (filter) {
        VideoFilter.CINEMATIC -> {
            // Teal shadows, orange highlights
            val array = floatArrayOf(
                contrast * 1.15f, 0f, 0f, 0f, brightness * 255 + 20f,
                0f, contrast * 0.95f, 0f, 0f, brightness * 255 + 5f,
                0f, 0f, contrast * 1.25f, 0f, brightness * 255 + 30f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.set(ColorMatrix(array))
        }
        VideoFilter.CYBERPUNK -> {
            // High neon violet / cyan
            val array = floatArrayOf(
                contrast * 1.3f, 0f, 0.2f, 0f, brightness * 255 + 35f,
                0f, contrast * 0.8f, 0f, 0f, brightness * 255 - 10f,
                0.2f, 0f, contrast * 1.4f, 0f, brightness * 255 + 45f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.set(ColorMatrix(array))
        }
        VideoFilter.FILM_NOIR -> {
            cm.setToSaturation(0f)
            val array = floatArrayOf(
                contrast * 1.4f, 0f, 0f, 0f, brightness * 255 - 20f,
                0f, contrast * 1.4f, 0f, 0f, brightness * 255 - 20f,
                0f, 0f, contrast * 1.4f, 0f, brightness * 255 - 20f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.timesAssign(ColorMatrix(array))
        }
        VideoFilter.GOLDEN_HOUR -> {
            val array = floatArrayOf(
                contrast * 1.25f, 0f, 0f, 0f, brightness * 255 + 35f,
                0f, contrast * 1.1f, 0f, 0f, brightness * 255 + 15f,
                0f, 0f, contrast * 0.8f, 0f, brightness * 255 - 25f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.set(ColorMatrix(array))
        }
        VideoFilter.EMERALD -> {
            val array = floatArrayOf(
                contrast * 0.85f, 0f, 0f, 0f, brightness * 255 - 10f,
                0f, contrast * 1.3f, 0f, 0f, brightness * 255 + 25f,
                0f, 0f, contrast * 1.0f, 0f, brightness * 255 + 10f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.set(ColorMatrix(array))
        }
        VideoFilter.VINTAGE_VHS -> {
            val array = floatArrayOf(
                contrast * 1.1f, 0.1f, 0f, 0f, brightness * 255 + 20f,
                0f, contrast * 1.0f, 0.1f, 0f, brightness * 255 + 10f,
                0.1f, 0f, contrast * 0.9f, 0f, brightness * 255 + 40f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.set(ColorMatrix(array))
        }
        VideoFilter.SEPIA -> {
            val array = floatArrayOf(
                0.393f * contrast, 0.769f * contrast, 0.189f * contrast, 0f, brightness * 255 + 30f,
                0.349f * contrast, 0.686f * contrast, 0.168f * contrast, 0f, brightness * 255 + 15f,
                0.272f * contrast, 0.534f * contrast, 0.131f * contrast, 0f, brightness * 255 - 10f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.set(ColorMatrix(array))
        }
        VideoFilter.HIGH_CONTRAST -> {
            val array = floatArrayOf(
                contrast * 1.5f, 0f, 0f, 0f, brightness * 255 - 10f,
                0f, contrast * 1.5f, 0f, 0f, brightness * 255 - 10f,
                0f, 0f, contrast * 1.5f, 0f, brightness * 255 - 10f,
                0f, 0f, 0f, 1f, 0f
            )
            cm.set(ColorMatrix(array))
        }
        else -> {
            if (brightness != 0f || contrast != 1f) {
                val array = floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness * 255,
                    0f, contrast, 0f, 0f, brightness * 255,
                    0f, 0f, contrast, 0f, brightness * 255,
                    0f, 0f, 0f, 1f, 0f
                )
                cm.timesAssign(ColorMatrix(array))
            }
        }
    }

    // Apply OpenShot Effects if present
    for (effect in effects) {
        if (!effect.isEnabled) continue
        when (effect.type) {
            VideoEffectType.NEGATIVE -> {
                val inv = effect.intensity
                val array = floatArrayOf(
                    1f - 2f * inv, 0f, 0f, 0f, 255f * inv,
                    0f, 1f - 2f * inv, 0f, 0f, 255f * inv,
                    0f, 0f, 1f - 2f * inv, 0f, 255f * inv,
                    0f, 0f, 0f, 1f, 0f
                )
                cm.timesAssign(ColorMatrix(array))
            }
            VideoEffectType.COLOR_SHIFT -> {
                val shift = effect.intensity
                val array = floatArrayOf(
                    1f - shift * 0.4f, shift * 0.5f, 0f, 0f, 15f * shift,
                    0f, 1f - shift * 0.2f, shift * 0.6f, 0f, 20f * shift,
                    shift * 0.6f, 0f, 1f - shift * 0.3f, 0f, 25f * shift,
                    0f, 0f, 0f, 1f, 0f
                )
                cm.timesAssign(ColorMatrix(array))
            }
            VideoEffectType.SEPIA_GRAIN -> {
                val sepiaAmount = effect.intensity
                val array = floatArrayOf(
                    0.393f * sepiaAmount + (1f - sepiaAmount), 0.769f * sepiaAmount, 0.189f * sepiaAmount, 0f, 30f * sepiaAmount,
                    0.349f * sepiaAmount, 0.686f * sepiaAmount + (1f - sepiaAmount), 0.168f * sepiaAmount, 0f, 20f * sepiaAmount,
                    0.272f * sepiaAmount, 0.534f * sepiaAmount, 0.131f * sepiaAmount + (1f - sepiaAmount), 0f, -5f * sepiaAmount,
                    0f, 0f, 0f, 1f, 0f
                )
                cm.timesAssign(ColorMatrix(array))
            }
            else -> {}
        }
    }

    return cm
}

data class KeyframeResult(
    val scale: Float,
    val rotation: Float,
    val alpha: Float,
    val positionX: Float,
    val positionY: Float
)

fun interpolateKeyframes(
    keyframes: List<OpenShotKeyframe>,
    elapsedInClipMs: Long,
    defaultScale: Float,
    defaultRotation: Float,
    defaultAlpha: Float,
    defaultPosX: Float,
    defaultPosY: Float
): KeyframeResult {
    if (keyframes.isEmpty()) {
        return KeyframeResult(defaultScale, defaultRotation, defaultAlpha, defaultPosX, defaultPosY)
    }

    val sorted = keyframes.sortedBy { it.timeOffsetMs }
    if (elapsedInClipMs <= sorted.first().timeOffsetMs) {
        val first = sorted.first()
        return KeyframeResult(first.scale, first.rotation, first.alpha, first.positionX, first.positionY)
    }
    if (elapsedInClipMs >= sorted.last().timeOffsetMs) {
        val last = sorted.last()
        return KeyframeResult(last.scale, last.rotation, last.alpha, last.positionX, last.positionY)
    }

    // Find surrounding keyframes
    for (i in 0 until sorted.size - 1) {
        val k1 = sorted[i]
        val k2 = sorted[i + 1]
        if (elapsedInClipMs in k1.timeOffsetMs..k2.timeOffsetMs) {
            val span = (k2.timeOffsetMs - k1.timeOffsetMs).coerceAtLeast(1L).toFloat()
            val rawT = ((elapsedInClipMs - k1.timeOffsetMs) / span).coerceIn(0f, 1f)
            val t = when (k1.curve) {
                KeyframeCurve.LINEAR -> rawT
                KeyframeCurve.CONSTANT -> 0f
                KeyframeCurve.BEZIER -> rawT * rawT * (3f - 2f * rawT) // Cubic ease in-out
                KeyframeCurve.EASE_IN -> rawT * rawT // Quadratic ease in
                KeyframeCurve.EASE_OUT -> rawT * (2f - rawT) // Quadratic ease out
            }
            return KeyframeResult(
                scale = k1.scale + (k2.scale - k1.scale) * t,
                rotation = k1.rotation + (k2.rotation - k1.rotation) * t,
                alpha = (k1.alpha + (k2.alpha - k1.alpha) * t).coerceIn(0f, 1f),
                positionX = k1.positionX + (k2.positionX - k1.positionX) * t,
                positionY = k1.positionY + (k2.positionY - k1.positionY) * t
            )
        }
    }

    return KeyframeResult(defaultScale, defaultRotation, defaultAlpha, defaultPosX, defaultPosY)
}
