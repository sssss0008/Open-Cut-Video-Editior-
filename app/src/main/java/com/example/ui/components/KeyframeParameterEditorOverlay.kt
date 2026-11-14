package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KeyframeCurve
import com.example.data.model.OpenShotKeyframe
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.PlayheadRed
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Keyframe Parameter Editor Overlay.
 * Allows users to adjust properties like scale, opacity, and rotation over time for selected video clips.
 * Features an interactive time scrubber with keyframe diamonds, previous/next keyframe navigation,
 * real-time parameter sliders with quick preset chips, and easing curve selection.
 */
@Composable
fun KeyframeParameterEditorOverlay(
    clip: VideoClip,
    currentTimelinePositionMs: Long,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSeekTimeline: (Long) -> Unit,
    onAddKeyframe: (OpenShotKeyframe) -> Unit,
    onUpdateKeyframe: (OpenShotKeyframe) -> Unit,
    onRemoveKeyframe: (keyframeId: String) -> Unit,
    onApplyPreset: (presetName: String) -> Unit,
    onClearKeyframes: () -> Unit,
    onOpenCurveGraphSheet: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clipDurationMs = clip.trimmedDurationMs.coerceAtLeast(100L)
    val elapsedInClipMs = (currentTimelinePositionMs - clip.timelineStartMs).coerceIn(0L, clipDurationMs)

    // Find active keyframe within 120ms of current playhead
    var selectedKeyframeId by remember(clip.keyframes, elapsedInClipMs) {
        mutableStateOf(
            clip.keyframes.find { abs(it.timeOffsetMs - elapsedInClipMs) <= 120L }?.id
        )
    }

    val activeKeyframe by remember(clip.keyframes, elapsedInClipMs, selectedKeyframeId) {
        derivedStateOf {
            clip.keyframes.find { it.id == selectedKeyframeId }
                ?: clip.keyframes.find { abs(it.timeOffsetMs - elapsedInClipMs) <= 120L }
        }
    }

    // Current interpolated parameter values at the playhead
    val interpolatedValues = remember(clip.keyframes, elapsedInClipMs, clip.scale, clip.rotation, clip.alpha) {
        interpolateKeyframes(
            keyframes = clip.keyframes,
            elapsedInClipMs = elapsedInClipMs,
            defaultScale = clip.scale,
            defaultRotation = clip.rotation,
            defaultAlpha = clip.alpha,
            defaultPosX = clip.positionX,
            defaultPosY = clip.positionY
        )
    }

    // Editable parameter states reflecting active keyframe or interpolated values
    val currentScale = activeKeyframe?.scale ?: interpolatedValues.scale
    val currentOpacity = activeKeyframe?.alpha ?: interpolatedValues.alpha
    val currentRotation = activeKeyframe?.rotation ?: interpolatedValues.rotation
    val currentCurve = activeKeyframe?.curve ?: KeyframeCurve.BEZIER

    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = StudioSurfaceDark,
        border = BorderStroke(1.dp, Color(0xFF2E2A44)),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("keyframe_parameter_editor_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header Bar: Title, Keyframe count badge, SMPTE time, Play/Pause, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AccentCyan.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Keyframe Overlay",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Keyframe Parameter Editor",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (clip.keyframes.isNotEmpty()) AccentCyan.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (clip.keyframes.isNotEmpty()) AccentCyan else Color.DarkGray)
                            ) {
                                Text(
                                    text = "${clip.keyframes.size} Keyframes",
                                    color = if (clip.keyframes.isNotEmpty()) AccentCyan else Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${clip.name} • ${String.format("%.2f", elapsedInClipMs / 1000f)}s / ${String.format("%.2f", clipDurationMs / 1000f)}s",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mini Play/Pause button
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("keyframe_overlay_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) PlayheadRed else AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close overlay button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("keyframe_overlay_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Keyframe Overlay",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time Scrubber Strip with Keyframe Diamond Markers
            KeyframeScrubberStrip(
                clipDurationMs = clipDurationMs,
                currentClipTimeMs = elapsedInClipMs,
                keyframes = clip.keyframes,
                activeKeyframeId = activeKeyframe?.id,
                onSeek = { targetMs ->
                    onSeekTimeline(clip.timelineStartMs + targetMs)
                },
                onSelectKeyframe = { kf ->
                    selectedKeyframeId = kf.id
                    onSeekTimeline(clip.timelineStartMs + kf.timeOffsetMs)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Keyframe Jump and Add / Remove Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161426), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Jump Previous Keyframe
                IconButton(
                    onClick = {
                        val prev = clip.keyframes.filter { it.timeOffsetMs < elapsedInClipMs - 50L }
                            .maxByOrNull { it.timeOffsetMs }
                        if (prev != null) {
                            selectedKeyframeId = prev.id
                            onSeekTimeline(clip.timelineStartMs + prev.timeOffsetMs)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    enabled = clip.keyframes.any { it.timeOffsetMs < elapsedInClipMs - 50L },
                    modifier = Modifier.size(34.dp).testTag("keyframe_overlay_prev")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Keyframe",
                        tint = if (clip.keyframes.any { it.timeOffsetMs < elapsedInClipMs - 50L }) AccentCyan else Color.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Add or Remove Keyframe at Playhead Button
                if (activeKeyframe != null) {
                    Button(
                        onClick = {
                            val kfId = activeKeyframe!!.id
                            onRemoveKeyframe(kfId)
                            selectedKeyframeId = null
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("keyframe_overlay_delete_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove Keyframe", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val newKf = OpenShotKeyframe(
                                id = UUID.randomUUID().toString(),
                                timeOffsetMs = elapsedInClipMs,
                                scale = currentScale,
                                rotation = currentRotation,
                                alpha = currentOpacity,
                                positionX = clip.positionX,
                                positionY = clip.positionY,
                                curve = currentCurve
                            )
                            onAddKeyframe(newKf)
                            selectedKeyframeId = newKf.id
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("keyframe_overlay_add_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Keyframe at Playhead", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Jump Next Keyframe
                IconButton(
                    onClick = {
                        val next = clip.keyframes.filter { it.timeOffsetMs > elapsedInClipMs + 50L }
                            .minByOrNull { it.timeOffsetMs }
                        if (next != null) {
                            selectedKeyframeId = next.id
                            onSeekTimeline(clip.timelineStartMs + next.timeOffsetMs)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    enabled = clip.keyframes.any { it.timeOffsetMs > elapsedInClipMs + 50L },
                    modifier = Modifier.size(34.dp).testTag("keyframe_overlay_next")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Keyframe",
                        tint = if (clip.keyframes.any { it.timeOffsetMs > elapsedInClipMs + 50L }) AccentCyan else Color.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ========================================================
            // Parameter 1: SCALE ADJUSTMENT OVER TIME
            // ========================================================
            KeyframeParameterSliderRow(
                label = "Scale",
                icon = Icons.Default.ZoomIn,
                valueText = "${(currentScale * 100).roundToInt()}% (${String.format("%.2f", currentScale)}x)",
                value = currentScale,
                valueRange = 0.2f..3.0f,
                accentColor = AccentCyan,
                testTag = "keyframe_scale_slider",
                quickPresets = listOf(0.5f to "0.5x", 1.0f to "1.0x", 1.35f to "1.35x", 1.75f to "1.75x", 2.2f to "2.2x"),
                onValueChange = { newScale ->
                    val kf = activeKeyframe
                    if (kf != null) {
                        onUpdateKeyframe(kf.copy(scale = newScale))
                    } else {
                        val newKf = OpenShotKeyframe(
                            timeOffsetMs = elapsedInClipMs,
                            scale = newScale,
                            rotation = currentRotation,
                            alpha = currentOpacity,
                            curve = currentCurve
                        )
                        onAddKeyframe(newKf)
                        selectedKeyframeId = newKf.id
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ========================================================
            // Parameter 2: OPACITY / ALPHA ADJUSTMENT OVER TIME
            // ========================================================
            KeyframeParameterSliderRow(
                label = "Opacity",
                icon = Icons.Default.Opacity,
                valueText = "${(currentOpacity * 100).roundToInt()}%",
                value = currentOpacity,
                valueRange = 0.0f..1.0f,
                accentColor = AccentPink,
                testTag = "keyframe_opacity_slider",
                quickPresets = listOf(0.0f to "0% (Fade)", 0.25f to "25%", 0.5f to "50%", 0.75f to "75%", 1.0f to "100%"),
                onValueChange = { newAlpha ->
                    val kf = activeKeyframe
                    if (kf != null) {
                        onUpdateKeyframe(kf.copy(alpha = newAlpha))
                    } else {
                        val newKf = OpenShotKeyframe(
                            timeOffsetMs = elapsedInClipMs,
                            scale = currentScale,
                            rotation = currentRotation,
                            alpha = newAlpha,
                            curve = currentCurve
                        )
                        onAddKeyframe(newKf)
                        selectedKeyframeId = newKf.id
                    }
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ========================================================
            // Parameter 3: ROTATION ANGLE ADJUSTMENT OVER TIME
            // ========================================================
            KeyframeParameterSliderRow(
                label = "Rotation",
                icon = Icons.Default.RotateRight,
                valueText = "${currentRotation.roundToInt()}°",
                value = currentRotation,
                valueRange = -180f..180f,
                accentColor = AccentAmber,
                testTag = "keyframe_rotation_slider",
                quickPresets = listOf(-90f to "-90°", 0f to "0°", 45f to "45°", 90f to "90°", 180f to "180°"),
                onValueChange = { newRotation ->
                    val kf = activeKeyframe
                    if (kf != null) {
                        onUpdateKeyframe(kf.copy(rotation = newRotation))
                    } else {
                        val newKf = OpenShotKeyframe(
                            timeOffsetMs = elapsedInClipMs,
                            scale = currentScale,
                            rotation = newRotation,
                            alpha = currentOpacity,
                            curve = currentCurve
                        )
                        onAddKeyframe(newKf)
                        selectedKeyframeId = newKf.id
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Easing Curve Selector (Linear, Bezier, Ease In, Ease Out, Hold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Curve:",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                KeyframeCurve.entries.forEach { curve ->
                    val isSelected = currentCurve == curve
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.25f) else Color(0xFF1E1B33),
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C2840)),
                        modifier = Modifier
                            .clickable {
                                val kf = activeKeyframe
                                if (kf != null) {
                                    onUpdateKeyframe(kf.copy(curve = curve))
                                }
                            }
                            .testTag("keyframe_curve_${curve.name.lowercase()}")
                    ) {
                        Text(
                            text = curve.displayName.split(" ").first(),
                            color = if (isSelected) AccentCyan else Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Quick Preset Animation Row & Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E1B33),
                    border = BorderStroke(1.dp, Color(0xFF2E2B4A)),
                    modifier = Modifier
                        .clickable { onApplyPreset("KEN_BURNS_ZOOM") }
                        .testTag("preset_ken_burns")
                ) {
                    Text(
                        text = "⚡ Zoom In",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E1B33),
                    border = BorderStroke(1.dp, Color(0xFF2E2B4A)),
                    modifier = Modifier
                        .clickable { onApplyPreset("FADE_DISSOLVE") }
                        .testTag("preset_fade_dissolve")
                ) {
                    Text(
                        text = "⚡ Fade In/Out",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E1B33),
                    border = BorderStroke(1.dp, Color(0xFF2E2B4A)),
                    modifier = Modifier
                        .clickable { onApplyPreset("SPIN_360") }
                        .testTag("preset_spin_360")
                ) {
                    Text(
                        text = "⚡ Spin 360",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (clip.keyframes.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = Color(0xFFEF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onClearKeyframes() }
                            .padding(4.dp)
                            .testTag("clear_all_keyframes_btn")
                    )
                }
            }
        }
    }
}

/**
 * Parameter Slider Row with icon, label, live value display, and quick preset chips.
 */
@Composable
private fun KeyframeParameterSliderRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    testTag: String,
    quickPresets: List<Pair<Float, String>>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = valueText,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = value.coerceIn(valueRange.start, valueRange.endInclusive),
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF26203D)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .testTag(testTag)
            )
        }

        // Quick Preset Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickPresets.forEach { (presetVal, presetLabel) ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E1C30),
                    modifier = Modifier.clickable { onValueChange(presetVal) }
                ) {
                    Text(
                        text = presetLabel,
                        color = Color.LightGray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Timeline Mini Scrubber Strip with Keyframe Diamond Markers.
 */
@Composable
fun KeyframeScrubberStrip(
    clipDurationMs: Long,
    currentClipTimeMs: Long,
    keyframes: List<OpenShotKeyframe>,
    activeKeyframeId: String?,
    onSeek: (Long) -> Unit,
    onSelectKeyframe: (OpenShotKeyframe) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0F0E17))
            .border(BorderStroke(1.dp, Color(0xFF231F38)), RoundedCornerShape(6.dp))
            .pointerInput(clipDurationMs) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    val targetMs = (fraction * clipDurationMs).toLong()
                    onSeek(targetMs)
                }
            }
            .pointerInput(clipDurationMs) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    val targetMs = (fraction * clipDurationMs).toLong()
                    onSeek(targetMs)
                }
            }
            .testTag("keyframe_scrubber_strip")
    ) {
        // Track Background with second grid marks
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // Horizontal center line
            drawLine(
                color = Color(0xFF2B2844),
                start = Offset(0f, height / 2f),
                end = Offset(width, height / 2f),
                strokeWidth = 2f
            )

            // Second tick marks
            val totalSeconds = (clipDurationMs / 1000f).coerceAtLeast(1f)
            val secCount = totalSeconds.toInt()
            for (s in 1..secCount) {
                val x = (s / totalSeconds) * width
                drawLine(
                    color = Color(0xFF383458),
                    start = Offset(x, 4f),
                    end = Offset(x, height - 4f),
                    strokeWidth = 1f
                )
            }

            // Keyframe diamond markers (◆)
            keyframes.forEach { kf ->
                val fraction = (kf.timeOffsetMs.toFloat() / clipDurationMs).coerceIn(0f, 1f)
                val kfX = fraction * width
                val kfY = height / 2f
                val isSelected = kf.id == activeKeyframeId
                val diamondRadius = if (isSelected) 6f else 4.5f

                // Draw diamond shape
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(kfX, kfY - diamondRadius)
                    lineTo(kfX + diamondRadius, kfY)
                    lineTo(kfX, kfY + diamondRadius)
                    lineTo(kfX - diamondRadius, kfY)
                    close()
                }

                drawPath(
                    path = path,
                    color = if (isSelected) AccentCyan else Color(0xFFFFB300)
                )
            }

            // Playhead indicator vertical line
            val playheadFraction = (currentClipTimeMs.toFloat() / clipDurationMs).coerceIn(0f, 1f)
            val playheadX = playheadFraction * width
            drawLine(
                color = PlayheadRed,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, height),
                strokeWidth = 2.5f
            )
            drawCircle(
                color = PlayheadRed,
                radius = 4f,
                center = Offset(playheadX, 4f)
            )
        }
    }
}
