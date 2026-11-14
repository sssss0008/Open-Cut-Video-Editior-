package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
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
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackTransitionColor
import kotlin.math.roundToInt

/**
 * Transition Library Component for selecting, previewing, and applying
 * visual transitions (crossfade, slide, wipe, dip to black/white, zoom, iris, glitch)
 * to clips and seams in the timeline.
 */
@Composable
fun TransitionLibraryComponent(
    clips: List<VideoClip>,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onApplyTransition: (clipId: String, transition: TransitionType, durationMs: Long) -> Unit,
    onApplyToAll: (transition: TransitionType, durationMs: Long) -> Unit,
    onStartDragTransition: (TransitionType, durationMs: Long) -> Unit = { _, _ -> },
    onStopDragTransition: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Target clip / seam: default to selected clip or first clip
    var targetClipId by remember(clips, selectedClipId) {
        mutableStateOf(selectedClipId ?: clips.firstOrNull()?.id ?: "")
    }

    val targetClip = clips.find { it.id == targetClipId } ?: clips.firstOrNull()
    var selectedTransition by remember(targetClip) {
        mutableStateOf(targetClip?.transitionIn ?: TransitionType.CROSSFADE)
    }
    var selectedCategory by remember { mutableStateOf("All") }
    var transitionDurationSec by remember(targetClip) {
        mutableFloatStateOf((targetClip?.transitionDurationMs ?: 600L) / 1000f)
    }

    val categories = listOf("All", "Dissolve", "Color Fade", "Wipe", "Slide", "Zoom", "Shape", "Stylized")
    val filteredTransitions = remember(selectedCategory) {
        if (selectedCategory == "All") {
            TransitionType.entries.toList()
        } else {
            TransitionType.entries.filter { it.category == selectedCategory || it == TransitionType.NONE }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfaceDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("transition_library_component")
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
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = "Transitions",
                            tint = TrackTransitionColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Transition Library",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visual scene cuts & dissolves between timeline clips",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Target Seam / Cut Selector (when multiple clips exist on the timeline)
        if (clips.size >= 2) {
            Text(
                text = "Apply Transition To Seam:",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("seam_selector_row")
            ) {
                items(clips.indices.toList()) { index ->
                    val clip = clips[index]
                    val isSelectedSeam = clip.id == targetClipId
                    val seamLabel = if (index == 0) {
                        "Start ➔ ${clip.name.take(8)}"
                    } else {
                        "Cut ${index}: ${clips[index - 1].name.take(6)} ➔ ${clip.name.take(6)}"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelectedSeam) TrackTransitionColor else StudioCardDark,
                        border = BorderStroke(
                            1.dp,
                            if (isSelectedSeam) AccentCyan else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .clickable {
                                targetClipId = clip.id
                                onSelectClip(clip.id)
                                selectedTransition = if (clip.transitionIn != TransitionType.NONE) clip.transitionIn else TransitionType.CROSSFADE
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .testTag("seam_item_${clip.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = seamLabel,
                                color = if (isSelectedSeam) Color.White else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (clip.transitionIn != TransitionType.NONE) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelectedSeam) Color.White else TrackTransitionColor,
                                    modifier = Modifier.size(14.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "⧗",
                                            color = if (isSelectedSeam) TrackTransitionColor else Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Animated Canvas Transition Preview
        TransitionLivePreviewBox(
            transition = selectedTransition,
            durationSec = transitionDurationSec,
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .padding(vertical = 4.dp)
        )

        // Duration Slider & Info Readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Duration: ${String.format(java.util.Locale.US, "%.1f", transitionDurationSec)}s",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${selectedTransition.displayName} (${selectedTransition.category})",
                color = AccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = transitionDurationSec,
            onValueChange = { transitionDurationSec = it },
            valueRange = 0.2f..2.5f,
            colors = SliderDefaults.colors(
                thumbColor = TrackTransitionColor,
                activeTrackColor = TrackTransitionColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .testTag("transition_duration_slider")
        )

        // Quick Preset Duration Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { dur ->
                val isSelected = kotlin.math.abs(transitionDurationSec - dur) < 0.05f
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) TrackTransitionColor else Color(0xFF1E293B),
                    modifier = Modifier.clickable { transitionDurationSec = dur }
                ) {
                    Text(
                        text = "${dur}s",
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) TrackTransitionColor else StudioCardDark,
                    border = BorderStroke(1.dp, if (isSelected) TrackTransitionColor else Color(0xFF2C2840)),
                    modifier = Modifier
                        .clickable { selectedCategory = cat }
                        .testTag("transition_cat_$cat")
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                .height(180.dp)
                .padding(vertical = 4.dp)
        ) {
            items(filteredTransitions) { transition ->
                val isSelected = selectedTransition == transition
                var isDraggingThis by remember { mutableStateOf(false) }
                var dragOffsetPx by remember { mutableStateOf(Offset.Zero) }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDraggingThis) TrackTransitionColor.copy(alpha = 0.9f) else if (isSelected) Color(0xFF38153A) else StudioCardDark,
                    border = BorderStroke(
                        width = if (isDraggingThis || isSelected) 2.dp else 1.dp,
                        color = if (isDraggingThis) AccentCyan else if (isSelected) TrackTransitionColor else Color(0xFF2C2840)
                    ),
                    modifier = Modifier
                        .zIndex(if (isDraggingThis) 100f else 1f)
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
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        .clickable {
                            selectedTransition = transition
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        .testTag("transition_card_${transition.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
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

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transition.displayName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Drag or Tap",
                                color = AccentCyan,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons: Apply to Selected Seam, Apply to All, Remove
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Remove Transition
            if (targetClip?.transitionIn != TransitionType.NONE) {
                OutlinedButton(
                    onClick = {
                        val clipId = targetClipId.ifEmpty { clips.firstOrNull()?.id ?: "" }
                        if (clipId.isNotEmpty()) {
                            onApplyTransition(clipId, TransitionType.NONE, 0L)
                            onClose()
                        }
                    },
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("remove_transition_button")
                ) {
                    Text("Remove", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Apply to All Cuts
            OutlinedButton(
                onClick = {
                    val durationMs = (transitionDurationSec * 1000).toLong()
                    onApplyToAll(selectedTransition, durationMs)
                    onClose()
                },
                border = BorderStroke(1.dp, TrackTransitionColor.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("apply_transition_all_cuts_button")
            ) {
                Text("Apply to All Cuts", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }

            // Apply to Selected Seam
            Button(
                onClick = {
                    val clipId = targetClipId.ifEmpty { clips.firstOrNull()?.id ?: "" }
                    if (clipId.isNotEmpty()) {
                        val durationMs = (transitionDurationSec * 1000).toLong()
                        onApplyTransition(clipId, selectedTransition, durationMs)
                        onClose()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TrackTransitionColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("apply_transition_seam_button")
            ) {
                Text("Apply Transition", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Modal Bottom Sheet wrapping TransitionLibraryComponent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitionLibrarySheet(
    clips: List<VideoClip>,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onApplyTransition: (clipId: String, transition: TransitionType, durationMs: Long) -> Unit,
    onApplyToAll: (transition: TransitionType, durationMs: Long) -> Unit,
    onStartDragTransition: (TransitionType, durationMs: Long) -> Unit = { _, _ -> },
    onStopDragTransition: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        TransitionLibraryComponent(
            clips = clips,
            selectedClipId = selectedClipId,
            onSelectClip = onSelectClip,
            onApplyTransition = onApplyTransition,
            onApplyToAll = onApplyToAll,
            onStartDragTransition = onStartDragTransition,
            onStopDragTransition = onStopDragTransition,
            onClose = onDismiss
        )
    }
}
