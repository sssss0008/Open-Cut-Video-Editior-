package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.model.TransitionType
import com.example.engine.TransitionsManager
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackTransitionColor
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Resolves which clip intersection seam in [seamBoundsInRoot] is targeted by [pointerInRoot].
 * Returns the clip ID of the incoming clip at that intersection, or null if outside all seams.
 */
fun resolveTransitionDropTarget(
    pointerInRoot: Offset,
    seamBoundsInRoot: Map<String, Rect>,
    hitPaddingPx: Float = 44f
): String? {
    var bestClipId: String? = null
    var bestDistance = Float.MAX_VALUE
    seamBoundsInRoot.forEach { (clipId, rect) ->
        val expanded = Rect(
            left = rect.left - hitPaddingPx,
            top = rect.top - hitPaddingPx,
            right = rect.right + hitPaddingPx,
            bottom = rect.bottom + hitPaddingPx
        )
        if (expanded.contains(pointerInRoot)) {
            val center = rect.center
            val dist = hypot(pointerInRoot.x - center.x, pointerInRoot.y - center.y)
            if (dist < bestDistance) {
                bestDistance = dist
                bestClipId = clipId
            }
        }
    }
    return bestClipId
}

/**
 * TransitionsLibrary Component.
 * Provides categorized visual transitions (Cross-fade, Slides, Wipes, Dissolves, Zoom, Iris, Glitch)
 * with continuous drag-and-drop support directly onto timeline clip intersections.
 */
@Composable
fun TransitionsLibrary(
    selectedTransition: TransitionType,
    onSelectTransition: (TransitionType) -> Unit,
    transitionDurationSec: Float,
    onDurationChange: (Float) -> Unit,
    onDragTransitionStart: (TransitionType, durationMs: Long, pointerInRoot: Offset?) -> Unit,
    onDragTransitionMove: (pointerInRoot: Offset) -> Unit,
    onDragTransitionEnd: () -> Unit,
    onDragTransitionCancel: () -> Unit,
    onApplyToSelectedClip: () -> Unit,
    onApplyToAllClips: () -> Unit,
    onOpenFullInspector: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Dissolve", "Slide", "Wipe", "Color Fade", "Zoom", "Shape", "Stylized")

    val filtered = remember(selectedCategory) {
        if (selectedCategory == "All") {
            TransitionType.entries.toList()
        } else {
            TransitionType.entries.filter { it.category == selectedCategory || it == TransitionType.NONE }
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioSurfaceDark,
        border = BorderStroke(1.dp, TrackTransitionColor.copy(alpha = 0.55f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("transitions_library")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
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
                        color = TrackTransitionColor.copy(alpha = 0.25f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Transform,
                                contentDescription = "Transitions Library",
                                tint = TrackTransitionColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Transitions Library",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Drag any card onto a clip intersection in the timeline",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onOpenFullInspector != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioCardDark,
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .clickable { onOpenFullInspector() }
                                .padding(horizontal = 2.dp)
                                .testTag("transitions_library_inspector_btn")
                        ) {
                            Text(
                                text = "Preview",
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "${String.format("%.1f", transitionDurationSec)}s",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("transitions_library_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Transitions Library",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Category Filter Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                items(categories) { cat ->
                    val isSel = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) TrackTransitionColor else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSel) TrackTransitionColor else Color(0xFF2C2840)),
                        modifier = Modifier
                            .heightIn(min = 32.dp)
                            .clickable { selectedCategory = cat }
                            .testTag("transition_panel_cat_$cat")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSel) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Draggable Transition Effect Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                items(filtered, key = { it.name }) { transition ->
                    val isSelected = selectedTransition == transition
                    var isDraggingCard by remember { mutableStateOf(false) }
                    var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDraggingCard) TrackTransitionColor else if (isSelected) Color(0xFF38153A) else StudioCardDark,
                        border = BorderStroke(
                            width = if (isDraggingCard || isSelected) 2.dp else 1.dp,
                            color = if (isDraggingCard) AccentCyan else if (isSelected) TrackTransitionColor else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .onGloballyPositioned { cardCoords = it }
                            .graphicsLayer {
                                if (isDraggingCard) {
                                    alpha = 0.55f
                                    scaleX = 0.96f
                                    scaleY = 0.96f
                                }
                            }
                            .pointerInput(transition, transitionDurationSec) {
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        isDraggingCard = true
                                        onSelectTransition(transition)
                                        val durMs = (transitionDurationSec * 1000f).toLong().coerceIn(200L, 2500L)
                                        val rootPos = cardCoords?.localToRoot(startOffset)
                                        onDragTransitionStart(transition, durMs, rootPos)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val rootPos = cardCoords?.localToRoot(change.position)
                                        if (rootPos != null) {
                                            onDragTransitionMove(rootPos)
                                        }
                                    },
                                    onDragEnd = {
                                        isDraggingCard = false
                                        onDragTransitionEnd()
                                    },
                                    onDragCancel = {
                                        isDraggingCard = false
                                        onDragTransitionCancel()
                                    }
                                )
                            }
                            .clickable {
                                onSelectTransition(transition)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .testTag("transition_card_${transition.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) TrackTransitionColor else Color(0xFF262038),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (transition == TransitionType.NONE) "✕" else TransitionsManager.transitionGlyph(transition),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = transition.displayName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragIndicator,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Drag to Cut",
                                    color = AccentCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Duration Slider and Quick Apply Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration:",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.width(56.dp)
                )

                Slider(
                    value = transitionDurationSec.coerceIn(0.2f, 2.0f),
                    onValueChange = onDurationChange,
                    valueRange = 0.2f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = TrackTransitionColor,
                        activeTrackColor = TrackTransitionColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApplyToSelectedClip,
                    colors = ButtonDefaults.buttonColors(containerColor = TrackTransitionColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("panel_apply_transition_btn")
                ) {
                    Text("Apply", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onApplyToAllClips,
                    border = BorderStroke(1.dp, TrackTransitionColor.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("panel_apply_all_transitions_btn")
                ) {
                    Text("All Cuts", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Floating drag preview badge positioned at root window coordinates while dragging a transition card
 * toward a clip intersection in the timeline.
 */
@Composable
fun TransitionDragGhostOverlay(
    transition: TransitionType,
    durationMs: Long,
    pointerInRoot: Offset,
    rootOriginInRoot: Offset,
    hoveredClipId: String?,
    modifier: Modifier = Modifier
) {
    val localOffset = pointerInRoot - rootOriginInRoot
    val isOverTarget = hoveredClipId != null

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isOverTarget) AccentEmerald else TrackTransitionColor,
        border = BorderStroke(2.dp, Color.White),
        shadowElevation = 18.dp,
        modifier = modifier
            .zIndex(999f)
            .offset {
                IntOffset(
                    x = (localOffset.x - 140f).roundToInt(),
                    y = (localOffset.y - 110f).roundToInt()
                )
            }
            .testTag("transition_drag_ghost")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = TransitionsManager.transitionGlyph(transition),
                color = if (isOverTarget) Color.Black else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    text = transition.displayName,
                    color = if (isOverTarget) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isOverTarget) "Release to Drop on Cut • ${durationMs}ms" else "Drop on Clip Intersection (⧗)",
                    color = if (isOverTarget) Color(0xFF064E3B) else AccentCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
