package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.SampleMediaRepository
import com.example.data.model.SampleVideo
import com.example.data.model.SnapTargetType
import com.example.data.model.TimelineCueMarker
import com.example.data.model.VideoClip
import com.example.data.model.VisualSnapTarget
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PlayheadRed
import kotlin.math.roundToInt

/**
 * Calculates the closest magnetic snap target on the timeline.
 * Magnetic snapping snaps to:
 * 1. Playhead position
 * 2. Cut points (clip start and end seams)
 * 3. Timeline start (00:00.00)
 * 4. User Cue Markers
 * 5. Major second boundaries on the ruler
 */
fun calculateSnapTarget(
    rawTimeMs: Long,
    timelineZoom: Float,
    currentPositionMs: Long,
    clips: List<VideoClip>,
    timelineMarkers: List<TimelineCueMarker>,
    totalDurationMs: Long,
    isSnappingEnabled: Boolean
): VisualSnapTarget? {
    if (!isSnappingEnabled) return null

    // Snap threshold distance in time (approx 18-20 dp equivalent)
    val snapToleranceMs = (20f / (55f * timelineZoom) * 1000f).toLong().coerceIn(80L, 350L)

    val candidates = mutableListOf<VisualSnapTarget>()

    // 1. Start of Timeline (0s)
    candidates.add(
        VisualSnapTarget(
            timeMs = 0L,
            label = "00:00.00 (Start)",
            type = SnapTargetType.START_ZERO,
            colorHex = 0xFF00E5FF
        )
    )

    // 2. Playhead position
    candidates.add(
        VisualSnapTarget(
            timeMs = currentPositionMs,
            label = "Playhead",
            type = SnapTargetType.PLAYHEAD,
            colorHex = 0xFFFF0055
        )
    )

    // 3. Clip cut seams (start and end of all clips)
    clips.forEach { clip ->
        if (clip.timelineStartMs > 0L) {
            candidates.add(
                VisualSnapTarget(
                    timeMs = clip.timelineStartMs,
                    label = "Cut (${clip.name.take(10)})",
                    type = SnapTargetType.CUT_POINT,
                    colorHex = 0xFF38BDF8
                )
            )
        }
        candidates.add(
            VisualSnapTarget(
                timeMs = clip.timelineEndMs,
                label = "Cut (${clip.name.take(10)})",
                type = SnapTargetType.CUT_POINT,
                colorHex = 0xFF38BDF8
            )
        )
    }

    // 4. Timeline Cue Markers
    timelineMarkers.forEach { marker ->
        candidates.add(
            VisualSnapTarget(
                timeMs = marker.timeMs,
                label = "Cue: ${marker.label}",
                type = SnapTargetType.CUE_MARKER,
                colorHex = marker.colorHex
            )
        )
    }

    // 5. Second grid lines
    val totalSeconds = (totalDurationMs / 1000L).toInt() + 5
    for (sec in 1..totalSeconds) {
        val secMs = sec * 1000L
        candidates.add(
            VisualSnapTarget(
                timeMs = secMs,
                label = "${sec}s Grid",
                type = SnapTargetType.SECOND_GRID,
                colorHex = 0xFF94A3B8
            )
        )
    }

    // Find candidate within tolerance
    val inRange = candidates.filter { kotlin.math.abs(it.timeMs - rawTimeMs) <= snapToleranceMs }
    if (inRange.isEmpty()) return null

    // Prioritize Playhead > Start Zero > Cue Marker > Cut Point > Second Grid
    return inRange.minByOrNull { target ->
        val distance = kotlin.math.abs(target.timeMs - rawTimeMs)
        val priorityWeight = when (target.type) {
            SnapTargetType.PLAYHEAD -> 0L
            SnapTargetType.START_ZERO -> 4L
            SnapTargetType.CUE_MARKER -> 8L
            SnapTargetType.CUT_POINT -> 15L
            SnapTargetType.SECOND_GRID -> 50L
        }
        distance + priorityWeight
    }
}

/**
 * Visual Magnetic Snapping Overlay line & badge.
 * Renders a neon cyan/amber laser guideline spanning through all tracks,
 * paired with a floating pill badge indicator displaying the snap target.
 */
@Composable
fun VisualSnapOverlay(
    snapTarget: VisualSnapTarget,
    msToDp: (Long) -> Dp,
    modifier: Modifier = Modifier
) {
    val xDp = msToDp(snapTarget.timeMs)
    val color = Color(snapTarget.colorHex)

    Box(
        modifier = modifier
            .offset(x = xDp)
            .width(2.dp)
            .zIndex(150f)
            .testTag("visual_snap_line")
    ) {
        // Glowing laser guideline
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Soft outer neon glow
            drawLine(
                color = color.copy(alpha = 0.45f),
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 6f
            )
            // Crisp inner laser line
            drawLine(
                color = color,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
            )
        }

        // Floating Snap Pill Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, color),
            modifier = Modifier
                .offset(x = (-36).dp, y = 2.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .testTag("visual_snap_badge")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "Magnetic Snap",
                    tint = color,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "SNAP: ${snapTarget.label}",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Draggable Media Clips Tray / Quick Media Dock.
 * Displays sample video clips that can be dragged directly downwards onto the Video track
 * with live visual snapping feedback, or added via a 1-tap quick add button.
 */
@Composable
fun DraggableMediaClipsTray(
    onDropClip: (SampleVideo, targetTimeMs: Long) -> Unit,
    onQuickAddClip: (SampleVideo) -> Unit,
    onActiveDragChange: (isDragging: Boolean, sample: SampleVideo?, previewX: Float, previewY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val sampleVideos = SampleMediaRepository.sampleVideos
    val scrollState = rememberScrollState()

    Surface(
        color = Color(0xFF0C101C),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("draggable_media_tray")
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Media Tray",
                        tint = AccentCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "MEDIA DOCK",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "• Drag clip down to timeline with snap",
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentCyan.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${sampleVideos.size} clips ready",
                        color = AccentCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Draggable Clip Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sampleVideos.forEach { sample ->
                    DraggableClipCard(
                        sample = sample,
                        onDrop = { dropTimeMs ->
                            onDropClip(sample, dropTimeMs)
                        },
                        onQuickAdd = {
                            onQuickAddClip(sample)
                        },
                        onDragUpdate = { isDragging, x, y ->
                            onActiveDragChange(isDragging, if (isDragging) sample else null, x, y)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableClipCard(
    sample: SampleVideo,
    onDrop: (targetTimeMs: Long) -> Unit,
    onQuickAdd: () -> Unit,
    onDragUpdate: (isDragging: Boolean, x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val gradientStart = Color(sample.colorGradientHex.first)
    val gradientEnd = Color(sample.colorGradientHex.second)

    Box(
        modifier = modifier
            .width(130.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(gradientStart, gradientEnd)
                )
            )
            .border(
                1.dp,
                if (isDragging) AccentCyan else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(6.dp)
            )
            .pointerInput(sample.name) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDragUpdate(true, 0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                        onDragUpdate(true, dragOffsetX, dragOffsetY)
                    },
                    onDragEnd = {
                        isDragging = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // If user dragged down toward timeline (positive Y > 25dp)
                        if (dragOffsetY > 20f) {
                            onDrop(-1L) // -1 indicates dropped onto timeline
                        }
                        onDragUpdate(false, 0f, 0f)
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        onDragUpdate(false, 0f, 0f)
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    }
                )
            }
            .testTag("media_clip_drag_card_${sample.name.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Title & Quick Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sample.name,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 1-tap quick add button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onQuickAdd()
                        }
                        .testTag("media_clip_add_${sample.name.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to timeline",
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            }

            // Bottom Row: Duration & Drag Handle indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${sample.durationMs / 1000}s",
                        color = AccentCyan,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag to timeline",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "DRAG",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
