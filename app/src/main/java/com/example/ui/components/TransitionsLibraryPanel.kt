package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackTransitionColor
import kotlin.math.roundToInt

@Composable
fun TransitionsLibraryPanel(
    selectedTransition: TransitionType,
    onSelectTransition: (TransitionType) -> Unit,
    transitionDurationSec: Float,
    onDurationChange: (Float) -> Unit,
    onStartDragTransition: (TransitionType, durationMs: Long) -> Unit,
    onStopDragTransition: () -> Unit,
    onApplyToSelectedClip: () -> Unit,
    onApplyToAllClips: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Dissolve", "Color Fade", "Wipe", "Slide", "Zoom", "Shape", "Stylized")

    val filtered = remember(selectedCategory) {
        if (selectedCategory == "All") {
            TransitionType.entries.toList()
        } else {
            TransitionType.entries.filter { it.category == selectedCategory || it == TransitionType.NONE }
        }
    }

    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = StudioSurfaceDark,
        border = BorderStroke(1.dp, Color(0xFF231F38)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("transitions_library_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Header with drag indicator and close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Transform,
                        contentDescription = "Transitions",
                        tint = TrackTransitionColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Transitions Library",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.1f", transitionDurationSec)}s",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Drag hint banner
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = TrackTransitionColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, TrackTransitionColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag",
                        tint = TrackTransitionColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Drag any transition card below and drop directly onto timeline cut badges!",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Category Chips
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
                            .clickable { selectedCategory = cat }
                            .testTag("transition_panel_cat_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Draggable Horizontal Scroll of Transition Cards
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                items(filtered) { transition ->
                    val isSelected = selectedTransition == transition
                    var isDragging by remember { mutableStateOf(false) }
                    var dragOffset by remember { mutableStateOf(Offset.Zero) }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDragging) TrackTransitionColor else if (isSelected) Color(0xFF38153A) else StudioCardDark,
                        border = BorderStroke(
                            width = if (isDragging || isSelected) 2.dp else 1.dp,
                            color = if (isDragging) AccentCyan else if (isSelected) TrackTransitionColor else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .width(130.dp)
                            .zIndex(if (isDragging) 100f else 1f)
                            .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                            .graphicsLayer {
                                if (isDragging) {
                                    scaleX = 1.08f
                                    scaleY = 1.08f
                                    shadowElevation = 18f
                                }
                            }
                            .pointerInput(transition) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDragging = true
                                        dragOffset = Offset.Zero
                                        onSelectTransition(transition)
                                        val durMs = (transitionDurationSec * 1000).toLong()
                                        onStartDragTransition(transition, durMs)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        dragOffset = Offset.Zero
                                        onStopDragTransition()
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                        dragOffset = Offset.Zero
                                        onStopDragTransition()
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
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) TrackTransitionColor else Color(0xFF262038),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = when (transition) {
                                            TransitionType.NONE -> "✕"
                                            TransitionType.CROSSFADE -> "⧗"
                                            TransitionType.DIP_TO_BLACK -> "⬛"
                                            TransitionType.DIP_TO_WHITE -> "⬜"
                                            TransitionType.WIPE_LEFT, TransitionType.WIPE_UP -> "⇦"
                                            TransitionType.WIPE_RIGHT, TransitionType.WIPE_DOWN -> "⇨"
                                            TransitionType.SLIDE_LEFT, TransitionType.SLIDE_RIGHT -> "➔"
                                            TransitionType.ZOOM_IN, TransitionType.ZOOM_OUT -> "⌕"
                                            TransitionType.IRIS_CIRCLE -> "◉"
                                            TransitionType.GLITCH_FLASH -> "⚡"
                                            else -> "T"
                                        },
                                        color = Color.White,
                                        fontSize = 11.sp,
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

                            Text(
                                text = "Hold & Drag ✥",
                                color = AccentCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Duration Slider and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration:",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.width(55.dp)
                )

                Slider(
                    value = transitionDurationSec,
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
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("panel_apply_transition_btn")
                ) {
                    Text("Apply", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = onApplyToAllClips,
                    border = BorderStroke(1.dp, TrackTransitionColor.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("panel_apply_all_transitions_btn")
                ) {
                    Text("All", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}
