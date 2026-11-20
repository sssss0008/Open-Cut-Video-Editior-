package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TextFields
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportResolution
import com.example.data.model.ImageOverlay
import com.example.data.model.LutPreset
import com.example.data.model.OverlaySticker
import com.example.data.model.ShapeOverlay
import com.example.data.model.TextOverlay
import com.example.data.model.TransitionType
import com.example.data.model.VideoClip
import com.example.data.model.VideoFilter
import com.example.engine.TransitionsManager
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class ExportPreviewSegment(
    val startMs: Long,
    val endMs: Long
) {
    val durationMs: Long
        get() = (endMs - startMs).coerceAtLeast(0L)
}

data class ActivePreviewOverlays(
    val textOverlays: List<TextOverlay>,
    val stickers: List<OverlaySticker>,
    val shapes: List<ShapeOverlay>,
    val imageOverlays: List<ImageOverlay>
)

/**
 * Computes a short, bounded preview window (up to [segmentLengthMs], default 5s)
 * anchored around [anchorPositionMs] within [0, totalDurationMs].
 */
fun computeExportPreviewSegment(
    totalDurationMs: Long,
    anchorPositionMs: Long,
    segmentLengthMs: Long = 5000L
): ExportPreviewSegment {
    val safeTotal = totalDurationMs.coerceAtLeast(1000L)
    val safeWindow = segmentLengthMs.coerceIn(500L, safeTotal)
    val halfWindow = safeWindow / 2L
    val clampedAnchor = anchorPositionMs.coerceIn(0L, safeTotal)
    val maxStart = (safeTotal - safeWindow).coerceAtLeast(0L)
    val start = (clampedAnchor - halfWindow).coerceIn(0L, maxStart)
    val end = (start + safeWindow).coerceAtMost(safeTotal)
    return ExportPreviewSegment(startMs = start, endMs = end)
}

/**
 * Finds the best timeline timestamp highlighting cinematic filters, transitions, or text overlays.
 */
fun findHighlightAnchorMs(
    clips: List<VideoClip>,
    textOverlays: List<TextOverlay>
): Long {
    val firstTextStart = textOverlays.minByOrNull { it.timelineStartMs }?.timelineStartMs
    if (firstTextStart != null) return firstTextStart + 500L
    val firstTransitionStart = clips.firstOrNull { it.transitionIn != TransitionType.NONE }?.timelineStartMs
    if (firstTransitionStart != null) return firstTransitionStart
    val firstFilteredStart = clips.firstOrNull {
        it.filter != VideoFilter.NONE || it.colorGrading.lut != LutPreset.NONE
    }?.timelineStartMs
    return firstFilteredStart ?: 0L
}

fun resolveActiveOverlaysAtTime(
    textOverlays: List<TextOverlay>,
    stickers: List<OverlaySticker>,
    shapes: List<ShapeOverlay>,
    imageOverlays: List<ImageOverlay>,
    positionMs: Long,
    isTextTrackVisible: Boolean = true,
    isStickerTrackVisible: Boolean = true,
    isShapeTrackVisible: Boolean = true
): ActivePreviewOverlays {
    return ActivePreviewOverlays(
        textOverlays = if (isTextTrackVisible) {
            textOverlays.filter { positionMs >= it.timelineStartMs && positionMs < it.timelineEndMs }
        } else emptyList(),
        stickers = if (isStickerTrackVisible) {
            stickers.filter { positionMs >= it.timelineStartMs && positionMs < it.timelineEndMs }
        } else emptyList(),
        shapes = if (isShapeTrackVisible) {
            shapes.filter { positionMs >= it.timelineStartMs && positionMs < it.timelineEndMs }
        } else emptyList(),
        imageOverlays = imageOverlays.filter { positionMs >= it.timelineStartMs && positionMs < it.timelineEndMs }
    )
}

private enum class PreviewAnchorMode(val label: String) {
    PLAYHEAD("Playhead"),
    INTRO("Intro (0s)"),
    HIGHLIGHT("Filters & Text")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportPreviewDialog(
    uiState: EditorUiState,
    selectedResolution: ExportResolution,
    selectedFps: Int,
    selectedBitrate: ExportBitrate,
    onDismiss: () -> Unit,
    onConfirmExport: () -> Unit
) {
    val totalDurationMs = uiState.totalDurationMs.coerceAtLeast(1000L)
    val initialMode = remember(uiState.clips, uiState.textOverlays, uiState.currentPositionMs) {
        if (uiState.textOverlays.isNotEmpty() || uiState.clips.any { it.filter != VideoFilter.NONE || it.colorGrading.lut != LutPreset.NONE }) {
            PreviewAnchorMode.HIGHLIGHT
        } else {
            PreviewAnchorMode.PLAYHEAD
        }
    }
    var selectedAnchorMode by remember { mutableStateOf(initialMode) }

    val segment = remember(selectedAnchorMode, totalDurationMs, uiState.currentPositionMs, uiState.clips, uiState.textOverlays) {
        val anchorMs = when (selectedAnchorMode) {
            PreviewAnchorMode.PLAYHEAD -> uiState.currentPositionMs
            PreviewAnchorMode.INTRO -> 0L
            PreviewAnchorMode.HIGHLIGHT -> findHighlightAnchorMs(uiState.clips, uiState.textOverlays)
        }
        computeExportPreviewSegment(
            totalDurationMs = totalDurationMs,
            anchorPositionMs = anchorMs,
            segmentLengthMs = 5000L
        )
    }

    var previewPositionMs by remember(segment) { mutableLongStateOf(segment.startMs) }
    var isPreviewPlaying by remember { mutableStateOf(true) }
    var isLoopEnabled by remember { mutableStateOf(true) }

    // Continuous frame tick loop advancing preview playhead over the short export segment
    LaunchedEffect(isPreviewPlaying, segment.startMs, segment.endMs, isLoopEnabled) {
        if (!isPreviewPlaying) return@LaunchedEffect
        val stepMs = 33L
        while (isActive && isPreviewPlaying) {
            delay(stepMs)
            val next = previewPositionMs + stepMs
            if (next >= segment.endMs) {
                if (isLoopEnabled) {
                    previewPositionMs = segment.startMs
                } else {
                    previewPositionMs = segment.endMs
                    isPreviewPlaying = false
                }
            } else {
                previewPositionMs = next
            }
        }
    }

    val activeClip = remember(uiState.clips, previewPositionMs) {
        uiState.clips.find { previewPositionMs >= it.timelineStartMs && previewPositionMs < it.timelineEndMs }
            ?: uiState.clips.lastOrNull { previewPositionMs >= it.timelineStartMs }
            ?: uiState.clips.firstOrNull()
    }

    val activeOverlays = remember(
        uiState.textOverlays,
        uiState.stickers,
        uiState.shapes,
        uiState.imageOverlays,
        previewPositionMs,
        uiState.isTextTrackVisible,
        uiState.isStickerTrackVisible,
        uiState.isShapeTrackVisible
    ) {
        resolveActiveOverlaysAtTime(
            textOverlays = uiState.textOverlays,
            stickers = uiState.stickers,
            shapes = uiState.shapes,
            imageOverlays = uiState.imageOverlays,
            positionMs = previewPositionMs,
            isTextTrackVisible = uiState.isTextTrackVisible,
            isStickerTrackVisible = uiState.isStickerTrackVisible,
            isShapeTrackVisible = uiState.isShapeTrackVisible
        )
    }

    val activeTransition = remember(uiState.clips, previewPositionMs) {
        TransitionsManager.evaluateTransitionAtTimelineMs(uiState.clips, previewPositionMs)
    }

    val segmentFilters = remember(uiState.clips, segment) {
        uiState.clips
            .filter { it.timelineEndMs > segment.startMs && it.timelineStartMs < segment.endMs }
            .map { clip ->
                buildString {
                    append(clip.filter.displayName)
                    if (clip.colorGrading.lut != LutPreset.NONE) {
                        append(" + ${clip.colorGrading.lut.displayName}")
                    }
                }
            }
            .distinct()
    }

    val segmentTextCount = remember(uiState.textOverlays, segment) {
        uiState.textOverlays.count { it.timelineEndMs > segment.startMs && it.timelineStartMs < segment.endMs }
    }

    val segmentTransitionCount = remember(uiState.clips, segment) {
        uiState.clips.count {
            it.transitionIn != TransitionType.NONE &&
                it.timelineStartMs in segment.startMs..segment.endMs
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = StudioSurfaceDark,
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag("export_preview_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Preview",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedResolution.label.split(" ").first()} • ${selectedFps} FPS • ${selectedBitrate.mbps} Mbps • ${uiState.aspectRatio.label.split(" ").first()}",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("export_preview_close_icon")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Export Preview",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segment Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PreviewAnchorMode.entries.forEach { mode ->
                        val isSelected = selectedAnchorMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else StudioCardDark,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) AccentCyan else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 36.dp)
                                .clickable { selectedAnchorMode = mode }
                                .testTag("export_preview_anchor_${mode.name.lowercase()}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    color = if (isSelected) AccentCyan else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Rendered Video Segment Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF06070B))
                        .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(14.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val canvasRatio = uiState.aspectRatio.aspectRatio
                        Box(
                            modifier = Modifier
                                .aspectRatio(
                                    canvasRatio,
                                    matchHeightConstraintsFirst = maxHeight < maxWidth * (1f / canvasRatio)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF090A0F))
                                .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .clickable { isPreviewPlaying = !isPreviewPlaying }
                                .testTag("export_preview_viewport"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activeClip != null) {
                                CanvasBackgroundProcessor(
                                    clip = activeClip,
                                    canvasAspectRatio = uiState.aspectRatio,
                                    currentPositionMs = previewPositionMs,
                                    activeTextOverlays = activeOverlays.textOverlays,
                                    activeStickers = activeOverlays.stickers,
                                    activeShapes = activeOverlays.shapes,
                                    activeImageOverlays = activeOverlays.imageOverlays,
                                    isStabilizationSplitPreview = false,
                                    isPlaying = isPreviewPlaying,
                                    playbackSpeed = 1.0f,
                                    isVideoTrackMuted = uiState.isVideoTrackMuted,
                                    audioTracks = uiState.audioClips,
                                    isAudioTrackMuted = uiState.isAudioTrackMuted,
                                    isVoiceoverActive = uiState.isVoiceoverActive,
                                    autoDuckingLevel = uiState.autoDuckingDuckLevel,
                                    masterVolume = uiState.masterVolume,
                                    isAutoDuckingEnabled = uiState.isAutoDuckingMasterEnabled
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = "No Clips",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Add a video clip to preview export",
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Top-left live timecode badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xCC090A0F),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (isPreviewPlaying) AccentEmerald else Color(0xFFF59E0B),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${VideoEditorViewModel.formatTime(previewPositionMs)} / ${VideoEditorViewModel.formatTime(segment.endMs)}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Top-right active filter & transition indicator
                            if (activeClip != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xCC1E1B4B),
                                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .testTag("export_preview_filter_badge")
                                ) {
                                    Text(
                                        text = buildString {
                                            append(activeClip.filter.displayName)
                                            if (activeClip.colorGrading.lut != LutPreset.NONE) {
                                                append(" • ${activeClip.colorGrading.lut.displayName}")
                                            }
                                            if (activeTransition != null) {
                                                append(" • ${activeTransition.transitionType.displayName}")
                                            }
                                        },
                                        color = AccentCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrubber + Play / Pause / Loop Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AccentCyan,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                if (!isPreviewPlaying && previewPositionMs >= segment.endMs) {
                                    previewPositionMs = segment.startMs
                                }
                                isPreviewPlaying = !isPreviewPlaying
                            }
                            .testTag("export_preview_play_pause")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPreviewPlaying) "Pause Preview" else "Play Preview",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Slider(
                            value = previewPositionMs.toFloat().coerceIn(
                                segment.startMs.toFloat(),
                                segment.endMs.toFloat().coerceAtLeast(segment.startMs.toFloat() + 1f)
                            ),
                            onValueChange = { pos ->
                                isPreviewPlaying = false
                                previewPositionMs = pos.toLong().coerceIn(segment.startMs, segment.endMs)
                            },
                            valueRange = segment.startMs.toFloat()..segment.endMs.toFloat().coerceAtLeast(segment.startMs.toFloat() + 1f),
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("export_preview_scrubber")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Segment: ${VideoEditorViewModel.formatTime(segment.startMs)} – ${VideoEditorViewModel.formatTime(segment.endMs)}",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format("%.1fs clip window", segment.durationMs / 1000f),
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = { isLoopEnabled = !isLoopEnabled },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("export_preview_loop_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = if (isLoopEnabled) "Loop Enabled" else "Loop Disabled",
                            tint = if (isLoopEnabled) AccentCyan else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Applied Filter & Overlay Summary Pills
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioCardDark,
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (segmentFilters.isEmpty()) "Filter: Original" else segmentFilters.joinToString(" | "),
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioCardDark,
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TextFields, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$segmentTextCount Text Overlays",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (segmentTransitionCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioCardDark,
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "$segmentTransitionCount Transitions",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_preview_close_button")
                    ) {
                        Text("Adjust Settings", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirmExport,
                        enabled = uiState.clips.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier
                            .weight(1.25f)
                            .height(48.dp)
                            .testTag("export_preview_confirm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export MP4", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
