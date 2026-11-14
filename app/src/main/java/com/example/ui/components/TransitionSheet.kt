package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Transform
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransitionType
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackTransitionColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitionSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onSelectTransition: (TransitionType, durationMs: Long) -> Unit,
    onApplyToAll: (TransitionType, durationMs: Long) -> Unit,
    onStartDragTransition: (TransitionType, durationMs: Long) -> Unit = { _, _ -> },
    onStopDragTransition: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTransition by remember { mutableStateOf(clip.transitionIn) }
    var selectedCategory by remember { mutableStateOf("All") }
    var transitionDurationSec by remember { mutableFloatStateOf(clip.transitionDurationMs / 1000f) }

    val categories = listOf("All", "Dissolve", "Color Fade", "Wipe", "Slide", "Zoom", "Shape", "Stylized")

    val filteredTransitions = remember(selectedCategory) {
        if (selectedCategory == "All") {
            TransitionType.entries.toList()
        } else {
            TransitionType.entries.filter { it.category == selectedCategory || it == TransitionType.NONE }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Video Transitions Library",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            // Drag and Drop Guidance Pill Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = TrackTransitionColor.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrackTransitionColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✥ Drag any transition card onto a timeline cut, or tap to assign below.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Live Animated Transition Visualizer Card
            TransitionLivePreviewBox(
                transition = selectedTransition,
                durationSec = transitionDurationSec,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(vertical = 4.dp)
            )

            // Duration Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transition Duration: ${String.format("%.1f", transitionDurationSec)}s",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = selectedTransition.displayName,
                    color = TrackTransitionColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = transitionDurationSec,
                onValueChange = { transitionDurationSec = it },
                valueRange = 0.2f..2.0f,
                steps = 17,
                colors = SliderDefaults.colors(
                    thumbColor = TrackTransitionColor,
                    activeTrackColor = TrackTransitionColor
                ),
                modifier = Modifier.height(28.dp)
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) TrackTransitionColor else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) TrackTransitionColor else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .testTag("transition_cat_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Transitions Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(vertical = 4.dp)
            ) {
                items(filteredTransitions) { transition ->
                    val isSelected = selectedTransition == transition
                    var isDraggingThis by remember { mutableStateOf(false) }
                    var dragOffsetPx by remember { mutableStateOf(Offset.Zero) }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDraggingThis) TrackTransitionColor.copy(alpha = 0.9f) else if (isSelected) Color(0xFF38153A) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isDraggingThis || isSelected) 2.dp else 1.dp,
                            color = if (isDraggingThis) AccentCyan else if (isSelected) TrackTransitionColor else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .zIndex(if (isDraggingThis) 99f else 1f)
                            .offset { IntOffset(dragOffsetPx.x.roundToInt(), dragOffsetPx.y.roundToInt()) }
                            .graphicsLayer {
                                if (isDraggingThis) {
                                    scaleX = 1.08f
                                    scaleY = 1.08f
                                    shadowElevation = 18f
                                }
                            }
                            .pointerInput(transition) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDraggingThis = true
                                        dragOffsetPx = Offset.Zero
                                        selectedTransition = transition
                                        val durMs = (transitionDurationSec * 1000).toLong()
                                        onStartDragTransition(transition, durMs)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetPx += dragAmount
                                    },
                                    onDragEnd = {
                                        isDraggingThis = false
                                        dragOffsetPx = Offset.Zero
                                        onStopDragTransition()
                                    },
                                    onDragCancel = {
                                        isDraggingThis = false
                                        dragOffsetPx = Offset.Zero
                                        onStopDragTransition()
                                    }
                                )
                            }
                            .clickable { selectedTransition = transition }
                            .testTag("transition_item_${transition.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) TrackTransitionColor else Color(0xFF262038),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, "Selected", tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(
                                            text = transition.displayName.take(1),
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = transition.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = transition.category,
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Apply to Selected & Apply to All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val durationMs = (transitionDurationSec * 1000).toLong()
                        onApplyToAll(selectedTransition, durationMs)
                        onDismiss()
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrackTransitionColor.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("apply_all_transitions_button")
                ) {
                    Text("Apply to All Clips", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        val durationMs = (transitionDurationSec * 1000).toLong()
                        onSelectTransition(selectedTransition, durationMs)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrackTransitionColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("apply_transition_button")
                ) {
                    Text("Apply Transition", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun TransitionLivePreviewBox(
    transition: TransitionType,
    durationSec: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "transitionPreview")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0D0B18),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26223B)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Base Clip A: Deep violet blue gradient
                val clipABrush = Brush.linearGradient(
                    listOf(Color(0xFF1E3A8A), Color(0xFF6B21A8))
                )
                // Incoming Clip B: Bright Cyan Amber gradient
                val clipBBrush = Brush.linearGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFFFFB300))
                )

                // Draw Clip A baseline
                drawRect(brush = clipABrush)

                // Render Transition Animation
                when (transition) {
                    TransitionType.CROSSFADE -> {
                        drawRect(brush = clipBBrush, alpha = progress)
                    }
                    TransitionType.DIP_TO_BLACK -> {
                        if (progress < 0.5f) {
                            val alpha = (progress * 2f).coerceIn(0f, 1f)
                            drawRect(color = Color.Black.copy(alpha = alpha))
                        } else {
                            drawRect(brush = clipBBrush)
                            val alpha = ((1f - progress) * 2f).coerceIn(0f, 1f)
                            drawRect(color = Color.Black.copy(alpha = alpha))
                        }
                    }
                    TransitionType.DIP_TO_WHITE -> {
                        if (progress < 0.5f) {
                            val alpha = (progress * 2f).coerceIn(0f, 1f)
                            drawRect(color = Color.White.copy(alpha = alpha))
                        } else {
                            drawRect(brush = clipBBrush)
                            val alpha = ((1f - progress) * 2f).coerceIn(0f, 1f)
                            drawRect(color = Color.White.copy(alpha = alpha))
                        }
                    }
                    TransitionType.WIPE_LEFT -> {
                        val wipeW = width * progress
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(width - wipeW, 0f),
                            size = Size(wipeW, height)
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(width - wipeW, 0f),
                            end = Offset(width - wipeW, height),
                            strokeWidth = 3f
                        )
                    }
                    TransitionType.WIPE_RIGHT -> {
                        val wipeW = width * progress
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(0f, 0f),
                            size = Size(wipeW, height)
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(wipeW, 0f),
                            end = Offset(wipeW, height),
                            strokeWidth = 3f
                        )
                    }
                    TransitionType.WIPE_UP -> {
                        val wipeH = height * progress
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(0f, height - wipeH),
                            size = Size(width, wipeH)
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, height - wipeH),
                            end = Offset(width, height - wipeH),
                            strokeWidth = 3f
                        )
                    }
                    TransitionType.WIPE_DOWN -> {
                        val wipeH = height * progress
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(0f, 0f),
                            size = Size(width, wipeH)
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(0f, wipeH),
                            end = Offset(width, wipeH),
                            strokeWidth = 3f
                        )
                    }
                    TransitionType.SLIDE_LEFT -> {
                        val slideX = width * (1f - progress)
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(slideX, 0f),
                            size = Size(width, height)
                        )
                    }
                    TransitionType.SLIDE_RIGHT -> {
                        val slideX = -width * (1f - progress)
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(slideX, 0f),
                            size = Size(width, height)
                        )
                    }
                    TransitionType.SLIDE_UP -> {
                        val slideY = height * (1f - progress)
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(0f, slideY),
                            size = Size(width, height)
                        )
                    }
                    TransitionType.SLIDE_DOWN -> {
                        val slideY = -height * (1f - progress)
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset(0f, slideY),
                            size = Size(width, height)
                        )
                    }
                    TransitionType.ZOOM_IN -> {
                        val scale = 0.3f + 0.7f * progress
                        val sW = width * scale
                        val sH = height * scale
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset((width - sW) / 2f, (height - sH) / 2f),
                            size = Size(sW, sH),
                            alpha = progress
                        )
                    }
                    TransitionType.ZOOM_OUT -> {
                        val scale = 1.6f - 0.6f * progress
                        val sW = width * scale
                        val sH = height * scale
                        drawRect(
                            brush = clipBBrush,
                            topLeft = Offset((width - sW) / 2f, (height - sH) / 2f),
                            size = Size(sW, sH),
                            alpha = progress
                        )
                    }
                    TransitionType.IRIS_CIRCLE -> {
                        val maxRadius = Math.hypot(width.toDouble() / 2, height.toDouble() / 2).toFloat()
                        val currentRadius = maxRadius * progress
                        drawCircle(
                            brush = clipBBrush,
                            radius = currentRadius,
                            center = Offset(width / 2, height / 2)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = currentRadius,
                            center = Offset(width / 2, height / 2),
                            style = Stroke(width = 3f)
                        )
                    }
                    TransitionType.GLITCH_FLASH -> {
                        drawRect(brush = clipBBrush, alpha = progress)
                        if (progress in 0.3f..0.7f) {
                            // RGB chromatic slice glitch
                            drawRect(
                                color = Color.Red.copy(alpha = 0.3f),
                                topLeft = Offset(10f, height * 0.3f),
                                size = Size(width - 20f, 15f)
                            )
                            drawRect(
                                color = Color.Cyan.copy(alpha = 0.4f),
                                topLeft = Offset(-10f, height * 0.6f),
                                size = Size(width, 10f)
                            )
                        }
                    }
                    TransitionType.NONE -> {
                        // Just shows hard cut at 50%
                        if (progress >= 0.5f) {
                            drawRect(brush = clipBBrush)
                        }
                    }
                }
            }

            // Overlay label
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = "Live Preview: ${transition.displayName} (${String.format("%.1f", durationSec)}s)",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
