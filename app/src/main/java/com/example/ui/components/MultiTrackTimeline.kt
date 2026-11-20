package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.AudioTrackItem
import com.example.data.model.ImageOverlay
import com.example.data.model.OverlaySticker
import com.example.data.model.SampleVideo
import com.example.data.model.ShapeOverlay
import com.example.data.model.TextOverlay
import com.example.data.model.TimelineCueMarker
import com.example.data.model.TrackType
import com.example.data.model.TransitionType
import com.example.data.model.VideoClip
import com.example.data.model.VisualSnapTarget
import com.example.util.DeviceMediaImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.PlayheadRed
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackOverlayColor
import com.example.ui.theme.TrackTextColor
import com.example.ui.theme.TrackTransitionColor
import com.example.ui.theme.TrackVideoColor
import com.example.viewmodel.VideoEditorViewModel

data class TimelineSyncPoint(
    val timeMs: Long,
    val label: String,
    val trackTypes: List<String>
)

@Composable
fun MultiTrackTimeline(
    clips: List<VideoClip>,
    audioClips: List<AudioTrackItem>,
    textOverlays: List<TextOverlay>,
    stickers: List<OverlaySticker>,
    shapes: List<ShapeOverlay> = emptyList(),
    imageOverlays: List<ImageOverlay> = emptyList(),
    currentPositionMs: Long,
    totalDurationMs: Long,
    selectedClipId: String?,
    selectedAudioId: String?,
    selectedTextId: String?,
    selectedStickerId: String?,
    selectedShapeId: String? = null,
    selectedImageOverlayId: String? = null,
    timelineZoom: Float,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectText: (String) -> Unit,
    onSelectSticker: (String) -> Unit,
    onSelectShape: (String) -> Unit = {},
    onSelectImageOverlay: (String) -> Unit = {},
    onZoomChange: (Float) -> Unit,
    onAddMediaClick: () -> Unit,
    onSelectTransitionClick: (String) -> Unit = {},
    isSnappingEnabled: Boolean = true,
    onToggleSnapping: () -> Unit = {},
    onReorderClips: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onOpenPrecisionScrubber: () -> Unit = {},
    draggingTransition: TransitionType? = null,
    hoveredDropClipId: String? = null,
    onDropTransition: (clipId: String, TransitionType) -> Unit = { _, _ -> },
    onCancelDragTransition: () -> Unit = {},
    onReportTransitionSeamBounds: (clipId: String, Rect) -> Unit = { _, _ -> },
    onClearTransitionSeamBounds: (clipId: String) -> Unit = {},
    timelineMarkers: List<TimelineCueMarker> = emptyList(),
    onAddCueMarker: () -> Unit = {},
    onDeleteCueMarker: (String) -> Unit = {},
    onJumpToMarker: (TimelineCueMarker) -> Unit = {},
    isVideoTrackMuted: Boolean = false,
    isVideoTrackLocked: Boolean = false,
    isAudioTrackMuted: Boolean = false,
    isAudioTrackLocked: Boolean = false,
    isStickerTrackVisible: Boolean = true,
    isStickerTrackLocked: Boolean = false,
    isShapeTrackVisible: Boolean = true,
    isShapeTrackLocked: Boolean = false,
    isTextTrackVisible: Boolean = true,
    isTextTrackLocked: Boolean = false,
    onToggleTrackVisibility: (TrackType) -> Unit = {},
    onToggleTrackLock: (TrackType) -> Unit = {},
    isMediaTrayExpanded: Boolean = false,
    onToggleMediaTray: () -> Unit = {},
    onDropNewClip: (SampleVideo, targetTimeMs: Long) -> Unit = { _, _ -> },
    onUpdateTrim: (clipId: String, inMs: Long, outMs: Long) -> Unit = { _, _, _ -> },
    onUpdateTrimLive: (clipId: String, inMs: Long, outMs: Long) -> Unit = { _, _, _ -> },
    onStepTrim: (clipId: String, deltaInMs: Long, deltaOutMs: Long) -> Unit = { _, _, _ -> },
    onResetTrim: (clipId: String) -> Unit = {},
    isPlaying: Boolean = false,
    onTogglePlay: () -> Unit = {},
    onSeekStart: () -> Unit = {},
    onSeekEnd: () -> Unit = {},
    onQuickSplit: () -> Unit = {},
    onJumpToStart: () -> Unit = {},
    onJumpToEnd: () -> Unit = {},
    onJumpToPrevCut: () -> Unit = {},
    onJumpToNextCut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val verticalTrackScrollState = rememberScrollState()
    var isVerticalTimelineMode by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var activeSnapTarget by remember { mutableStateOf<VisualSnapTarget?>(null) }
    var isDraggingNewClip by remember { mutableStateOf(false) }
    var draggedSample by remember { mutableStateOf<SampleVideo?>(null) }
    var newClipHoverTimeMs by remember { mutableStateOf(0L) }

    val checkSnap: (Long) -> Long = { rawMs ->
        val target = calculateSnapTarget(
            rawTimeMs = rawMs,
            timelineZoom = timelineZoom,
            currentPositionMs = currentPositionMs,
            clips = clips,
            timelineMarkers = timelineMarkers,
            totalDurationMs = totalDurationMs,
            isSnappingEnabled = isSnappingEnabled
        )
        if (target != activeSnapTarget) {
            if (target != null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            activeSnapTarget = target
        }
        target?.timeMs ?: rawMs
    }

    // Pixels per second (scale with zoom)
    // Base 55 dp per second * zoom
    val dpPerSecond = 55.dp * timelineZoom
    val msToDp: (Long) -> Dp = { ms -> dpPerSecond * (ms.toFloat() / 1000f) }

    val timelineWidthDp = maxOf(msToDp(totalDurationMs) + 300.dp, 600.dp)

    // Calculate inter-track synchronization points (cuts, beat markers, text/overlay alignment)
    val syncPoints = remember(clips, audioClips, textOverlays, stickers, shapes) {
        val boundaries = mutableListOf<Pair<Long, String>>()
        clips.forEach {
            boundaries.add(it.timelineStartMs to "Video")
            boundaries.add(it.timelineEndMs to "Video")
        }
        audioClips.forEach {
            boundaries.add(it.timelineStartMs to "Audio")
            boundaries.add(it.timelineEndMs to "Audio")
            if (it.isBeatSyncEnabled) {
                if (it.beatMarkersMs.isNotEmpty()) {
                    it.beatMarkersMs.forEach { marker ->
                        boundaries.add((it.timelineStartMs + marker) to "Beat")
                    }
                } else {
                    var b = it.timelineStartMs
                    while (b <= it.timelineEndMs) {
                        boundaries.add(b to "Beat")
                        b += 500L
                    }
                }
            }
        }
        textOverlays.forEach {
            boundaries.add(it.timelineStartMs to "Text")
            boundaries.add(it.timelineEndMs to "Text")
        }
        stickers.forEach {
            boundaries.add(it.timelineStartMs to "Sticker")
            boundaries.add(it.timelineEndMs to "Sticker")
        }
        shapes.forEach {
            boundaries.add(it.timelineStartMs to "Shape")
            boundaries.add(it.timelineEndMs to "Shape")
        }

        val sorted = boundaries.filter { it.first > 0L }.sortedBy { it.first }
        val points = mutableListOf<TimelineSyncPoint>()
        var cluster = mutableListOf<Pair<Long, String>>()

        for (item in sorted) {
            if (cluster.isEmpty()) {
                cluster.add(item)
            } else {
                val avg = cluster.map { it.first }.average()
                if (kotlin.math.abs(item.first - avg) <= 80.0) {
                    cluster.add(item)
                } else {
                    if (cluster.size >= 2 || cluster.any { it.second == "Beat" }) {
                        val syncMs = cluster.map { it.first }.average().toLong()
                        val tracks = cluster.map { it.second }.distinct()
                        points.add(TimelineSyncPoint(syncMs, tracks.joinToString("+"), tracks))
                    }
                    cluster = mutableListOf(item)
                }
            }
        }
        if (cluster.size >= 2 || cluster.any { it.second == "Beat" }) {
            val syncMs = cluster.map { it.first }.average().toLong()
            val tracks = cluster.map { it.second }.distinct()
            points.add(TimelineSyncPoint(syncMs, tracks.joinToString("+"), tracks))
        }
        points.distinctBy { it.timeMs / 120 }
    }

    // Auto-scroll timeline to keep playhead in view when playing
    LaunchedEffect(currentPositionMs) {
        val playheadPx = with(density) { msToDp(currentPositionMs).toPx() }
        val viewPortStart = scrollState.value
        val viewPortEnd = viewPortStart + 600
        if (playheadPx > viewPortEnd - 100 || playheadPx < viewPortStart) {
            scrollState.animateScrollTo(maxOf(0, (playheadPx - 200).toInt()))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfaceDark)
            .border(1.dp, Color(0xFF1F2937))
    ) {
        // Timeline Header: Tracks Legend, Precision Scrubber, Snapping Magnet & Zoom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrackBadge(title = "Video", color = TrackVideoColor, icon = Icons.Default.Movie)
                TrackBadge(title = "Audio/Beat", color = TrackAudioColor, icon = Icons.Default.GraphicEq)
                TrackBadge(title = "Lottie", color = TrackOverlayColor, icon = Icons.Default.AutoAwesome)
                TrackBadge(title = "Shapes", color = Color(0xFF8B5CF6), icon = Icons.Default.Category)
                TrackBadge(title = "Text", color = TrackTextColor, icon = Icons.Default.TextFields)
            }

            // Controls: Orientation Toggle (Horiz/Vert), Precision Scrubber, Clips Dock, Magnet Snapping & Zoom
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Horizontal / Vertical Timeline Orientation Toggle
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isVerticalTimelineMode) AccentPink.copy(alpha = 0.25f) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isVerticalTimelineMode) AccentPink else AccentCyan.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isVerticalTimelineMode = !isVerticalTimelineMode
                        }
                        .testTag("timeline_orientation_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Horizontal or Vertical Timeline",
                            tint = if (isVerticalTimelineMode) AccentPink else AccentCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isVerticalTimelineMode) "Vert" else "Horiz",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))

                // Quick Frame Scrubber / Jog Dial Access
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenPrecisionScrubber()
                        }
                        .testTag("timeline_precision_scrubber_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Precision Frame Scrubber",
                            tint = AccentCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Scrub",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))

                // Media Clips Dock Toggle
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isMediaTrayExpanded) AccentCyan.copy(alpha = 0.25f) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isMediaTrayExpanded) AccentCyan else Color(0xFF334155)),
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleMediaTray()
                        }
                        .testTag("timeline_media_tray_toggle_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Clips Dock",
                            tint = if (isMediaTrayExpanded) AccentCyan else Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Dock",
                            color = if (isMediaTrayExpanded) AccentCyan else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isMediaTrayExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (isMediaTrayExpanded) AccentCyan else Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))

                // OpenShot Magnet Snapping Toggle
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSnapping()
                    },
                    modifier = Modifier.size(28.dp).testTag("timeline_magnet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Magnet Snapping",
                        tint = if (isSnappingEnabled) AccentCyan else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onZoomChange(timelineZoom - 0.25f)
                    },
                    modifier = Modifier.size(28.dp).testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.ZoomOut, "Zoom Out", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
                Text(
                    text = "${(timelineZoom * 100).toInt()}%",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onZoomChange(timelineZoom + 0.25f)
                    },
                    modifier = Modifier.size(28.dp).testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.ZoomIn, "Zoom In", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Active Transition Drag-and-Drop Guidance Banner
        if (draggingTransition != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF2A153E),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF4081)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .testTag("dragging_transition_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = null,
                            tint = Color(0xFFFF4081),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dragging ${draggingTransition.displayName}: Drop on any clip seam (⧗) to connect",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onCancelDragTransition,
                        modifier = Modifier.size(24.dp).testTag("cancel_drag_transition_button")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Cancel",
                            tint = Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Draggable Media Clips Tray / Quick Media Dock
        if (isMediaTrayExpanded) {
            DraggableMediaClipsTray(
                onDropClip = { sample, _ ->
                    val dropTime = activeSnapTarget?.timeMs ?: newClipHoverTimeMs
                    onDropNewClip(sample, dropTime)
                    activeSnapTarget = null
                    isDraggingNewClip = false
                },
                onQuickAddClip = { sample ->
                    onDropNewClip(sample, clips.lastOrNull()?.timelineEndMs ?: 0L)
                },
                onActiveDragChange = { isDragging, sample, x, y ->
                    isDraggingNewClip = isDragging
                    draggedSample = sample
                    if (isDragging) {
                        val dpPerSec = 55.dp * timelineZoom
                        val pxPerSec = with(density) { dpPerSec.toPx() }
                        val calculatedMs = with(density) {
                            ((x + scrollState.value - 78.dp.toPx()).coerceAtLeast(0f) / pxPerSec * 1000f).toLong()
                        }
                        newClipHoverTimeMs = checkSnap(calculatedMs)
                    } else {
                        activeSnapTarget = null
                    }
                }
            )
        }

        // Contextual Interactive Trimmer Bar when a video clip is selected
        val selectedVideoClip = remember(clips, selectedClipId) { clips.find { it.id == selectedClipId } }
        if (selectedVideoClip != null && !isVideoTrackLocked) {
            InteractiveTimelineTrimmer(
                clip = selectedVideoClip,
                onStepTrim = onStepTrim,
                onResetTrim = onResetTrim
            )
        }

        // 1. Interactive Timeline Mini-Map / Project Overview Navigator Bar
        TimelineMiniMapNavigator(
            clips = clips,
            totalDurationMs = totalDurationMs,
            currentPositionMs = currentPositionMs,
            scrollState = scrollState,
            timelineWidthDp = timelineWidthDp,
            onSeek = { targetMs ->
                val snapped = if (isSnappingEnabled) checkSnap(targetMs) else targetMs
                onSeek(snapped)
            }
        )

        // 2. Timeline Quick Navigation & Transport Bar
        TimelineQuickNavBar(
            currentPositionMs = currentPositionMs,
            totalDurationMs = totalDurationMs,
            isPlaying = isPlaying,
            timelineZoom = timelineZoom,
            isSnappingEnabled = isSnappingEnabled,
            onTogglePlay = onTogglePlay,
            onJumpToStart = onJumpToStart,
            onJumpToEnd = onJumpToEnd,
            onJumpToPrevCut = onJumpToPrevCut,
            onJumpToNextCut = onJumpToNextCut,
            onZoomChange = onZoomChange,
            onToggleSnapping = onToggleSnapping,
            onQuickSplit = onQuickSplit,
            onAddMarker = onAddCueMarker
        )

        // 3. Interactive Timeline Zoom Controls (Smooth Slider + Frame-Accuracy Presets + Scale Readout)
        TimelineInteractiveZoomBar(
            timelineZoom = timelineZoom,
            totalDurationMs = totalDurationMs,
            onZoomChange = onZoomChange
        )

        if (isVerticalTimelineMode) {
            VerticalScrollableMultiTrackTimeline(
                clips = clips,
                audioClips = audioClips,
                textOverlays = textOverlays,
                currentPositionMs = currentPositionMs,
                totalDurationMs = totalDurationMs,
                selectedClipId = selectedClipId,
                selectedAudioId = selectedAudioId,
                onSeek = onSeek,
                onSelectClip = onSelectClip,
                onSelectAudio = onSelectAudio,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            // Main Multi-Track Container: Pinned Left Track Headers + Scrollable Lanes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 1. PINNED STATIONARY TRACK HEADERS COLUMN
                TrackHeadersColumn(
                    isVideoMuted = isVideoTrackMuted,
                    isVideoLocked = isVideoTrackLocked,
                    isAudioMuted = isAudioTrackMuted,
                    isAudioLocked = isAudioTrackLocked,
                    isStickerVisible = isStickerTrackVisible,
                    isStickerLocked = isStickerTrackLocked,
                    isShapeVisible = isShapeTrackVisible,
                    isShapeLocked = isShapeTrackLocked,
                    isTextVisible = isTextTrackVisible,
                    isTextLocked = isTextTrackLocked,
                    hasShapes = shapes.isNotEmpty(),
                    hasImages = imageOverlays.isNotEmpty(),
                    onToggleVisibility = onToggleTrackVisibility,
                    onToggleLock = onToggleTrackLock,
                    onAddCueMarker = onAddCueMarker,
                    modifier = Modifier
                        .width(78.dp)
                        .fillMaxHeight()
                )

                // Stationary vertical divider between headers and track lanes
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF23293D))
                )

                // 2. HORIZONTALLY & VERTICALLY SCROLLABLE TRACK LANES
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(scrollState)
                        .testTag("multi_track_timeline_scroll")
                ) {
                    // Visual Snapping Laser Overlay & Pill Badge
                    if (activeSnapTarget != null) {
                        VisualSnapOverlay(
                            snapTarget = activeSnapTarget!!,
                            msToDp = msToDp,
                            modifier = Modifier.fillMaxHeight()
                        )
                    }

                    // Track Synchronization Visual Guides Layer across all tracks
                    TrackSyncOverlay(
                        syncPoints = syncPoints,
                        msToDp = msToDp,
                        currentPositionMs = currentPositionMs,
                        timelineWidthDp = timelineWidthDp
                    )

                    Column(
                        modifier = Modifier
                            .width(timelineWidthDp)
                            .fillMaxHeight()
                            .verticalScroll(verticalTrackScrollState)
                            .padding(bottom = 6.dp)
                    ) {
                        // 1. Timecode Ruler with tick marks & Cue Markers
                        TimelineRuler(
                            totalDurationMs = totalDurationMs,
                            msToDp = msToDp,
                            timelineMarkers = timelineMarkers,
                            onSeek = { rawMs ->
                                val snapped = if (isSnappingEnabled) checkSnap(rawMs) else rawMs
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSeek(snapped)
                            },
                            onMarkerClick = { marker ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onJumpToMarker(marker)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 2. Main Video Sequence Lane (Track 1)
                        VideoTrackLane(
                            clips = clips,
                            selectedClipId = selectedClipId,
                            msToDp = msToDp,
                            isLocked = isVideoTrackLocked,
                            isMuted = isVideoTrackMuted,
                            onSelectClip = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectClip(it)
                            },
                            onAddMediaClick = onAddMediaClick,
                            onSelectTransitionClick = onSelectTransitionClick,
                            onReorderClips = onReorderClips,
                            draggingTransition = draggingTransition,
                            hoveredDropClipId = hoveredDropClipId,
                            onDropTransition = onDropTransition,
                            onReportTransitionSeamBounds = onReportTransitionSeamBounds,
                            onClearTransitionSeamBounds = onClearTransitionSeamBounds,
                            onCheckSnap = checkSnap,
                            onClearSnap = { activeSnapTarget = null },
                            onUpdateTrim = onUpdateTrim,
                            onUpdateTrimLive = onUpdateTrimLive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 3. Audio & Beat Sync Lane (Track 2)
                        AudioTrackLane(
                            audioClips = audioClips,
                            selectedAudioId = selectedAudioId,
                            msToDp = msToDp,
                            isLocked = isAudioTrackLocked,
                            isMuted = isAudioTrackMuted,
                            onSelectAudio = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectAudio(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 4. Lottie / Vector Overlays Lane (Track 3)
                        StickerTrackLane(
                            stickers = stickers,
                            selectedStickerId = selectedStickerId,
                            msToDp = msToDp,
                            isLocked = isStickerTrackLocked,
                            isVisible = isStickerTrackVisible,
                            onSelectSticker = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectSticker(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 5. Shapes Overlays Lane (Track 4)
                        if (shapes.isNotEmpty()) {
                            ShapeTrackLane(
                                shapes = shapes,
                                selectedShapeId = selectedShapeId,
                                msToDp = msToDp,
                                isLocked = isShapeTrackLocked,
                                isVisible = isShapeTrackVisible,
                                onSelectShape = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectShape(it)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // 5b. Photo / Image Overlays Lane
                        if (imageOverlays.isNotEmpty()) {
                            ImageOverlayTrackLane(
                                imageOverlays = imageOverlays,
                                selectedImageOverlayId = selectedImageOverlayId,
                                msToDp = msToDp,
                                onSelectImageOverlay = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectImageOverlay(it)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // 6. Text Overlays Lane (Track 5)
                        TextTrackLane(
                            textOverlays = textOverlays,
                            selectedTextId = selectedTextId,
                            msToDp = msToDp,
                            isLocked = isTextTrackLocked,
                            isVisible = isTextTrackVisible,
                            onSelectText = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectText(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }

                    // Scrubber / Playhead Line that spans vertically across all tracks
                    val playheadOffset = msToDp(currentPositionMs)
                    val isClipUnderPlayhead = remember(clips, currentPositionMs) {
                        clips.any { currentPositionMs >= it.timelineStartMs && currentPositionMs < it.timelineEndMs }
                    }
                    PlayheadScrubber(
                        offsetDp = playheadOffset,
                        currentPositionMs = currentPositionMs,
                        totalDurationMs = totalDurationMs,
                        timelineZoom = timelineZoom,
                        msToDp = msToDp,
                        onSeek = onSeek,
                        onSeekStart = onSeekStart,
                        onSeekEnd = onSeekEnd,
                        onQuickSplit = onQuickSplit,
                        isClipUnderPlayhead = isClipUnderPlayhead,
                        onCheckSnap = if (isSnappingEnabled) checkSnap else null,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalScrollableMultiTrackTimeline(
    clips: List<VideoClip>,
    audioClips: List<AudioTrackItem>,
    textOverlays: List<TextOverlay>,
    currentPositionMs: Long,
    totalDurationMs: Long,
    selectedClipId: String?,
    selectedAudioId: String?,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSelectAudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val safeTotalMs = totalDurationMs.coerceAtLeast(1000L)

    Column(
        modifier = modifier
            .background(Color(0xFF0B1120))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("vertical_timeline_scrollable_container")
    ) {
        // Master Playhead Scrubber Slider at Top of Vertical Timeline
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SCRUB",
                color = AccentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Slider(
                value = currentPositionMs.toFloat().coerceIn(0f, safeTotalMs.toFloat()),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..safeTotalMs.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = PlayheadRed,
                    activeTrackColor = AccentCyan
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .testTag("vertical_timeline_scrubber_slider")
            )
            Text(
                text = "${String.format("%.1f", currentPositionMs / 1000f)}s / ${String.format("%.1f", safeTotalMs / 1000f)}s",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Vertically Scrollable Video & Audio Track Stack
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(verticalScroll)
                .testTag("vertical_timeline_tracks_list"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "VIDEO TRACKS (${clips.size} CLIPS)",
                color = TrackVideoColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            clips.forEachIndexed { idx, clip ->
                val isSelected = clip.id == selectedClipId
                val isAtPlayhead = currentPositionMs in clip.timelineStartMs..clip.timelineEndMs
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF1E293B) else StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected || isAtPlayhead) 1.5.dp else 1.dp,
                        color = when {
                            isSelected -> AccentCyan
                            isAtPlayhead -> PlayheadRed
                            else -> Color(0xFF334155)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectClip(clip.id)
                            onSeek(clip.timelineStartMs)
                        }
                        .testTag("vertical_clip_item_${clip.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = TrackVideoColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${idx + 1}. ${clip.name}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${String.format("%.1f", clip.timelineStartMs / 1000f)}s - ${String.format("%.1f", clip.timelineEndMs / 1000f)}s • Filter: ${clip.filter.displayName}",
                                    color = AccentCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        if (isAtPlayhead) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PlayheadRed
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (audioClips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AUDIO & VOICEOVER TRACKS (${audioClips.size})",
                    color = TrackAudioColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                audioClips.forEach { audio ->
                    val isSelected = audio.id == selectedAudioId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF1E293B) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentCyan else TrackAudioColor.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectAudio(audio.id)
                                onSeek(audio.timelineStartMs)
                            }
                            .testTag("vertical_audio_item_${audio.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = TrackAudioColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = audio.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${audio.category.name} • Vol ${(audio.volume * 100).toInt()}% • ${if (audio.isDuckingEnabled) "Auto-Duck ON" else "Standard"}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(
                                text = "${String.format("%.1f", audio.durationMs / 1000f)}s",
                                color = TrackAudioColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TrackBadge(title: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(11.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = title, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TrackSyncOverlay(
    syncPoints: List<TimelineSyncPoint>,
    msToDp: (Long) -> Dp,
    currentPositionMs: Long,
    timelineWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.width(timelineWidthDp).fillMaxHeight()) {
        syncPoints.forEach { syncPoint ->
            val xDp = msToDp(syncPoint.timeMs)
            val isPlayheadNear = kotlin.math.abs(currentPositionMs - syncPoint.timeMs) <= 80L
            val color = if (isPlayheadNear) AccentCyan else Color(0x6600E5FF)

            // Vertical dashed sync line spanning across all track lanes
            Canvas(
                modifier = Modifier
                    .offset(x = xDp)
                    .width(1.dp)
                    .fillMaxHeight()
            ) {
                drawLine(
                    color = color,
                    start = Offset(0f, 28.dp.toPx()),
                    end = Offset(0f, size.height),
                    strokeWidth = if (isPlayheadNear) 2.5f else 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // Sync Badge at the top ruler
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = if (isPlayheadNear) AccentCyan else Color(0xDD0E1E2E),
                border = androidx.compose.foundation.BorderStroke(1.dp, color),
                modifier = Modifier.offset(x = (xDp - 14.dp).coerceAtLeast(0.dp), y = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "⚡SYNC",
                        color = if (isPlayheadNear) Color.Black else AccentCyan,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineRuler(
    totalDurationMs: Long,
    msToDp: (Long) -> Dp,
    timelineMarkers: List<TimelineCueMarker> = emptyList(),
    onSeek: (Long) -> Unit,
    onMarkerClick: (TimelineCueMarker) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .background(Color(0xFF141222))
            .pointerInput(totalDurationMs) {
                detectTapGestures { offset ->
                    val clickedMs = with(density) {
                        val dpPerSec = msToDp(1000L).toPx()
                        ((offset.x / dpPerSec) * 1000L).toLong()
                    }
                    onSeek(clickedMs)
                }
            }
            .pointerInput(totalDurationMs) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val draggedMs = with(density) {
                        val dpPerSec = msToDp(1000L).toPx()
                        ((change.position.x / dpPerSec) * 1000L).toLong()
                    }
                    onSeek(draggedMs)
                }
            }
            .testTag("timeline_ruler")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalSeconds = (totalDurationMs / 1000L).toInt() + 10
            for (sec in 0..totalSeconds) {
                val secMs = sec * 1000L
                val x = msToDp(secMs).toPx()

                // Major second tick
                drawLine(
                    color = Color.White.copy(alpha = 0.65f),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - 10f),
                    strokeWidth = 1.5f
                )

                // Half second tick
                val halfX = msToDp(secMs + 500L).toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(halfX, size.height),
                    end = Offset(halfX, size.height - 6f),
                    strokeWidth = 1f
                )

                // Sub-frame quarter second ticks
                val q1X = msToDp(secMs + 250L).toPx()
                val q3X = msToDp(secMs + 750L).toPx()
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(q1X, size.height),
                    end = Offset(q1X, size.height - 4f),
                    strokeWidth = 0.8f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.18f),
                    start = Offset(q3X, size.height),
                    end = Offset(q3X, size.height - 4f),
                    strokeWidth = 0.8f
                )
            }
        }

        // Draw second timestamp numbers
        val totalSeconds = (totalDurationMs / 1000L).toInt() + 5
        for (sec in 0..totalSeconds step 2) {
            val xDp = msToDp(sec * 1000L)
            Text(
                text = "${sec}s",
                color = Color.LightGray.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .offset(x = xDp + 4.dp, y = 2.dp)
            )
        }

        // Interactive Cue Markers on Ruler
        timelineMarkers.forEach { marker ->
            val markerX = msToDp(marker.timeMs)
            val markerColor = Color(marker.colorHex)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.dp, markerColor),
                modifier = Modifier
                    .offset(x = (markerX - 10.dp).coerceAtLeast(0.dp), y = 2.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMarkerClick(marker)
                    }
                    .testTag("timeline_cue_marker_${marker.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = marker.label,
                        tint = markerColor,
                        modifier = Modifier.size(9.dp)
                    )
                    Text(
                        text = marker.label,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun VideoTrackLane(
    clips: List<VideoClip>,
    selectedClipId: String?,
    msToDp: (Long) -> Dp,
    onSelectClip: (String) -> Unit,
    onAddMediaClick: () -> Unit,
    onSelectTransitionClick: (String) -> Unit = {},
    onReorderClips: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    draggingTransition: TransitionType? = null,
    hoveredDropClipId: String? = null,
    onDropTransition: (clipId: String, TransitionType) -> Unit = { _, _ -> },
    onReportTransitionSeamBounds: (clipId: String, Rect) -> Unit = { _, _ -> },
    onClearTransitionSeamBounds: (clipId: String) -> Unit = {},
    isLocked: Boolean = false,
    isMuted: Boolean = false,
    onCheckSnap: ((Long) -> Long)? = null,
    onClearSnap: (() -> Unit)? = null,
    onUpdateTrim: (clipId: String, inMs: Long, outMs: Long) -> Unit = { _, _, _ -> },
    onUpdateTrimLive: (clipId: String, inMs: Long, outMs: Long) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var targetDropIndex by remember { mutableStateOf<Int?>(null) }

    val clipWidthsPx = clips.map { clip ->
        with(density) { msToDp(clip.trimmedDurationMs).coerceAtLeast(44.dp).toPx() }
    }

    Box(
        modifier = modifier
            .background(Color(0xFF0F0E17))
            .border(1.dp, Color(0xFF1B182B))
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            clips.forEachIndexed { index, clip ->
                val clipWidth = msToDp(clip.trimmedDurationMs).coerceAtLeast(44.dp)
                val isSelected = clip.id == selectedClipId
                val isBeingDragged = draggingIndex == index
                val isHoveredIntersection = hoveredDropClipId == clip.id

                DisposableEffect(clip.id) {
                    onDispose {
                        onClearTransitionSeamBounds(clip.id)
                    }
                }

                // Dedicated drop seam at clip intersections when a transition is being dragged
                if (draggingTransition != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isHoveredIntersection) AccentEmerald else Color(0xFF6B21A8),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isHoveredIntersection) 2.5.dp else 1.5.dp,
                            color = if (isHoveredIntersection) Color.White else Color(0xFFFF4081)
                        ),
                        modifier = Modifier
                            .width(if (isHoveredIntersection) 34.dp else 26.dp)
                            .fillMaxHeight(0.88f)
                            .onGloballyPositioned { coords ->
                                onReportTransitionSeamBounds(clip.id, coords.boundsInRoot())
                            }
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDropTransition(clip.id, draggingTransition)
                            }
                            .testTag("transition_seam_drop_${clip.id}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isHoveredIntersection) "⇣" else "⧗",
                                color = if (isHoveredIntersection) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (index == 0) "In" else "Drop",
                                color = if (isHoveredIntersection) Color.Black else Color.White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Visual drop target insertion indicator before this clip
                if (draggingIndex != null && targetDropIndex == index && draggingIndex != index) {
                    DropTargetIndicator()
                }

                Box(
                    modifier = Modifier
                        .then(
                            if (isBeingDragged) {
                                Modifier
                                    .zIndex(100f)
                                    .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
                                    .graphicsLayer {
                                        scaleX = 1.08f
                                        scaleY = 1.08f
                                        shadowElevation = 16f
                                    }
                            } else {
                                Modifier.zIndex(1f)
                            }
                        )
                        .width(clipWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 1.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isBeingDragged) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        AccentCyan.copy(alpha = 0.95f),
                                        Color(clip.accentColorHex).copy(alpha = 0.85f)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(clip.accentColorHex),
                                        Color(clip.accentColorHex).copy(alpha = 0.7f)
                                    )
                                )
                            }
                        )
                        .border(
                            width = if (isBeingDragged) 3.dp else if (isSelected) 2.5.dp else 1.dp,
                            color = if (isBeingDragged) AccentCyan else if (isSelected) AccentCyan else Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .then(
                            if (!isLocked) {
                                Modifier.pointerInput(clip.id, index) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffsetPx = 0f
                                            targetDropIndex = index
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onSelectClip(clip.id)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetPx += dragAmount.x
                                            val curIdx = draggingIndex ?: return@detectDragGesturesAfterLongPress
                                            var origStart = 0f
                                            for (i in 0 until curIdx) {
                                                origStart += clipWidthsPx.getOrElse(i) { 100f }
                                            }
                                            val curCenter = origStart + (clipWidthsPx.getOrNull(curIdx) ?: 100f) / 2f + dragOffsetPx
                                            val pxPerSec = with(density) { msToDp(1000L).toPx() }
                                            val curCenterMs = if (pxPerSec > 0f) ((curCenter / pxPerSec) * 1000f).toLong().coerceAtLeast(0L) else 0L
                                            onCheckSnap?.invoke(curCenterMs)

                                            var runStart = 0f
                                            var target = 0
                                            for (i in clips.indices) {
                                                val w = clipWidthsPx.getOrElse(i) { 100f }
                                                if (curCenter > runStart + w / 2f) {
                                                    target = i
                                                }
                                                runStart += w
                                            }
                                            val newTarget = target.coerceIn(0, clips.lastIndex)
                                            if (newTarget != targetDropIndex) {
                                                targetDropIndex = newTarget
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        },
                                        onDragEnd = {
                                            val from = draggingIndex
                                            val to = targetDropIndex
                                            if (from != null && to != null && from != to) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onReorderClips(from, to)
                                            }
                                            onClearSnap?.invoke()
                                            draggingIndex = null
                                            dragOffsetPx = 0f
                                            targetDropIndex = null
                                        },
                                        onDragCancel = {
                                            onClearSnap?.invoke()
                                            draggingIndex = null
                                            dragOffsetPx = 0f
                                            targetDropIndex = null
                                        }
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onSelectClip(clip.id) }
                        .testTag("clip_item_${clip.id}")
                ) {
                    if (isMuted) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = Color(0xCCEF4444)
                            ) {
                                Text(
                                    text = "MUTED",
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Track Locked",
                            tint = Color(0xFFFBBF24).copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(3.dp)
                                .size(12.dp)
                        )
                    }
                    if (isBeingDragged) {
                        // Prominent reordering badge when elevated
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "✥ REORDERING",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Drop to swap slot",
                                    color = AccentCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        val isRealClip = !clip.uri.isNullOrBlank() && !clip.uri.startsWith("sample://")
                        val filmstripFrames by produceState<List<android.graphics.Bitmap>>(
                            initialValue = emptyList(),
                            key1 = clip.uri,
                            key2 = clip.trimInMs,
                            key3 = clip.trimOutMs
                        ) {
                            if (isRealClip) {
                                value = withContext(Dispatchers.IO) {
                                    DeviceMediaImporter.extractTimelineThumbnails(
                                        context = context,
                                        fileOrUri = clip.uri,
                                        trimInMs = clip.trimInMs,
                                        trimOutMs = clip.trimOutMs,
                                        count = 4
                                    )
                                }
                            } else {
                                value = emptyList()
                            }
                        }

                        if (filmstripFrames.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 8.dp)
                            ) {
                                filmstripFrames.forEach { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f))
                            )
                        }

                        // Top 35mm filmstrip sprocket perforations
                        FilmstripPerforations(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        // Visual simulated audio waveform inside video clip
                        ClipWaveformPreview(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                        )

                        // Bottom 35mm filmstrip sprocket perforations
                        FilmstripPerforations(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        // Standard film strip layout with name, timecode, and transition badge
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = if (isSelected) 14.dp else 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = clip.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${VideoEditorViewModel.formatTime(clip.trimmedDurationMs)} • ${clip.speed}x",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (clip.trimInMs > 0L || clip.trimOutMs < clip.sourceDurationMs) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = AccentCyan.copy(alpha = 0.35f),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentCyan)
                                        ) {
                                            Text(
                                                text = "✂",
                                                color = AccentCyan,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 3.dp)
                                            )
                                        }
                                    }
                                    if (clip.hasCrop) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = Color(0xCC0EA5E9)
                                        ) {
                                            Text(
                                                text = "CROP",
                                                color = Color.White,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 3.dp)
                                            )
                                        }
                                    }
                                    if (clip.resizeWidth > 0 && clip.resizeHeight > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(2.dp),
                                            color = Color(0xCC6366F1)
                                        ) {
                                            Text(
                                                text = "${clip.resizeWidth}×${clip.resizeHeight}",
                                                color = Color.White,
                                                fontSize = 7.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(horizontal = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Transition badge: clickable to configure transition or drop target for dragged transition
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isHoveredIntersection -> AccentEmerald
                                    draggingTransition != null -> AccentPink
                                    clip.transitionIn != TransitionType.NONE -> TrackTransitionColor
                                    else -> Color.Black.copy(alpha = 0.5f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    if (draggingTransition != null) 2.dp else 1.dp,
                                    if (draggingTransition != null || clip.transitionIn != TransitionType.NONE) Color.White else Color.Gray
                                ),
                                modifier = Modifier
                                    .size(if (draggingTransition != null) 24.dp else 20.dp)
                                    .onGloballyPositioned { coords ->
                                        if (draggingTransition == null) {
                                            onReportTransitionSeamBounds(clip.id, coords.boundsInRoot())
                                        }
                                    }
                                    .clickable {
                                        if (draggingTransition != null) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDropTransition(clip.id, draggingTransition)
                                        } else {
                                            onSelectClip(clip.id)
                                            onSelectTransitionClick(clip.id)
                                        }
                                    }
                                    .testTag("transition_badge_${clip.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (draggingTransition != null) "⇣" else when (clip.transitionIn) {
                                            TransitionType.NONE -> "+"
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
                                        fontSize = if (draggingTransition != null) 11.sp else 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Interactive touch-friendly Trim Handles on clip edges when selected
                    if (isSelected && !isBeingDragged) {
                        ClipTrimHandleStart(
                            clip = clip,
                            msToDp = msToDp,
                            isLocked = isLocked,
                            onUpdateTrimLive = onUpdateTrimLive,
                            onUpdateTrim = onUpdateTrim,
                            onCheckSnap = onCheckSnap,
                            onClearSnap = onClearSnap
                        )
                        ClipTrimHandleEnd(
                            clip = clip,
                            msToDp = msToDp,
                            isLocked = isLocked,
                            onUpdateTrimLive = onUpdateTrimLive,
                            onUpdateTrim = onUpdateTrim,
                            onCheckSnap = onCheckSnap,
                            onClearSnap = onClearSnap
                        )
                    }
                }
            }

            // Quick "+" Add Clip Button at the end of video sequence
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = StudioCardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383552)),
                modifier = Modifier
                    .size(width = 54.dp, height = 54.dp)
                    .padding(horizontal = 4.dp)
                    .clickable { onAddMediaClick() }
                    .testTag("add_clip_timeline_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Clip",
                        tint = AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BoxScope.ClipTrimHandleStart(
    clip: VideoClip,
    msToDp: (Long) -> Dp,
    isLocked: Boolean,
    onUpdateTrimLive: (clipId: String, inMs: Long, outMs: Long) -> Unit,
    onUpdateTrim: (clipId: String, inMs: Long, outMs: Long) -> Unit,
    onCheckSnap: ((Long) -> Long)?,
    onClearSnap: (() -> Unit)?
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var currentInMs by remember(clip.id, clip.trimInMs) { mutableLongStateOf(clip.trimInMs) }
    var dragAccumPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .width(36.dp)
            .fillMaxHeight()
            .testTag("trim_handle_start_${clip.id}")
            .then(
                if (!isLocked) {
                    Modifier.pointerInput(clip.id, clip.sourceDurationMs) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragAccumPx = 0f
                                currentInMs = clip.trimInMs
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumPx += dragAmount.x
                                val pxPerSec = with(density) { msToDp(1000L).toPx() }
                                val pxPerMs = if (pxPerSec > 0f) pxPerSec / 1000f else 0.05f
                                val deltaMs = (dragAccumPx / pxPerMs).toLong()
                                var candidateIn = (clip.trimInMs + deltaMs).coerceIn(0L, clip.trimOutMs - 200L)

                                if (onCheckSnap != null) {
                                    val candidateTimelineStart = clip.timelineStartMs + (candidateIn - clip.trimInMs)
                                    val snappedTimeline = onCheckSnap(candidateTimelineStart)
                                    val snapDelta = snappedTimeline - candidateTimelineStart
                                    candidateIn = (candidateIn + snapDelta).coerceIn(0L, clip.trimOutMs - 200L)
                                }

                                if (candidateIn != currentInMs) {
                                    currentInMs = candidateIn
                                    onUpdateTrimLive(clip.id, candidateIn, clip.trimOutMs)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                onUpdateTrim(clip.id, currentInMs, clip.trimOutMs)
                                onClearSnap?.invoke()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragCancel = {
                                isDragging = false
                                onClearSnap?.invoke()
                            }
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Visual Bracket Handle '['
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp),
            color = if (isDragging) Color(0xFF00E5FF) else AccentCyan,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
            modifier = Modifier
                .width(13.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(2.dp)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(1.dp))
                    )
                    if (it < 2) Spacer(modifier = Modifier.height(3.dp))
                }
            }
        }

        // Live Floating Tooltip HUD during drag
        if (isDragging) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xEE090D16),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                modifier = Modifier
                    .offset(x = 16.dp, y = (-26).dp)
                    .zIndex(200f)
                    .testTag("trim_tooltip_in")
            ) {
                val deltaS = (currentInMs - clip.trimInMs).toFloat() / 1000f
                Text(
                    text = "IN: ${VideoEditorViewModel.formatTime(currentInMs)} (${if (deltaS >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", deltaS)}s)",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun BoxScope.ClipTrimHandleEnd(
    clip: VideoClip,
    msToDp: (Long) -> Dp,
    isLocked: Boolean,
    onUpdateTrimLive: (clipId: String, inMs: Long, outMs: Long) -> Unit,
    onUpdateTrim: (clipId: String, inMs: Long, outMs: Long) -> Unit,
    onCheckSnap: ((Long) -> Long)?,
    onClearSnap: (() -> Unit)?
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var currentOutMs by remember(clip.id, clip.trimOutMs) { mutableLongStateOf(clip.trimOutMs) }
    var dragAccumPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(36.dp)
            .fillMaxHeight()
            .testTag("trim_handle_end_${clip.id}")
            .then(
                if (!isLocked) {
                    Modifier.pointerInput(clip.id, clip.sourceDurationMs) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragAccumPx = 0f
                                currentOutMs = clip.trimOutMs
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumPx += dragAmount.x
                                val pxPerSec = with(density) { msToDp(1000L).toPx() }
                                val pxPerMs = if (pxPerSec > 0f) pxPerSec / 1000f else 0.05f
                                val deltaMs = (dragAccumPx / pxPerMs).toLong()
                                var candidateOut = (clip.trimOutMs + deltaMs).coerceIn(clip.trimInMs + 200L, clip.sourceDurationMs)

                                if (onCheckSnap != null) {
                                    val candidateTimelineEnd = clip.timelineStartMs + (candidateOut - clip.trimInMs)
                                    val snappedTimeline = onCheckSnap(candidateTimelineEnd)
                                    val snapDelta = snappedTimeline - candidateTimelineEnd
                                    candidateOut = (candidateOut + snapDelta).coerceIn(clip.trimInMs + 200L, clip.sourceDurationMs)
                                }

                                if (candidateOut != currentOutMs) {
                                    currentOutMs = candidateOut
                                    onUpdateTrimLive(clip.id, clip.trimInMs, candidateOut)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                onUpdateTrim(clip.id, clip.trimInMs, currentOutMs)
                                onClearSnap?.invoke()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragCancel = {
                                isDragging = false
                                onClearSnap?.invoke()
                            }
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Visual Bracket Handle ']'
        Surface(
            shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp),
            color = if (isDragging) Color(0xFFFF4081) else AccentCyan,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
            modifier = Modifier
                .width(13.dp)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(2.dp)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(1.dp))
                    )
                    if (it < 2) Spacer(modifier = Modifier.height(3.dp))
                }
            }
        }

        // Live Floating Tooltip HUD during drag
        if (isDragging) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xEE090D16),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentPink),
                modifier = Modifier
                    .offset(x = (-16).dp, y = (-26).dp)
                    .zIndex(200f)
                    .testTag("trim_tooltip_out")
            ) {
                val deltaS = (currentOutMs - clip.trimOutMs).toFloat() / 1000f
                Text(
                    text = "OUT: ${VideoEditorViewModel.formatTime(currentOutMs)} (${if (deltaS >= 0) "+" else ""}${String.format(java.util.Locale.US, "%.1f", deltaS)}s)",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FilmstripPerforations(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.height(3.dp)) {
        val holeWidth = 4.dp.toPx()
        val holeHeight = 2.5.dp.toPx()
        val spacing = 8.dp.toPx()
        val count = (size.width / (holeWidth + spacing)).toInt().coerceAtLeast(1)
        for (i in 0 until count) {
            val left = i * (holeWidth + spacing) + spacing / 2f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(holeWidth, holeHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
            )
        }
    }
}

@Composable
fun ClipWaveformPreview(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val barWidth = 2.dp.toPx()
        val gap = 3.dp.toPx()
        val count = (size.width / (barWidth + gap)).toInt().coerceAtLeast(1)
        val midY = size.height / 2f
        for (i in 0 until count) {
            val factor = ((kotlin.math.sin(i * 0.6) * 0.35 + kotlin.math.cos(i * 1.1) * 0.25 + 0.35).toFloat()).coerceIn(0.15f, 0.95f)
            val barH = size.height * factor * 0.75f
            val x = i * (barWidth + gap)
            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(x, midY - barH / 2f),
                end = Offset(x, midY + barH / 2f),
                strokeWidth = barWidth
            )
        }
    }
}

@Composable
fun InteractiveTimelineTrimmer(
    clip: VideoClip,
    onStepTrim: (clipId: String, deltaInMs: Long, deltaOutMs: Long) -> Unit,
    onResetTrim: (clipId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF13111E),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("interactive_timeline_trimmer_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Head (Start In-point) step buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = AccentCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan)
                ) {
                    Text(
                        text = "IN: ${VideoEditorViewModel.formatTime(clip.trimInMs)}",
                        color = AccentCyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = StudioCardDark,
                    modifier = Modifier
                        .clickable { onStepTrim(clip.id, -100L, 0L) }
                        .testTag("trimmer_step_in_minus")
                ) {
                    Text(
                        text = "-0.1s",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = StudioCardDark,
                    modifier = Modifier
                        .clickable { onStepTrim(clip.id, +100L, 0L) }
                        .testTag("trimmer_step_in_plus")
                ) {
                    Text(
                        text = "+0.1s",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Center Duration and Reset
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Dur: ${VideoEditorViewModel.formatTime(clip.trimmedDurationMs)} / ${VideoEditorViewModel.formatTime(clip.sourceDurationMs)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (clip.trimInMs > 0L || clip.trimOutMs < clip.sourceDurationMs) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFF332040),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentPink),
                        modifier = Modifier
                            .clickable { onResetTrim(clip.id) }
                            .testTag("trimmer_reset_button")
                    ) {
                        Text(
                            text = "↺ Reset",
                            color = AccentPink,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Tail (End Out-point) step buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = StudioCardDark,
                    modifier = Modifier
                        .clickable { onStepTrim(clip.id, 0L, -100L) }
                        .testTag("trimmer_step_out_minus")
                ) {
                    Text(
                        text = "-0.1s",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = StudioCardDark,
                    modifier = Modifier
                        .clickable { onStepTrim(clip.id, 0L, +100L) }
                        .testTag("trimmer_step_out_plus")
                ) {
                    Text(
                        text = "+0.1s",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = AccentPink.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentPink)
                ) {
                    Text(
                        text = "OUT: ${VideoEditorViewModel.formatTime(clip.trimOutMs)}",
                        color = AccentPink,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DropTargetIndicator() {
    Box(
        modifier = Modifier
            .width(14.dp)
            .fillMaxHeight()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(AccentCyan, RoundedCornerShape(2.dp))
        )
        Text(
            text = "▼",
            color = AccentCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Text(
            text = "▲",
            color = AccentCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun AudioTrackLane(
    audioClips: List<AudioTrackItem>,
    selectedAudioId: String?,
    msToDp: (Long) -> Dp,
    onSelectAudio: (String) -> Unit,
    isLocked: Boolean = false,
    isMuted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF191206))
            .border(1.dp, Color(0xFF332008))
    ) {
        audioClips.forEach { audio ->
            val startDp = msToDp(audio.timelineStartMs)
            val widthDp = msToDp(audio.durationMs).coerceAtLeast(44.dp)
            val isSelected = audio.id == selectedAudioId

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFD97706), Color(0xFFF59E0B))
                        )
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color.White else Color(0xFFFBBF24),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectAudio(audio.id) }
                    .testTag("audio_item_${audio.id}")
            ) {
                // Rendered Audio Waveform Lines
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    val step = 5f
                    var x = 2f
                    var i = 0
                    while (x < size.width - 2f) {
                        val midY = size.height / 2f
                        val waveAmp = ((Math.sin(i * 0.45).toFloat() + 1f) * 0.4f + 0.2f) * (size.height * 0.4f)
                        drawLine(
                            color = Color(0xFFFFFBEB).copy(alpha = 0.85f),
                            start = Offset(x, midY - waveAmp),
                            end = Offset(x, midY + waveAmp),
                            strokeWidth = 2.5f
                        )
                        x += step
                        i++
                    }
                }

                // Rendered Beat Sync Vertical Snap Markers
                if (audio.beatMarkersMs.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        audio.beatMarkersMs.forEach { beatMs ->
                            if (beatMs >= audio.timelineStartMs && beatMs <= audio.timelineEndMs) {
                                val relMs = beatMs - audio.timelineStartMs
                                val beatPx = (relMs.toFloat() / audio.durationMs.toFloat()) * size.width
                                drawLine(
                                    color = Color(0xFFFEF08A),
                                    start = Offset(beatPx, 0f),
                                    end = Offset(beatPx, size.height),
                                    strokeWidth = 2f
                                )
                                drawCircle(
                                    color = Color(0xFFF59E0B),
                                    radius = 3f,
                                    center = Offset(beatPx, 4f)
                                )
                            }
                        }
                    }
                }

                // Audio Info & Badges
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (audio.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Audio",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = audio.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (audio.isBeatSyncEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = "BEAT SNAP",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${(audio.volume * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TextTrackLane(
    textOverlays: List<TextOverlay>,
    selectedTextId: String?,
    msToDp: (Long) -> Dp,
    onSelectText: (String) -> Unit,
    isLocked: Boolean = false,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF071912))
            .border(1.dp, Color(0xFF0F3927))
    ) {
        textOverlays.forEach { textOverlay ->
            val startDp = msToDp(textOverlay.timelineStartMs)
            val widthDp = msToDp(textOverlay.durationMs).coerceAtLeast(36.dp)
            val isSelected = textOverlay.id == selectedTextId

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF047857), Color(0xFF10B981))
                        )
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color.White else TrackTextColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectText(textOverlay.id) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "T: ${textOverlay.text}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StickerTrackLane(
    stickers: List<OverlaySticker>,
    selectedStickerId: String?,
    msToDp: (Long) -> Dp,
    onSelectSticker: (String) -> Unit,
    isLocked: Boolean = false,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF130E26))
            .border(1.dp, Color(0xFF2E1C59))
    ) {
        stickers.forEach { sticker ->
            val startDp = msToDp(sticker.timelineStartMs)
            val widthDp = msToDp(sticker.durationMs).coerceAtLeast(36.dp)
            val isSelected = sticker.id == selectedStickerId

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6))
                        )
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) AccentCyan else TrackOverlayColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectSticker(sticker.id) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${sticker.emojiOrIcon} ${sticker.label}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Looping Icon Indicator for Lottie / Vector Overlays
                    if (sticker.isAnimated || sticker.loopAnimation) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Looping Animation",
                                tint = Color(0xFFE9D5FF),
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShapeTrackLane(
    shapes: List<ShapeOverlay>,
    selectedShapeId: String?,
    msToDp: (Long) -> Dp,
    onSelectShape: (String) -> Unit,
    isLocked: Boolean = false,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF161026))
            .border(1.dp, Color(0xFF33205A))
    ) {
        shapes.forEach { shape ->
            val startDp = msToDp(shape.timelineStartMs)
            val widthDp = msToDp(shape.durationMs).coerceAtLeast(36.dp)
            val isSelected = shape.id == selectedShapeId

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF5B21B6), Color(0xFF7C3AED))
                        )
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) AccentCyan else Color(0xFFC084FC),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectShape(shape.id) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❖ ${shape.shapeType.displayName}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = shape.animation.displayName,
                        color = Color(0xFFDDD6FE),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ImageOverlayTrackLane(
    imageOverlays: List<ImageOverlay>,
    selectedImageOverlayId: String?,
    msToDp: (Long) -> Dp,
    onSelectImageOverlay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0E1824))
            .border(1.dp, Color(0xFF1E3A5F))
    ) {
        imageOverlays.forEach { overlay ->
            val startDp = msToDp(overlay.timelineStartMs)
            val widthDp = msToDp(overlay.durationMs).coerceAtLeast(44.dp)
            val isSelected = overlay.id == selectedImageOverlayId

            Box(
                modifier = Modifier
                    .offset(x = startDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0369A1))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) AccentCyan else Color(0xFF38BDF8),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectImageOverlay(overlay.id) }
                    .padding(horizontal = 6.dp)
                    .testTag("image_overlay_item_${overlay.id}"),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "🖼 ${overlay.name}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PlayheadScrubber(
    offsetDp: Dp,
    currentPositionMs: Long,
    totalDurationMs: Long,
    timelineZoom: Float,
    msToDp: (Long) -> Dp,
    onSeek: (Long) -> Unit,
    onSeekStart: () -> Unit = {},
    onSeekEnd: () -> Unit = {},
    onQuickSplit: (() -> Unit)? = null,
    isClipUnderPlayhead: Boolean = false,
    onCheckSnap: ((Long) -> Long)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var isDragging by remember { mutableStateOf(false) }
    var scrubVirtualPosMs by remember { mutableLongStateOf(currentPositionMs) }

    LaunchedEffect(currentPositionMs) {
        if (!isDragging) {
            scrubVirtualPosMs = currentPositionMs
        }
    }

    val smpte = formatSmpteTimecode(currentPositionMs)
    val frameNumber = ((currentPositionMs * 30L / 1000L) % 30L).toInt()

    val handleDragGesture: suspend androidx.compose.ui.input.pointer.PointerInputScope.() -> Unit = {
        detectDragGestures(
            onDragStart = {
                isDragging = true
                scrubVirtualPosMs = currentPositionMs
                onSeekStart()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                val pxPerSec = with(density) { msToDp(1000L).toPx() }
                val pxPerMs = if (pxPerSec > 0f) pxPerSec / 1000f else 0.05f
                val deltaMs = (dragAmount.x / pxPerMs).toLong()

                val rawCandidate = (scrubVirtualPosMs + deltaMs).coerceIn(0L, totalDurationMs)
                scrubVirtualPosMs = rawCandidate
                val candidateMs = onCheckSnap?.invoke(rawCandidate) ?: rawCandidate

                if (candidateMs != currentPositionMs) {
                    onSeek(candidateMs)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            },
            onDragEnd = {
                isDragging = false
                onSeekEnd()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDragCancel = {
                isDragging = false
                onSeekEnd()
            }
        )
    }

    Box(
        modifier = modifier
            .offset(x = (offsetDp - 36.dp).coerceAtLeast(0.dp))
            .width(72.dp)
            .zIndex(150f),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Floating SMPTE timecode & frame readout badge (draggable)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDragging) Color(0xF22D0A14) else Color(0xEE1E0B10),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isDragging) 1.5.dp else 1.dp,
                    color = if (isDragging) AccentCyan else PlayheadRed
                ),
                shadowElevation = if (isDragging) 8.dp else 4.dp,
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .graphicsLayer {
                        if (isDragging) {
                            scaleX = 1.1f
                            scaleY = 1.1f
                        }
                    }
                    .pointerInput(totalDurationMs, timelineZoom, handleDragGesture)
                    .testTag("playhead_timecode_badge")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = smpte,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "F$frameNumber",
                            color = if (isDragging) AccentCyan else PlayheadRed,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (isDragging) {
                        Text(
                            text = "⚡ SCRUBBING",
                            color = AccentCyan,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Top grab handle pointer (minimum 48dp x 28dp touchable zone with grip ridges)
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 28.dp)
                    .pointerInput(totalDurationMs, timelineZoom, handleDragGesture)
                    .testTag("playhead_grab_handle"),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Tactile pill head with ridges
                    Surface(
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                        color = if (isDragging) Color(0xFFFF1744) else PlayheadRed,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .width(26.dp)
                            .height(13.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(7.dp)
                                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(1.dp))
                                )
                                if (it < 2) Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                    }

                    // Inverted needle pointer
                    Canvas(modifier = Modifier.size(20.dp, 12.dp)) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(path = path, color = if (isDragging) Color(0xFFFF1744) else PlayheadRed)
                    }
                }
            }

            // Quick Split pill directly anchored on the playhead if hovering over a video clip
            if (isClipUnderPlayhead && onQuickSplit != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xEA991B1B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onQuickSplit()
                        }
                        .padding(vertical = 1.dp)
                        .testTag("playhead_direct_split_button")
                ) {
                    Text(
                        text = "✂ Split",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Vertical needle laser line spanning all tracks (also draggable!)
            Box(
                modifier = Modifier
                    .width(if (isDragging) 32.dp else 24.dp)
                    .weight(1f)
                    .pointerInput(totalDurationMs, timelineZoom, handleDragGesture)
                    .testTag("playhead_vertical_laser_line"),
                contentAlignment = Alignment.Center
            ) {
                if (isDragging) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        PlayheadRed.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .width(if (isDragging) 3.5.dp else 2.dp)
                        .fillMaxHeight()
                        .background(if (isDragging) Color(0xFFFF2A55) else PlayheadRed)
                )
            }

            // Bottom anchor diamond
            Canvas(modifier = Modifier.size(10.dp, 10.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(0f, size.height / 2f)
                    close()
                }
                drawPath(path = path, color = if (isDragging) Color(0xFFFF2A55) else PlayheadRed)
            }
        }
    }
}

@Composable
fun TimelineMiniMapNavigator(
    clips: List<VideoClip>,
    totalDurationMs: Long,
    currentPositionMs: Long,
    scrollState: androidx.compose.foundation.ScrollState,
    timelineWidthDp: Dp,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(Color(0xFF090D18))
            .border(1.dp, Color(0xFF1E2638))
            .pointerInput(totalDurationMs) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val targetMs = (frac * totalDurationMs).toLong()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSeek(targetMs)
                }
            }
            .pointerInput(totalDurationMs) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val frac = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val targetMs = (frac * totalDurationMs).toLong()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSeek(targetMs)
                }
            }
            .testTag("timeline_mini_map_navigator")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val safeTotalMs = totalDurationMs.coerceAtLeast(1000L).toFloat()

        // 1. Clips miniature track segments
        Canvas(modifier = Modifier.fillMaxSize()) {
            clips.forEach { clip ->
                val startFrac = (clip.timelineStartMs.toFloat() / safeTotalMs).coerceIn(0f, 1f)
                val durationFrac = (clip.trimmedDurationMs.toFloat() / safeTotalMs).coerceIn(0f, 1f)
                val left = startFrac * widthPx
                val w = (durationFrac * widthPx).coerceAtLeast(2f)

                drawRoundRect(
                    color = Color(clip.accentColorHex),
                    topLeft = Offset(left, 3f),
                    size = androidx.compose.ui.geometry.Size(w - 1f, size.height - 6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }
        }

        // 2. Viewport Highlight Rectangle representing visible timeline scroll area
        val totalTimelinePx = with(density) { timelineWidthDp.toPx() }
        if (totalTimelinePx > 0f) {
            val viewStartFrac = (scrollState.value.toFloat() / totalTimelinePx).coerceIn(0f, 1f)
            val viewWidthFrac = (widthPx / totalTimelinePx).coerceIn(0.08f, 1f)
            val lensLeft = (viewStartFrac * widthPx)
            val lensWidth = (viewWidthFrac * widthPx).coerceAtLeast(24f)

            Box(
                modifier = Modifier
                    .offset(x = with(density) { lensLeft.toDp() })
                    .width(with(density) { lensWidth.toDp() })
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
            )
        }

        // 3. Mini Playhead indicator
        val playheadFrac = (currentPositionMs.toFloat() / safeTotalMs).coerceIn(0f, 1f)
        val playheadX = playheadFrac * widthPx
        Canvas(
            modifier = Modifier
                .offset(x = with(density) { (playheadX - 1.5f).toDp() })
                .width(3.dp)
                .fillMaxHeight()
        ) {
            drawLine(
                color = PlayheadRed,
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
fun TimelineQuickNavBar(
    currentPositionMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    timelineZoom: Float,
    isSnappingEnabled: Boolean,
    onTogglePlay: () -> Unit,
    onJumpToStart: () -> Unit,
    onJumpToEnd: () -> Unit,
    onJumpToPrevCut: () -> Unit,
    onJumpToNextCut: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onToggleSnapping: () -> Unit,
    onQuickSplit: () -> Unit,
    onAddMarker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0B101E))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Jump navigation & Play transport
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onJumpToStart()
                },
                modifier = Modifier.size(28.dp).testTag("quick_nav_jump_start")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Jump to Start",
                    tint = Color.LightGray,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onJumpToPrevCut()
                },
                modifier = Modifier.size(28.dp).testTag("quick_nav_prev_cut")
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Previous Cut",
                    tint = Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isPlaying) PlayheadRed else AccentCyan,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTogglePlay()
                    }
                    .testTag("quick_nav_play_pause")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onJumpToNextCut()
                },
                modifier = Modifier.size(28.dp).testTag("quick_nav_next_cut")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Next Cut",
                    tint = Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onJumpToEnd()
                },
                modifier = Modifier.size(28.dp).testTag("quick_nav_jump_end")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Jump to End",
                    tint = Color.LightGray,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Center: Quick Split & Add Marker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF991B1B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuickSplit()
                    }
                    .testTag("quick_nav_split_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "✂ Split",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f)),
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onAddMarker()
                    }
                    .testTag("quick_nav_add_marker")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = "Add Marker",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Marker",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Right: Zoom Presets (Fit, 1x, 2x, 4x) & Snapping
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf(
                "Fit" to 0.5f,
                "1x" to 1.0f,
                "2x" to 2.0f,
                "4x" to 4.0f
            ).forEach { (label, zoomVal) ->
                val isSelected = kotlin.math.abs(timelineZoom - zoomVal) < 0.2f
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = if (isSelected) AccentCyan else Color(0xFF1A2234),
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onZoomChange(zoomVal)
                        }
                        .padding(horizontal = 1.dp)
                        .testTag("zoom_preset_$label")
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.LightGray,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleSnapping()
                },
                modifier = Modifier.size(26.dp).testTag("quick_nav_snap_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "Magnet Snapping",
                    tint = if (isSnappingEnabled) AccentCyan else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Interactive Timeline Zoom Control Bar for expanding or contracting video/audio tracks
 * with a continuous slider, fine +/- step buttons, and a real-time frame/ms resolution indicator.
 */
@Composable
fun TimelineInteractiveZoomBar(
    timelineZoom: Float,
    totalDurationMs: Long,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dpPerSec = (55f * timelineZoom).roundToInt()
    val msPerDp = (1000f / (55f * timelineZoom)).roundToInt().coerceAtLeast(1)
    val scaleLabel = when {
        timelineZoom >= 3.2f -> "FRAME-ACCURATE (${dpPerSec}dp/s • 1f=33ms)"
        timelineZoom >= 1.8f -> "FINE CUT (${dpPerSec}dp/s • ${msPerDp}ms/dp)"
        timelineZoom <= 0.7f -> "FIT OVERVIEW (${dpPerSec}dp/s)"
        else -> "STANDARD (${dpPerSec}dp/s • ${msPerDp}ms/dp)"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0E1526))
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .testTag("timeline_interactive_zoom_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ZoomOut,
            contentDescription = "Contract Timeline View",
            tint = AccentCyan,
            modifier = Modifier
                .size(15.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onZoomChange((timelineZoom - 0.25f).coerceIn(0.5f, 4.0f))
                }
                .testTag("interactive_zoom_contract_icon")
        )

        Slider(
            value = timelineZoom.coerceIn(0.5f, 4.0f),
            onValueChange = { newZoom ->
                onZoomChange(newZoom.coerceIn(0.5f, 4.0f))
            },
            valueRange = 0.5f..4.0f,
            colors = SliderDefaults.colors(
                thumbColor = AccentCyan,
                activeTrackColor = AccentCyan,
                inactiveTrackColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .weight(1f)
                .height(22.dp)
                .testTag("timeline_zoom_slider")
        )

        Icon(
            imageVector = Icons.Default.ZoomIn,
            contentDescription = "Expand Timeline View",
            tint = AccentCyan,
            modifier = Modifier
                .size(15.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onZoomChange((timelineZoom + 0.25f).coerceIn(0.5f, 4.0f))
                }
                .testTag("interactive_zoom_expand_icon")
        )

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (timelineZoom >= 2.5f) AccentPink.copy(alpha = 0.2f) else Color(0xFF172136),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (timelineZoom >= 2.5f) AccentPink else AccentCyan.copy(alpha = 0.45f)
            )
        ) {
            Text(
                text = "${(timelineZoom * 100).roundToInt()}% • $scaleLabel",
                color = if (timelineZoom >= 2.5f) AccentPink else AccentCyan,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

