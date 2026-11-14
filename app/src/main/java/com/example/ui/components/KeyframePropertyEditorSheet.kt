package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KeyframeCurve
import com.example.data.model.OpenShotKeyframe
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.VideoEditorViewModel
import java.util.UUID
import kotlin.math.roundToInt

enum class KeyframePropertyType(val label: String, val iconDescription: String) {
    SCALE("Scale", "Scale Zoom"),
    ROTATION("Rotation", "Rotation Angle"),
    OPACITY("Opacity", "Alpha Transparency"),
    POSITION("Position", "2D Coordinate Offset")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyframePropertyEditorSheet(
    clip: VideoClip,
    currentTimelinePositionMs: Long,
    onDismiss: () -> Unit,
    onAddKeyframe: (OpenShotKeyframe) -> Unit,
    onUpdateKeyframe: (OpenShotKeyframe) -> Unit,
    onRemoveKeyframe: (keyframeId: String) -> Unit,
    onApplyPreset: (presetName: String) -> Unit,
    onClearKeyframes: () -> Unit,
    onSeekTimeline: (Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    val clipDurationMs = clip.trimmedDurationMs.coerceAtLeast(100L)
    val currentClipTimeMs = (currentTimelinePositionMs - clip.timelineStartMs).coerceIn(0L, clipDurationMs)

    var selectedProperty by remember { mutableStateOf(KeyframePropertyType.SCALE) }
    var selectedKeyframeId by remember { mutableStateOf<String?>(null) }

    // Find nearest keyframe within 100ms of current playhead or selected id
    val activeKeyframe by remember(clip.keyframes, currentClipTimeMs, selectedKeyframeId) {
        derivedStateOf {
            clip.keyframes.find { it.id == selectedKeyframeId }
                ?: clip.keyframes.find { kotlin.math.abs(it.timeOffsetMs - currentClipTimeMs) <= 120L }
        }
    }

    // Default or active values
    val currentScale = activeKeyframe?.scale ?: clip.scale
    val currentRotation = activeKeyframe?.rotation ?: clip.rotation
    val currentAlpha = activeKeyframe?.alpha ?: clip.alpha
    val currentPosX = activeKeyframe?.positionX ?: clip.positionX
    val currentPosY = activeKeyframe?.positionY ?: clip.positionY
    val currentCurve = activeKeyframe?.curve ?: KeyframeCurve.BEZIER

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AccentCyan.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Keyframe Editor",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Keyframe Property Curve Editor",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${clip.name} • ${clip.keyframes.size} Keyframes • ${String.format("%.2f", currentClipTimeMs / 1000f)}s / ${String.format("%.2f", clipDurationMs / 1000f)}s",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Property Selection Tabs (Scale, Rotation, Opacity, Position)
            TabRow(
                selectedTabIndex = selectedProperty.ordinal,
                containerColor = Color(0xFF161426),
                contentColor = AccentCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedProperty.ordinal]),
                        color = AccentCyan
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                KeyframePropertyType.entries.forEach { prop ->
                    val isSelected = selectedProperty == prop
                    Tab(
                        selected = isSelected,
                        onClick = {
                            selectedProperty = prop
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        text = {
                            Text(
                                text = prop.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AccentCyan else Color.Gray
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (prop) {
                                    KeyframePropertyType.SCALE -> Icons.Default.ZoomIn
                                    KeyframePropertyType.ROTATION -> Icons.Default.RotateRight
                                    KeyframePropertyType.OPACITY -> Icons.Default.Opacity
                                    KeyframePropertyType.POSITION -> Icons.Default.OpenWith
                                },
                                contentDescription = prop.label,
                                tint = if (isSelected) AccentCyan else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("keyframe_tab_${prop.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Keyframe Timeline Curve Canvas
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F0E17),
                border = BorderStroke(1.dp, Color(0xFF231F38)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("keyframe_curve_canvas")
            ) {
                KeyframeCurveCanvas(
                    clipDurationMs = clipDurationMs,
                    currentClipTimeMs = currentClipTimeMs,
                    keyframes = clip.keyframes,
                    propertyType = selectedProperty,
                    selectedKeyframeId = activeKeyframe?.id,
                    onSeek = { targetMs ->
                        val globalMs = clip.timelineStartMs + targetMs
                        onSeekTimeline(globalMs)
                    },
                    onSelectKeyframe = { kf ->
                        selectedKeyframeId = kf.id
                        val globalMs = clip.timelineStartMs + kf.timeOffsetMs
                        onSeekTimeline(globalMs)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation and Add/Remove Keyframe Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141224), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Jump to previous keyframe
                IconButton(
                    onClick = {
                        val prev = clip.keyframes.filter { it.timeOffsetMs < currentClipTimeMs - 50L }
                            .maxByOrNull { it.timeOffsetMs }
                        if (prev != null) {
                            selectedKeyframeId = prev.id
                            onSeekTimeline(clip.timelineStartMs + prev.timeOffsetMs)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    enabled = clip.keyframes.any { it.timeOffsetMs < currentClipTimeMs - 50L },
                    modifier = Modifier.testTag("keyframe_prev_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Keyframe",
                        tint = if (clip.keyframes.any { it.timeOffsetMs < currentClipTimeMs - 50L }) AccentCyan else Color.DarkGray
                    )
                }

                // Central Keyframe Button (+ Add Keyframe or Remove Keyframe at Playhead)
                if (activeKeyframe != null) {
                    Button(
                        onClick = {
                            val kfId = activeKeyframe!!.id
                            onRemoveKeyframe(kfId)
                            selectedKeyframeId = null
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("keyframe_delete_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, "Delete Keyframe", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove Keyframe", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val newKf = OpenShotKeyframe(
                                id = UUID.randomUUID().toString(),
                                timeOffsetMs = currentClipTimeMs,
                                scale = currentScale,
                                rotation = currentRotation,
                                alpha = currentAlpha,
                                positionX = currentPosX,
                                positionY = currentPosY,
                                curve = currentCurve
                            )
                            onAddKeyframe(newKf)
                            selectedKeyframeId = newKf.id
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("keyframe_add_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, "Add Keyframe", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Keyframe at Playhead", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Jump to next keyframe
                IconButton(
                    onClick = {
                        val next = clip.keyframes.filter { it.timeOffsetMs > currentClipTimeMs + 50L }
                            .minByOrNull { it.timeOffsetMs }
                        if (next != null) {
                            selectedKeyframeId = next.id
                            onSeekTimeline(clip.timelineStartMs + next.timeOffsetMs)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    enabled = clip.keyframes.any { it.timeOffsetMs > currentClipTimeMs + 50L },
                    modifier = Modifier.testTag("keyframe_next_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Keyframe",
                        tint = if (clip.keyframes.any { it.timeOffsetMs > currentClipTimeMs + 50L }) AccentCyan else Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Property Value Controls (Scale, Rotation, Opacity, Position)
            when (selectedProperty) {
                KeyframePropertyType.SCALE -> {
                    ScalePropertyEditor(
                        scale = currentScale,
                        onScaleChange = { newScale ->
                            if (activeKeyframe != null) {
                                onUpdateKeyframe(activeKeyframe!!.copy(scale = newScale))
                            } else {
                                val newKf = OpenShotKeyframe(
                                    timeOffsetMs = currentClipTimeMs,
                                    scale = newScale,
                                    rotation = currentRotation,
                                    alpha = currentAlpha,
                                    positionX = currentPosX,
                                    positionY = currentPosY,
                                    curve = currentCurve
                                )
                                onAddKeyframe(newKf)
                                selectedKeyframeId = newKf.id
                            }
                        }
                    )
                }
                KeyframePropertyType.ROTATION -> {
                    RotationPropertyEditor(
                        rotation = currentRotation,
                        onRotationChange = { newRot ->
                            if (activeKeyframe != null) {
                                onUpdateKeyframe(activeKeyframe!!.copy(rotation = newRot))
                            } else {
                                val newKf = OpenShotKeyframe(
                                    timeOffsetMs = currentClipTimeMs,
                                    scale = currentScale,
                                    rotation = newRot,
                                    alpha = currentAlpha,
                                    positionX = currentPosX,
                                    positionY = currentPosY,
                                    curve = currentCurve
                                )
                                onAddKeyframe(newKf)
                                selectedKeyframeId = newKf.id
                            }
                        }
                    )
                }
                KeyframePropertyType.OPACITY -> {
                    OpacityPropertyEditor(
                        alpha = currentAlpha,
                        onAlphaChange = { newAlpha ->
                            if (activeKeyframe != null) {
                                onUpdateKeyframe(activeKeyframe!!.copy(alpha = newAlpha))
                            } else {
                                val newKf = OpenShotKeyframe(
                                    timeOffsetMs = currentClipTimeMs,
                                    scale = currentScale,
                                    rotation = currentRotation,
                                    alpha = newAlpha,
                                    positionX = currentPosX,
                                    positionY = currentPosY,
                                    curve = currentCurve
                                )
                                onAddKeyframe(newKf)
                                selectedKeyframeId = newKf.id
                            }
                        }
                    )
                }
                KeyframePropertyType.POSITION -> {
                    PositionPropertyEditor(
                        positionX = currentPosX,
                        positionY = currentPosY,
                        onPositionChange = { newX, newY ->
                            if (activeKeyframe != null) {
                                onUpdateKeyframe(activeKeyframe!!.copy(positionX = newX, positionY = newY))
                            } else {
                                val newKf = OpenShotKeyframe(
                                    timeOffsetMs = currentClipTimeMs,
                                    scale = currentScale,
                                    rotation = currentRotation,
                                    alpha = currentAlpha,
                                    positionX = newX,
                                    positionY = newY,
                                    curve = currentCurve
                                )
                                onAddKeyframe(newKf)
                                selectedKeyframeId = newKf.id
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Curve Interpolation Type Selector (Bezier, Linear, Constant Hold)
            Text(
                text = "Keyframe Interpolation Curve",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeyframeCurve.entries.forEach { curve ->
                    val isSelected = currentCurve == curve
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF26223B)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (activeKeyframe != null) {
                                    onUpdateKeyframe(activeKeyframe!!.copy(curve = curve))
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .testTag("keyframe_curve_${curve.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = curve.displayName.substringBefore(" "),
                                color = if (isSelected) AccentCyan else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (curve) {
                                    KeyframeCurve.BEZIER -> "Smooth Ease"
                                    KeyframeCurve.EASE_IN -> "Ease In"
                                    KeyframeCurve.EASE_OUT -> "Ease Out"
                                    KeyframeCurve.LINEAR -> "Constant"
                                    KeyframeCurve.CONSTANT -> "Hold Step"
                                },
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Motion Animation Presets Bar
            Text(
                text = "Motion Animation Presets",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimationPresetChip("Ken Burns Zoom", "KEN_BURNS_ZOOM") { onApplyPreset("KEN_BURNS_ZOOM") }
                AnimationPresetChip("Pan Left → Right", "PAN_LEFT_RIGHT") { onApplyPreset("PAN_LEFT_RIGHT") }
                AnimationPresetChip("Fade In & Out", "FADE_DISSOLVE") { onApplyPreset("FADE_DISSOLVE") }
                AnimationPresetChip("Spin 360°", "SPIN_360") { onApplyPreset("SPIN_360") }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions: Clear All Keyframes & Done
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearKeyframes,
                    border = BorderStroke(1.dp, Color(0xFF4B232D)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("keyframe_clear_all_btn")
                ) {
                    Text("Clear All Keyframes", color = Color(0xFFF87171), fontSize = 11.sp)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("keyframe_done_btn")
                ) {
                    Text("Apply & Close", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun KeyframeCurveCanvas(
    clipDurationMs: Long,
    currentClipTimeMs: Long,
    keyframes: List<OpenShotKeyframe>,
    propertyType: KeyframePropertyType,
    selectedKeyframeId: String?,
    onSeek: (Long) -> Unit,
    onSelectKeyframe: (OpenShotKeyframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(clipDurationMs) {
                detectTapGestures { offset ->
                    val clickedTimeMs = ((offset.x / size.width) * clipDurationMs).toLong().coerceIn(0L, clipDurationMs)
                    val clickedKf = keyframes.find {
                        val kfX = (it.timeOffsetMs.toFloat() / clipDurationMs) * size.width
                        kotlin.math.abs(kfX - offset.x) < 30f
                    }
                    if (clickedKf != null) {
                        onSelectKeyframe(clickedKf)
                    } else {
                        onSeek(clickedTimeMs)
                    }
                }
            }
            .pointerInput(clipDurationMs) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val draggedTimeMs = ((change.position.x / size.width) * clipDurationMs).toLong().coerceIn(0L, clipDurationMs)
                    onSeek(draggedTimeMs)
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // Background grid lines (quarters)
        for (i in 1..3) {
            val y = height * (i / 4f)
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Second tick marks along bottom
        val totalSec = (clipDurationMs / 1000f)
        val secStep = if (totalSec > 10f) 2 else 1
        for (sec in 0..(totalSec.toInt() + 1) step secStep) {
            val x = (sec * 1000L / clipDurationMs.toFloat()) * width
            if (x <= width) {
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(x, height - 12f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }
        }

        // Draw property curve if keyframes exist
        val sortedKeyframes = keyframes.sortedBy { it.timeOffsetMs }
        if (sortedKeyframes.isNotEmpty()) {
            val points = sortedKeyframes.map { kf ->
                val x = (kf.timeOffsetMs.toFloat() / clipDurationMs) * width
                val normalizedY = when (propertyType) {
                    KeyframePropertyType.SCALE -> ((kf.scale - 0.1f) / 3.9f).coerceIn(0f, 1f)
                    KeyframePropertyType.ROTATION -> ((kf.rotation + 360f) / 720f).coerceIn(0f, 1f)
                    KeyframePropertyType.OPACITY -> kf.alpha.coerceIn(0f, 1f)
                    KeyframePropertyType.POSITION -> ((kf.positionX + 1.0f) / 2.0f).coerceIn(0f, 1f)
                }
                // Invert Y so 1.0 is at top
                val y = height * (1f - normalizedY * 0.75f - 0.12f)
                Offset(x, y) to kf
            }

            // Draw smooth spline curve
            val path = Path()
            val fillPath = Path()

            path.moveTo(points.first().first.x, points.first().first.y)
            fillPath.moveTo(points.first().first.x, height)
            fillPath.lineTo(points.first().first.x, points.first().first.y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i].first
                val p2 = points[i + 1].first
                val kf1 = points[i].second

                when (kf1.curve) {
                    KeyframeCurve.LINEAR -> {
                        path.lineTo(p2.x, p2.y)
                        fillPath.lineTo(p2.x, p2.y)
                    }
                    KeyframeCurve.CONSTANT -> {
                        path.lineTo(p2.x, p1.y)
                        path.lineTo(p2.x, p2.y)
                        fillPath.lineTo(p2.x, p1.y)
                        fillPath.lineTo(p2.x, p2.y)
                    }
                    KeyframeCurve.BEZIER -> {
                        val controlX1 = p1.x + (p2.x - p1.x) * 0.45f
                        val controlX2 = p1.x + (p2.x - p1.x) * 0.55f
                        path.cubicTo(controlX1, p1.y, controlX2, p2.y, p2.x, p2.y)
                        fillPath.cubicTo(controlX1, p1.y, controlX2, p2.y, p2.x, p2.y)
                    }
                    KeyframeCurve.EASE_IN -> {
                        val controlX1 = p1.x + (p2.x - p1.x) * 0.7f
                        path.cubicTo(controlX1, p1.y, p2.x, p2.y, p2.x, p2.y)
                        fillPath.cubicTo(controlX1, p1.y, p2.x, p2.y, p2.x, p2.y)
                    }
                    KeyframeCurve.EASE_OUT -> {
                        val controlX2 = p1.x + (p2.x - p1.x) * 0.3f
                        path.cubicTo(p1.x, p1.y, controlX2, p2.y, p2.x, p2.y)
                        fillPath.cubicTo(p1.x, p1.y, controlX2, p2.y, p2.x, p2.y)
                    }
                }
            }

            fillPath.lineTo(points.last().first.x, height)
            fillPath.close()

            // Fill gradient under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AccentCyan.copy(alpha = 0.35f),
                        AccentCyan.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw stroke of curve
            drawPath(
                path = path,
                color = AccentCyan,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )

            // Draw Keyframe Diamonds
            points.forEach { (pt, kf) ->
                val isSelected = kf.id == selectedKeyframeId
                val diamondRadius = if (isSelected) 8f else 6f

                // Halo ring for selected keyframe
                if (isSelected) {
                    drawCircle(
                        color = AccentPink,
                        radius = 12f,
                        center = pt,
                        style = Stroke(width = 2f)
                    )
                }

                // Rotated square (diamond)
                val diamondPath = Path().apply {
                    moveTo(pt.x, pt.y - diamondRadius)
                    lineTo(pt.x + diamondRadius, pt.y)
                    lineTo(pt.x, pt.y + diamondRadius)
                    lineTo(pt.x - diamondRadius, pt.y)
                    close()
                }
                drawPath(diamondPath, color = if (isSelected) AccentPink else Color.White)
                drawPath(diamondPath, color = AccentCyan, style = Stroke(width = 1.5f))
            }
        }

        // Draw Playhead scrubber line
        val playheadX = (currentClipTimeMs.toFloat() / clipDurationMs) * width
        drawLine(
            color = AccentCyan,
            start = Offset(playheadX, 0f),
            end = Offset(playheadX, height),
            strokeWidth = 2f
        )
        drawCircle(
            color = AccentCyan,
            radius = 5f,
            center = Offset(playheadX, 6f)
        )
    }
}

@Composable
fun ScalePropertyEditor(
    scale: Float,
    onScaleChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141224), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Scale Zoom Multiplier", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "${String.format("%.2f", scale)}x",
                color = AccentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = scale,
            onValueChange = onScaleChange,
            valueRange = 0.1f..4.0f,
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan
            ),
            modifier = Modifier.testTag("keyframe_scale_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f).forEach { s ->
                QuickValueChip("${s}x", isSelected = kotlin.math.abs(scale - s) < 0.05f) {
                    onScaleChange(s)
                }
            }
        }
    }
}

@Composable
fun RotationPropertyEditor(
    rotation: Float,
    onRotationChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141224), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rotation Angle", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "${rotation.roundToInt()}°",
                color = AccentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = rotation,
            onValueChange = onRotationChange,
            valueRange = -360f..360f,
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan
            ),
            modifier = Modifier.testTag("keyframe_rotation_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(-180f, -90f, 0f, 90f, 180f, 360f).forEach { r ->
                QuickValueChip("${r.roundToInt()}°", isSelected = kotlin.math.abs(rotation - r) < 2f) {
                    onRotationChange(r)
                }
            }
        }
    }
}

@Composable
fun OpacityPropertyEditor(
    alpha: Float,
    onAlphaChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141224), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Alpha Opacity", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "${(alpha * 100).roundToInt()}%",
                color = AccentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = alpha,
            onValueChange = onAlphaChange,
            valueRange = 0.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan
            ),
            modifier = Modifier.testTag("keyframe_alpha_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f).forEach { a ->
                val pct = "${(a * 100).toInt()}%"
                QuickValueChip(pct, isSelected = kotlin.math.abs(alpha - a) < 0.03f) {
                    onAlphaChange(a)
                }
            }
        }
    }
}

@Composable
fun PositionPropertyEditor(
    positionX: Float,
    positionY: Float,
    onPositionChange: (Float, Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141224), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("2D Canvas Displacement Offset", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "X:${String.format("%.2f", positionX)} Y:${String.format("%.2f", positionY)}",
                color = AccentCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // X coordinate slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("X", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(18.dp))
            Slider(
                value = positionX,
                onValueChange = { onPositionChange(it, positionY) },
                valueRange = -1.0f..1.0f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                modifier = Modifier.weight(1f).testTag("keyframe_pos_x_slider")
            )
        }

        // Y coordinate slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Y", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.width(18.dp))
            Slider(
                value = positionY,
                onValueChange = { onPositionChange(positionX, it) },
                valueRange = -1.0f..1.0f,
                colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = AccentPink),
                modifier = Modifier.weight(1f).testTag("keyframe_pos_y_slider")
            )
        }

        // Quick Center Button
        OutlinedButton(
            onClick = { onPositionChange(0f, 0f) },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.End)
                .height(34.dp)
        ) {
            Icon(Icons.Default.CenterFocusStrong, "Center", tint = AccentCyan, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Center (0,0)", color = Color.White, fontSize = 11.sp)
        }
    }
}

@Composable
fun QuickValueChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) AccentCyan else Color(0xFF1E1A34),
        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF332E52)),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AnimationPresetChip(label: String, presetId: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1D1B2E),
        border = BorderStroke(1.dp, Color(0xFF352F54)),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("keyframe_preset_$presetId")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, label, tint = AccentAmber, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
