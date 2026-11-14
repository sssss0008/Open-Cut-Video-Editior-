package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.PlayheadRed
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.VideoEditorViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * High-performance Video Scrubber UI component featuring:
 * 1. Micro-scrubber frame filmstrip ribbon with SMPTE frame ticks.
 * 2. Precision tactile rotary Jog Wheel / Shuttle dial for frame-by-frame scrubbing.
 * 3. Dual-handle frame-accurate Mark In / Mark Out trimmer.
 * 4. Nudge controls (-10f, -1f, +1f, +10f) and playhead sync.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecisionVideoScrubberSheet(
    clip: VideoClip,
    currentPlayheadMs: Long,
    fps: Int = 30,
    onDismiss: () -> Unit,
    onSeek: (Long) -> Unit,
    onApplyTrim: (inMs: Long, outMs: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    val frameDurationMs = (1000.0 / fps).toLong().coerceAtLeast(1L)
    var localTrimInMs by remember(clip) { mutableLongStateOf(clip.trimInMs) }
    var localTrimOutMs by remember(clip) { mutableLongStateOf(clip.trimOutMs) }

    // Position within the clip's source duration
    val clipRelativeMs = (currentPlayheadMs - clip.timelineStartMs)
        .coerceIn(0L, clip.trimmedDurationMs)
    val currentSourceMs = (clip.trimInMs + clipRelativeMs * clip.speed)
        .toLong()
        .coerceIn(0L, clip.sourceDurationMs)

    val currentFrameNumber = (currentSourceMs / frameDurationMs).toInt()
    val totalSourceFrames = (clip.sourceDurationMs / frameDurationMs).toInt().coerceAtLeast(1)
    val trimmedFrames = ((localTrimOutMs - localTrimInMs) / frameDurationMs).toInt().coerceAtLeast(1)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("precision_scrubber_sheet")
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(AccentCyan.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, AccentCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Precision Scrubber",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Frame Scrubber: ${clip.name}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "30 FPS Frame-Accurate Precision Trimming",
                            color = AccentCyan,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("precision_scrubber_close")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SMPTE Timecode & Frame Counter Dashboard
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F1522),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT TIMECODE",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatSmpteTimecode(currentSourceMs, fps),
                            color = AccentCyan,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "FRAME",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "#$currentFrameNumber / $totalSourceFrames",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TRIMMED DURATION",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$trimmedFrames frames (${VideoEditorViewModel.formatTime(localTrimOutMs - localTrimInMs)})",
                            color = AccentPink,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. High-Performance Micro-Scrubber Filmstrip Ribbon
            Text(
                text = "FILMSTRIP SCRUBBER (Drag anywhere to navigate frames):",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            MicroScrubberRibbon(
                sourceDurationMs = clip.sourceDurationMs,
                currentSourceMs = currentSourceMs,
                trimInMs = localTrimInMs,
                trimOutMs = localTrimOutMs,
                clipColor = Color(clip.accentColorHex),
                fps = fps,
                onSeekToSourceMs = { newSourceMs ->
                    val deltaSourceMs = newSourceMs - clip.trimInMs
                    val newTimelinePos = (clip.timelineStartMs + (deltaSourceMs / clip.speed).toLong())
                        .coerceIn(0L, clip.timelineEndMs)
                    onSeek(newTimelinePos)
                },
                onUpdateTrim = { inMs, outMs ->
                    localTrimInMs = inMs
                    localTrimOutMs = outMs
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Tactical Jog Wheel & Frame Stepping Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive Rotary Jog Wheel / Shuttle Dial
                TactileJogWheel(
                    currentSourceMs = currentSourceMs,
                    frameDurationMs = frameDurationMs,
                    onStepFrame = { frameDelta ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val deltaMs = frameDelta * frameDurationMs
                        val newPos = (currentPlayheadMs + deltaMs).coerceAtLeast(0L)
                        onSeek(newPos)
                    },
                    modifier = Modifier
                        .size(110.dp)
                        .testTag("rotary_jog_wheel")
                )

                // Quick Navigation Grid
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "FRAME STEPPING",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Stepping Buttons: -10f, -1f, +1f, +10f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StepButton(label = "-10f", onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSeek((currentPlayheadMs - 10 * frameDurationMs).coerceAtLeast(0L))
                        })
                        StepButton(label = "-1f", onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSeek((currentPlayheadMs - frameDurationMs).coerceAtLeast(0L))
                        })
                        StepButton(label = "+1f", onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSeek(currentPlayheadMs + frameDurationMs)
                        })
                        StepButton(label = "+10f", onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSeek(currentPlayheadMs + 10 * frameDurationMs)
                        })
                    }

                    // Jump to Head/Tail
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSeek(clip.timelineStartMs)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Icon(Icons.Default.FirstPage, "Start", tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("To Start", fontSize = 10.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSeek(clip.timelineEndMs)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(34.dp)
                        ) {
                            Text("To End", fontSize = 10.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.LastPage, "End", tint = AccentCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Mark In / Mark Out Action Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF131A26),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF243046)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Set In Point Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            localTrimInMs = currentSourceMs.coerceAtMost(localTrimOutMs - frameDurationMs * 2)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp).testTag("set_in_at_playhead")
                    ) {
                        Text("[ Set In", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Reset Trim
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            localTrimInMs = 0L
                            localTrimOutMs = clip.sourceDurationMs
                        },
                        modifier = Modifier.size(36.dp).testTag("reset_trim_button")
                    ) {
                        Icon(Icons.Default.Refresh, "Reset", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }

                    // Set Out Point Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            localTrimOutMs = currentSourceMs.coerceAtLeast(localTrimInMs + frameDurationMs * 2)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp).testTag("set_out_at_playhead")
                    ) {
                        Text("Set Out ]", color = AccentPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Commit Trim Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onApplyTrim(localTrimInMs, localTrimOutMs)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("apply_precision_trim_button")
            ) {
                Icon(Icons.Default.Check, "Apply", tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Apply Frame-Accurate Trim (${trimmedFrames} Frames)",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Micro-Scrubber Ribbon Component with Filmstrip & Dual-Handle Trimming.
 */
@Composable
fun MicroScrubberRibbon(
    sourceDurationMs: Long,
    currentSourceMs: Long,
    trimInMs: Long,
    trimOutMs: Long,
    clipColor: Color,
    fps: Int,
    onSeekToSourceMs: (Long) -> Unit,
    onUpdateTrim: (inMs: Long, outMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090D16))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val totalHeightPx = constraints.maxHeight.toFloat()

        val msToPx: (Long) -> Float = { ms ->
            if (sourceDurationMs > 0) (ms.toFloat() / sourceDurationMs.toFloat()) * totalWidthPx else 0f
        }
        val pxToMs: (Float) -> Long = { px ->
            if (totalWidthPx > 0) ((px / totalWidthPx) * sourceDurationMs).toLong().coerceIn(0L, sourceDurationMs) else 0L
        }

        // Tap/Drag anywhere on ribbon to scrub playhead
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(sourceDurationMs) {
                    detectTapGestures { offset ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeekToSourceMs(pxToMs(offset.x))
                    }
                }
                .pointerInput(sourceDurationMs) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        onSeekToSourceMs(pxToMs(change.position.x))
                    }
                }
        ) {
            // Render Filmstrip cells & SMPTE Frame ticks
            Canvas(modifier = Modifier.fillMaxSize()) {
                val frameDurationMs = 1000.0 / fps
                val totalFrames = (sourceDurationMs / frameDurationMs).toInt().coerceAtLeast(1)

                // Draw filmstrip frames background
                val cellCount = 12
                val cellWidth = size.width / cellCount
                for (i in 0 until cellCount) {
                    val left = i * cellWidth
                    // Film cell border
                    drawRect(
                        color = Color(0xFF161E2E),
                        topLeft = Offset(left + 1f, 4f),
                        size = androidx.compose.ui.geometry.Size(cellWidth - 2f, size.height - 8f)
                    )
                    // Top & Bottom film perforations
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(left + 4f, 6f),
                        size = androidx.compose.ui.geometry.Size(cellWidth - 8f, 6f)
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.6f),
                        topLeft = Offset(left + 4f, size.height - 12f),
                        size = androidx.compose.ui.geometry.Size(cellWidth - 8f, 6f)
                    )
                }

                // Inactive dimmed region (before Trim In)
                val inPx = msToPx(trimInMs)
                if (inPx > 0f) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.65f),
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(inPx, size.height)
                    )
                }

                // Active trimmed range highlight
                val outPx = msToPx(trimOutMs)
                drawRect(
                    color = clipColor.copy(alpha = 0.28f),
                    topLeft = Offset(inPx, 4f),
                    size = androidx.compose.ui.geometry.Size(maxOf(outPx - inPx, 4f), size.height - 8f)
                )

                // Inactive dimmed region (after Trim Out)
                if (outPx < size.width) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.65f),
                        topLeft = Offset(outPx, 0f),
                        size = androidx.compose.ui.geometry.Size(size.width - outPx, size.height)
                    )
                }

                // Fine Frame Ticks (every 5 frames)
                for (f in 0..totalFrames step 5) {
                    val frameMs = (f * frameDurationMs).toLong()
                    val x = msToPx(frameMs)
                    val isMajor = f % 15 == 0
                    drawLine(
                        color = if (isMajor) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                        start = Offset(x, size.height - 14f),
                        end = Offset(x, size.height - if (isMajor) 24f else 18f),
                        strokeWidth = if (isMajor) 1.5f else 1f
                    )
                }
            }

            // Left Trim In Handle
            val inOffsetDp = with(density) { msToPx(trimInMs).toDp() }
            Box(
                modifier = Modifier
                    .offset { IntOffset(with(density) { inOffsetDp.toPx() }.roundToInt() - 10, 0) }
                    .width(22.dp)
                    .fillMaxHeight()
                    .pointerInput(sourceDurationMs) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val currentPx = msToPx(trimInMs)
                            val newPx = currentPx + dragAmount.x
                            val newMs = pxToMs(newPx).coerceIn(0L, trimOutMs - 100L)
                            onUpdateTrim(newMs, trimOutMs)
                        }
                    }
                    .testTag("trim_in_handle")
            ) {
                // Bracket visual
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(AccentCyan, RoundedCornerShape(2.dp))
                )
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp),
                    color = AccentCyan,
                    modifier = Modifier
                        .size(width = 16.dp, height = 30.dp)
                        .align(Alignment.Center)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("[", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }

            // Right Trim Out Handle
            val outOffsetDp = with(density) { msToPx(trimOutMs).toDp() }
            Box(
                modifier = Modifier
                    .offset { IntOffset(with(density) { outOffsetDp.toPx() }.roundToInt() - 12, 0) }
                    .width(22.dp)
                    .fillMaxHeight()
                    .pointerInput(sourceDurationMs) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val currentPx = msToPx(trimOutMs)
                            val newPx = currentPx + dragAmount.x
                            val newMs = pxToMs(newPx).coerceIn(trimInMs + 100L, sourceDurationMs)
                            onUpdateTrim(trimInMs, newMs)
                        }
                    }
                    .testTag("trim_out_handle")
            ) {
                Surface(
                    shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
                    color = AccentPink,
                    modifier = Modifier
                        .size(width = 16.dp, height = 30.dp)
                        .align(Alignment.Center)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("]", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .background(AccentPink, RoundedCornerShape(2.dp))
                )
            }

            // Red Playhead Line & Frame Pointer
            val playheadPx = msToPx(currentSourceMs)
            val playheadDp = with(density) { playheadPx.toDp() }
            Box(
                modifier = Modifier
                    .offset { IntOffset(with(density) { playheadDp.toPx() }.roundToInt() - 1, 0) }
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(PlayheadRed)
                    .shadow(elevation = 6.dp, spotColor = PlayheadRed)
            )

            // Playhead indicator head badge
            Box(
                modifier = Modifier
                    .offset { IntOffset(with(density) { playheadDp.toPx() }.roundToInt() - 6, 0) }
                    .size(width = 12.dp, height = 12.dp)
                    .background(PlayheadRed, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            )
        }
    }
}

/**
 * Tactile Rotary Jog Wheel for High-Performance Frame-Accurate Scrubbing.
 * Rotating the dial clockwise/counter-clockwise steps forward/backward frame-by-frame.
 */
@Composable
fun TactileJogWheel(
    currentSourceMs: Long,
    frameDurationMs: Long,
    onStepFrame: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var previousAngle by remember { mutableFloatStateOf(0f) }
    var accumulatedDeltaAngle by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF2B3242), Color(0xFF141923), Color(0xFF090D14))
                )
            )
            .border(2.dp, Color(0xFF334155), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        previousAngle = (atan2(offset.y - centerY, offset.x - centerX) * 180f / PI).toFloat()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val currentAngle = (atan2(change.position.y - centerY, change.position.x - centerX) * 180f / PI).toFloat()
                        var delta = currentAngle - previousAngle
                        // Wrap around -180 to 180
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        rotationAngle = (rotationAngle + delta) % 360f
                        accumulatedDeltaAngle += delta
                        previousAngle = currentAngle

                        // Every 12 degrees of wheel rotation = 1 video frame step
                        val degreesPerFrame = 12f
                        if (accumulatedDeltaAngle >= degreesPerFrame) {
                            val frames = (accumulatedDeltaAngle / degreesPerFrame).toInt()
                            onStepFrame(frames)
                            accumulatedDeltaAngle -= frames * degreesPerFrame
                        } else if (accumulatedDeltaAngle <= -degreesPerFrame) {
                            val frames = (accumulatedDeltaAngle / degreesPerFrame).toInt()
                            onStepFrame(frames)
                            accumulatedDeltaAngle -= frames * degreesPerFrame
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Rotary grooves and tick marks
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 4f
            val innerRadius = outerRadius - 10f

            // Draw outer radial tick grooves (30 ticks for 30fps metaphor)
            for (i in 0 until 30) {
                val tickAngleRad = ((i * 12f + rotationAngle) * PI / 180f).toFloat()
                val start = Offset(
                    center.x + innerRadius * cos(tickAngleRad),
                    center.y + innerRadius * sin(tickAngleRad)
                )
                val end = Offset(
                    center.x + outerRadius * cos(tickAngleRad),
                    center.y + outerRadius * sin(tickAngleRad)
                )
                drawLine(
                    color = if (i % 5 == 0) AccentCyan else Color.White.copy(alpha = 0.25f),
                    start = start,
                    end = end,
                    strokeWidth = if (i % 5 == 0) 2f else 1f
                )
            }

            // Jog wheel inner dimple
            val dimpleAngleRad = (rotationAngle * PI / 180f).toFloat()
            val dimpleDistance = outerRadius * 0.65f
            val dimpleCenter = Offset(
                center.x + dimpleDistance * cos(dimpleAngleRad),
                center.y + dimpleDistance * sin(dimpleAngleRad)
            )
            drawCircle(
                color = AccentCyan,
                radius = 6f,
                center = dimpleCenter
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = dimpleCenter
            )
        }

        // Center dial badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "JOG",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "DIAL",
                color = AccentCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StepButton(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}
