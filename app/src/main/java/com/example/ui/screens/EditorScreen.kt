package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AspectRatio
import com.example.data.model.EditorWorkspace
import com.example.data.model.TextStylePreset
import com.example.data.model.TransitionType
import com.example.ui.components.AiVideoStudioComponent
import com.example.ui.components.AiVideoStudioSheet
import com.example.ui.components.AudioMixerSheet
import com.example.ui.components.AudioMixingControl
import com.example.ui.components.AudioMixingSheet
import com.example.ui.components.AudioPickerSheet
import com.example.ui.components.CanvasBackgroundSheet
import com.example.ui.components.ChromaKeySheet
import com.example.ui.components.CinematicFilterSelectionPanel
import com.example.ui.components.ColorGradingSheet
import com.example.ui.components.ColorGradingStudio
import com.example.ui.components.EditToolstrip
import com.example.ui.components.FilterSheet
import com.example.ui.components.KeyframeParameterEditorOverlay
import com.example.ui.components.KeyframePropertyEditorSheet
import com.example.ui.components.KeyframeSheet
import com.example.ui.components.MediaPickerSheet
import com.example.ui.components.MotionVfxStudio
import com.example.ui.components.MultiTrackTimeline
import com.example.ui.components.OpenShotEffectsSheet
import com.example.ui.components.OpenShotRazorSheet
import com.example.ui.components.OpenShotTitleSheet
import com.example.ui.components.PrecisionVideoScrubberSheet
import com.example.ui.components.ProjectHistorySheet
import com.example.ui.components.ShapeEditorSheet
import com.example.ui.components.SpeedSheet
import com.example.ui.components.StabilizationSheet
import com.example.ui.components.StickerPickerSheet
import com.example.ui.components.TextEditorSheet
import com.example.ui.components.TransitionLibrarySheet
import com.example.ui.components.TransitionSheet
import com.example.ui.components.TransitionsLibraryPanel
import com.example.ui.components.TrimSheet
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    uiState: EditorUiState,
    viewModel: VideoEditorViewModel,
    onBack: () -> Unit,
    onNavigateExport: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showAspectRatioPicker by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    // Android predictive back handler
    BackHandler {
        onBack()
    }

    // Show status message snackbars
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showRenameDialog = true }
                            .testTag("editor_title_rename_trigger")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_icon),
                            contentDescription = "OpenCut App Icon",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.currentProject?.title ?: "OpenCut Editor",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename Project",
                                    tint = AccentCyan.copy(alpha = 0.8f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = "${uiState.aspectRatio.label.split(" ").first()} • ${uiState.clips.size} clips",
                                color = AccentCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Projects", tint = Color.White)
                    }
                },
                actions = {
                    // Smart Performance & Proxy Quality 1-Tap Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (uiState.proxyQualityMode.resolutionScale < 1f) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (uiState.proxyQualityMode.resolutionScale < 1f) Color(0xFF10B981) else AccentCyan.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { viewModel.cycleProxyQualityMode() }
                            .testTag("proxy_mode_badge")
                    ) {
                        Text(
                            text = uiState.proxyQualityMode.shortBadge,
                            color = if (uiState.proxyQualityMode.resolutionScale < 1f) Color(0xFF34D399) else AccentCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Room Editing History & Layer Metadata
                    IconButton(
                        onClick = { viewModel.toggleHistorySheet(true) },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Editing History & Layers",
                            tint = if (uiState.editHistory.isNotEmpty()) AccentCyan else Color.White
                        )
                    }

                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) Color.White else Color.Gray.copy(alpha = 0.4f)
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) Color.White else Color.Gray.copy(alpha = 0.4f)
                        )
                    }

                    // Export Button
                    Button(
                        onClick = onNavigateExport,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(36.dp)
                            .testTag("export_nav_button")
                    ) {
                        Icon(Icons.Default.FileDownload, "Export", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioSurfaceDark)
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Zone 1: Top Zone (Preview Monitor & Timecode)
            VideoPlayerView(
                activeClip = uiState.activeClipAtPlayhead,
                aspectRatio = uiState.aspectRatio,
                currentPositionMs = uiState.currentPositionMs,
                totalDurationMs = uiState.totalDurationMs,
                isPlaying = uiState.isPlaying,
                activeTextOverlays = uiState.activeTextOverlays,
                activeStickers = uiState.activeStickers,
                activeShapes = uiState.activeShapes,
                isStabilizationSplitPreview = uiState.isStabilizationSplitPreview,
                isKeyframeOverlayActive = uiState.showKeyframeOverlay,
                onToggleKeyframeOverlay = { viewModel.toggleKeyframeOverlay() },
                isScrubbing = uiState.isScrubbing,
                playbackSpeed = uiState.playbackSpeed,
                onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                onJumpToStart = { viewModel.jumpToStart() },
                onJumpToEnd = { viewModel.jumpToEnd() },
                onTogglePlay = { viewModel.togglePlayPause() },
                onStepFrame = { viewModel.stepFrame(it) },
                onJumpCut = { viewModel.jumpToCut(it) },
                onChangeAspectRatio = { showAspectRatioPicker = !showAspectRatioPicker },
                onImportVideoClick = { viewModel.toggleMediaPicker(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            )

            // Keyframe Parameter Editor Overlay (adjust scale, opacity, rotation over time)
            AnimatedVisibility(
                visible = uiState.showKeyframeOverlay && uiState.selectedClip != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                if (uiState.selectedClip != null) {
                    val clip = uiState.selectedClip!!
                    KeyframeParameterEditorOverlay(
                        clip = clip,
                        currentTimelinePositionMs = uiState.currentPositionMs,
                        isPlaying = uiState.isPlaying,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onSeekTimeline = { timeMs -> viewModel.seekTo(timeMs) },
                        onAddKeyframe = { kf -> viewModel.addKeyframe(clip.id, kf) },
                        onUpdateKeyframe = { kf -> viewModel.updateKeyframe(clip.id, kf) },
                        onRemoveKeyframe = { kfId -> viewModel.removeKeyframe(clip.id, kfId) },
                        onApplyPreset = { preset -> viewModel.applyKeyframePreset(clip.id, preset) },
                        onClearKeyframes = { viewModel.clearAllKeyframes(clip.id) },
                        onOpenCurveGraphSheet = { viewModel.toggleKeyframeSheet(true) },
                        onClose = { viewModel.toggleKeyframeOverlay(false) }
                    )
                }
            }

            // Aspect ratio quick switch selector dropdown
            if (showAspectRatioPicker) {
                Surface(
                    color = Color(0xFF1B182B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AspectRatio.entries.forEach { aspect ->
                            val isSelected = uiState.aspectRatio == aspect
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AccentCyan else Color(0xFF27243C),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.setAspectRatio(aspect)
                                        showAspectRatioPicker = false
                                    }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = aspect.label.split(" ").first(),
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // DaVinci Resolve Style Professional Workspace Switcher Bar
            WorkspaceSwitcherBar(
                currentWorkspace = uiState.currentWorkspace,
                onSelectWorkspace = { ws ->
                    if (ws == EditorWorkspace.DELIVER) {
                        onNavigateExport()
                    } else {
                        viewModel.setWorkspace(ws)
                    }
                }
            )

            when (uiState.currentWorkspace) {
                EditorWorkspace.COLOR -> {
                    val targetClip = uiState.selectedClip ?: uiState.clips.firstOrNull()
                    if (targetClip != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.05f)
                        ) {
                            CinematicFilterSelectionPanel(
                                clip = targetClip,
                                allClipsCount = uiState.clips.size,
                                onSelectFilter = { viewModel.setClipFilter(targetClip.id, it) },
                                onUpdateFilterIntensity = { viewModel.setClipFilterIntensity(targetClip.id, it) },
                                onUpdateColorGrading = { viewModel.updateColorGrading(targetClip.id, it) },
                                onUpdateBasicGrading = { b, c, s -> viewModel.updateColorGrading(targetClip.id, b, c, s) },
                                onApplyPresetToAllClips = { filter, lut ->
                                    viewModel.applyFilterPresetToAllClips(filter, lut)
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                            ColorGradingStudio(
                                clip = targetClip,
                                onUpdateGrading = { settings ->
                                    viewModel.updateColorGrading(targetClip.id, settings)
                                },
                                onResetGrading = {
                                    viewModel.resetColorGrading(targetClip.id)
                                },
                                isCompareSplit = uiState.isColorCompareSplit,
                                onToggleCompareSplit = {
                                    viewModel.toggleColorCompareSplit()
                                },
                                onClose = { viewModel.setWorkspace(EditorWorkspace.EDIT) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.05f)
                                .background(StudioSurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Select or add a clip to start Color Grading", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                EditorWorkspace.AUDIO -> {
                    AudioMixingControl(
                        uiState = uiState,
                        onToggleAutoDuckingMaster = { viewModel.setAutoDuckingMaster(it) },
                        onSetAutoDuckingLevel = { viewModel.setAutoDuckingLevel(it) },
                        onToggleTrackDucking = { viewModel.toggleAudioDucking(it) },
                        onSetTrackVolume = { audioId, vol, muted -> viewModel.setAudioTrackVolume(audioId, vol, muted) },
                        onSetTrackPan = { audioId, pan -> viewModel.setAudioPan(audioId, pan) },
                        onSetTrackCategory = { audioId, cat -> viewModel.setAudioTrackCategory(audioId, cat) },
                        onSetClipVolume = { clipId, vol, muted -> viewModel.setClipVolume(clipId, vol, muted) },
                        onToggleClipSpeech = { clipId, speech -> viewModel.toggleClipSpeech(clipId, speech) },
                        onClose = { viewModel.setWorkspace(EditorWorkspace.EDIT) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.05f)
                    )
                }

                EditorWorkspace.MOTION_VFX -> {
                    val targetClip = uiState.selectedClip ?: uiState.clips.firstOrNull()
                    MotionVfxStudio(
                        activeClip = targetClip,
                        onApplySpeedRamp = { preset, interp, preservePitch ->
                            viewModel.applySpeedRampPreset(preset, interp, preservePitch)
                        },
                        onApplyParticleVfx = { vfxPreset ->
                            viewModel.applyParticleVfx(vfxPreset)
                        },
                        onToggleNodeStage = { nodeName, enabled ->
                            viewModel.toggleNodeStage(nodeName, enabled)
                        },
                        onApplyMotionTrack = { mode, targetLabel ->
                            viewModel.applyMotionTracker(mode, targetLabel)
                        },
                        onClose = { viewModel.setWorkspace(EditorWorkspace.EDIT) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.05f)
                    )
                }

                EditorWorkspace.AI_MAGIC -> {
                    AiVideoStudioComponent(
                        uiState = uiState,
                        onExecuteSilenceRemoval = { clipId, thresholdDb ->
                            viewModel.executeAiSilenceRemoval(clipId, thresholdDb)
                        },
                        onGenerateAutoCaptions = { preset ->
                            viewModel.generateAiAutoCaptions(preset)
                        },
                        onApplySmartReframe = { targetAspect ->
                            viewModel.applyAiSmartReframe(targetAspect)
                        },
                        onDetectSceneCuts = { clipId ->
                            viewModel.detectAiSceneCuts(clipId)
                        },
                        onEnhanceVoice = { clipId ->
                            viewModel.enhanceAiVoice(clipId)
                        },
                        onClose = { viewModel.setWorkspace(EditorWorkspace.EDIT) },
                        onExecuteCopilotPrompt = { prompt ->
                            viewModel.executeAiCopilotPrompt(prompt)
                        },
                        onSeekToTime = { timeMs ->
                            viewModel.seekTo(timeMs)
                        },
                        onRippleCutTranscript = { startMs, endMs, text ->
                            viewModel.rippleCutTranscriptSegment(startMs, endMs, text)
                        },
                        onRemoveFillerWords = {
                            viewModel.removeTranscriptFillerWords()
                        },
                        onSwitchMulticamAngle = { label, filter ->
                            viewModel.switchMulticamAngle(label, filter)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.05f)
                    )
                }

                EditorWorkspace.EDIT, EditorWorkspace.DELIVER -> {
                    // Zone 2: Middle Zone (Contextual Toolbar / Property Inspector)
                    EditToolstrip(
                        hasSelectedClip = uiState.selectedClip != null,
                        hasSelectedAudio = uiState.selectedAudio != null,
                        hasSelectedText = uiState.selectedText != null,
                        hasSelectedSticker = uiState.selectedStickerId != null,
                        hasSelectedShape = uiState.selectedShapeId != null,
                        onSplit = { viewModel.splitSelectedClip() },
                        onTrim = { viewModel.toggleTrimSheet(true) },
                        onSpeed = { viewModel.toggleSpeedSheet(true) },
                        onFilter = { viewModel.toggleFilterSheet(true) },
                        onTransition = {
                            if (uiState.selectedClipId == null && uiState.clips.isNotEmpty()) {
                                viewModel.selectClip(uiState.clips.first().id)
                            }
                            viewModel.toggleTransitionSheet(true)
                        },
                        onRotate = { uiState.selectedClipId?.let { viewModel.rotateClip(it) } },
                        onFlip = { uiState.selectedClipId?.let { viewModel.flipHorizontalClip(it) } },
                        onAudio = { viewModel.toggleAudioSheet(true) },
                        onDetachAudio = { uiState.selectedClipId?.let { viewModel.detachAudio(it) } },
                        onText = { viewModel.toggleTextSheet(true) },
                        onStickers = { viewModel.toggleStickerSheet(true) },
                        onShapes = { viewModel.toggleShapeSheet(true) },
                        onStabilize = { viewModel.toggleStabilizationSheet(true) },
                        onAddMusic = { viewModel.toggleAudioPicker(true) },
                        onDelete = {
                            when {
                                uiState.selectedClipId != null -> viewModel.deleteClip(uiState.selectedClipId)
                                uiState.selectedAudioId != null -> viewModel.deleteAudioItem(uiState.selectedAudioId)
                                uiState.selectedTextId != null -> viewModel.deleteTextOverlay(uiState.selectedTextId)
                                uiState.selectedStickerId != null -> viewModel.deleteSticker(uiState.selectedStickerId)
                                uiState.selectedShapeId != null -> viewModel.deleteShapeOverlay(uiState.selectedShapeId!!)
                            }
                        },
                        onDuplicate = { uiState.selectedClipId?.let { viewModel.duplicateClip(it) } },
                        onMoveLeft = { uiState.selectedClipId?.let { viewModel.moveClip(it, moveLeft = true) } },
                        onMoveRight = { uiState.selectedClipId?.let { viewModel.moveClip(it, moveLeft = false) } },
                        onFreeze = { viewModel.freezeFrameAtPlayhead() },
                        onReverse = { uiState.selectedClipId?.let { viewModel.toggleClipReverse(it) } },
                        onCanvasBg = { viewModel.toggleCanvasBgSheet(true) },
                        onKeyframe = { viewModel.toggleKeyframeOverlay() },
                        onChromaKey = { viewModel.toggleChromaKeySheet(true) },
                        onBeatSync = {
                            val targetId = uiState.selectedAudioId ?: uiState.audioClips.firstOrNull()?.id
                            if (targetId != null) viewModel.toggleBeatSync(targetId)
                        },
                        onAudioDucking = {
                            viewModel.toggleAudioSheet(true)
                        },
                        onEffects = { viewModel.toggleEffectsSheet(true) },
                        onRazor = { viewModel.toggleRazorSheet(true) },
                        onOpenShotTitles = { viewModel.toggleTitleTemplatesSheet(true) },
                        onToggleSnapping = { viewModel.toggleSnapping() },
                        onPrecisionScrubber = { viewModel.togglePrecisionScrubber(true) },
                        onColorGrade = { viewModel.toggleColorGradingSheet(true) },
                        onAiMagic = { viewModel.toggleAiStudioSheet(true) },
                        isReversed = uiState.selectedClip?.isReversed == true,
                        hasKeyframe = uiState.selectedClip?.hasKeyframe == true,
                        isChromaKeyActive = uiState.selectedClip?.isChromaKeyEnabled == true,
                        isBeatSyncActive = uiState.audioClips.any { it.isBeatSyncEnabled },
                        isDuckingActive = uiState.isAutoDuckingMasterEnabled && uiState.audioClips.any { it.isDuckingEnabled },
                        isStabilized = uiState.selectedClip?.stabilization?.isEnabled == true,
                        isSnappingActive = uiState.isSnappingEnabled,
                        hasAppliedEffects = uiState.selectedClip?.effects?.isNotEmpty() == true
                    )

                    // Zone 3: Bottom Zone (Multi-Track Precision Timeline - Thumb reachable)
                    MultiTrackTimeline(
                        clips = uiState.clips,
                        audioClips = uiState.audioClips,
                        textOverlays = uiState.textOverlays,
                        stickers = uiState.stickers,
                        shapes = uiState.shapes,
                        currentPositionMs = uiState.currentPositionMs,
                        totalDurationMs = uiState.totalDurationMs,
                        selectedClipId = uiState.selectedClipId,
                        selectedAudioId = uiState.selectedAudioId,
                        selectedTextId = uiState.selectedTextId,
                        selectedStickerId = uiState.selectedStickerId,
                        selectedShapeId = uiState.selectedShapeId,
                        timelineZoom = uiState.timelineZoom,
                        onSeek = { viewModel.seekTo(it) },
                        onSelectClip = { viewModel.selectClip(it) },
                        onSelectAudio = { viewModel.selectAudio(it) },
                        onSelectText = { viewModel.selectText(it) },
                        onSelectSticker = { viewModel.selectSticker(it) },
                        onSelectShape = { viewModel.selectShape(it) },
                        onZoomChange = { viewModel.setZoom(it) },
                        onAddMediaClick = { viewModel.toggleMediaPicker(true) },
                        onSelectTransitionClick = { clipId ->
                            viewModel.selectClip(clipId)
                            viewModel.toggleTransitionSheet(true)
                        },
                        isSnappingEnabled = uiState.isSnappingEnabled,
                        onToggleSnapping = { viewModel.toggleSnapping() },
                        onReorderClips = { from, to -> viewModel.reorderClips(from, to) },
                        onOpenPrecisionScrubber = { viewModel.togglePrecisionScrubber(true) },
                        draggingTransition = uiState.draggingTransition,
                        onDropTransition = { clipId, transition -> viewModel.dropTransitionOnClip(clipId) },
                        onCancelDragTransition = { viewModel.stopDraggingTransition() },
                        timelineMarkers = uiState.timelineMarkers,
                        onAddCueMarker = { viewModel.addTimelineCueMarker() },
                        onDeleteCueMarker = { viewModel.deleteTimelineCueMarker(it) },
                        onJumpToMarker = { viewModel.jumpToMarker(it) },
                        isVideoTrackMuted = uiState.isVideoTrackMuted,
                        isVideoTrackLocked = uiState.isVideoTrackLocked,
                        isAudioTrackMuted = uiState.isAudioTrackMuted,
                        isAudioTrackLocked = uiState.isAudioTrackLocked,
                        isStickerTrackVisible = uiState.isStickerTrackVisible,
                        isStickerTrackLocked = uiState.isStickerTrackLocked,
                        isShapeTrackVisible = uiState.isShapeTrackVisible,
                        isShapeTrackLocked = uiState.isShapeTrackLocked,
                        isTextTrackVisible = uiState.isTextTrackVisible,
                        isTextTrackLocked = uiState.isTextTrackLocked,
                        onToggleTrackVisibility = { viewModel.toggleTrackVisibility(it) },
                        onToggleTrackLock = { viewModel.toggleTrackLock(it) },
                        isMediaTrayExpanded = uiState.isMediaTrayExpanded,
                        onToggleMediaTray = { viewModel.toggleMediaTray() },
                        onDropNewClip = { sample, targetTimeMs ->
                            if (targetTimeMs >= 0L) {
                                viewModel.insertClipAtTime(sample, targetTimeMs)
                            } else {
                                viewModel.addSampleClip(sample)
                            }
                        },
                        onUpdateTrim = { clipId, inMs, outMs -> viewModel.updateTrim(clipId, inMs, outMs) },
                        onUpdateTrimLive = { clipId, inMs, outMs -> viewModel.updateTrimLive(clipId, inMs, outMs) },
                        onStepTrim = { clipId, dIn, dOut -> viewModel.stepTrim(clipId, dIn, dOut) },
                        onResetTrim = { clipId -> viewModel.resetTrim(clipId) },
                        isPlaying = uiState.isPlaying,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onSeekStart = { viewModel.startScrubbing() },
                        onSeekEnd = { viewModel.stopScrubbing() },
                        onQuickSplit = { viewModel.splitSelectedClip() },
                        onJumpToStart = { viewModel.jumpToStart() },
                        onJumpToEnd = { viewModel.jumpToEnd() },
                        onJumpToPrevCut = { viewModel.jumpToCut(false) },
                        onJumpToNextCut = { viewModel.jumpToCut(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.05f)
                    )
                }
            }
        }
    }

    // Modal Sheets Integration
    if (uiState.showMediaPickerSheet) {
        MediaPickerSheet(
            onDismiss = { viewModel.toggleMediaPicker(false) },
            onSelectSample = { viewModel.addSampleClip(it) },
            onImportDeviceVideo = { uri, name -> viewModel.addCustomMediaClip(uri, name) },
            onGenerateTestVideo = { viewModel.addDeviceTestVideoClip() }
        )
    }

    if (uiState.showAudioPickerSheet) {
        AudioPickerSheet(
            onDismiss = { viewModel.toggleAudioPicker(false) },
            onSelectAudio = { viewModel.addAudioTrack(it) }
        )
    }

    if (uiState.showFilterSheet && uiState.selectedClip != null) {
        val activeClip = uiState.selectedClip!!
        FilterSheet(
            clip = activeClip,
            onDismiss = { viewModel.toggleFilterSheet(false) },
            onSelectFilter = { viewModel.setClipFilter(activeClip.id, it) },
            onUpdateGrading = { b, c, s -> viewModel.updateColorGrading(activeClip.id, b, c, s) },
            allClipsCount = uiState.clips.size,
            onUpdateFilterIntensity = { viewModel.setClipFilterIntensity(activeClip.id, it) },
            onUpdateColorGradingSettings = { viewModel.updateColorGrading(activeClip.id, it) },
            onApplyPresetToAllClips = { filter, lut ->
                viewModel.applyFilterPresetToAllClips(filter, lut)
            }
        )
    }

    if (uiState.showTrimSheet && uiState.selectedClip != null) {
        TrimSheet(
            clip = uiState.selectedClip!!,
            onDismiss = { viewModel.toggleTrimSheet(false) },
            onApplyTrim = { inMs, outMs -> viewModel.updateTrim(uiState.selectedClip!!.id, inMs, outMs) }
        )
    }

    if (uiState.showSpeedSheet && uiState.selectedClip != null) {
        SpeedSheet(
            clip = uiState.selectedClip!!,
            onDismiss = { viewModel.toggleSpeedSheet(false) },
            onSelectSpeed = { viewModel.setClipSpeed(uiState.selectedClip!!.id, it) }
        )
    }

    if (uiState.showTransitionSheet) {
        TransitionLibrarySheet(
            clips = uiState.clips,
            selectedClipId = uiState.selectedClipId,
            onSelectClip = { viewModel.selectClip(it) },
            onApplyTransition = { clipId, transition, durationMs ->
                viewModel.setTransition(clipId, transition, durationMs)
            },
            onApplyToAll = { transition, durationMs ->
                viewModel.applyTransitionToAllClips(transition, durationMs)
            },
            onStartDragTransition = { transition, durationMs ->
                viewModel.startDraggingTransition(transition, durationMs)
            },
            onStopDragTransition = {
                viewModel.stopDraggingTransition()
            },
            onDismiss = { viewModel.toggleTransitionSheet(false) }
        )
    }

    if (uiState.showTransitionsLibraryPanel) {
        val selClip = uiState.selectedClip ?: uiState.clips.firstOrNull()
        val selTrans = selClip?.transitionIn ?: TransitionType.CROSSFADE
        val durSec = (selClip?.transitionDurationMs ?: 600L) / 1000f

        TransitionsLibraryPanel(
            selectedTransition = selTrans,
            onSelectTransition = { trans ->
                selClip?.let { viewModel.setTransition(it.id, trans, (durSec * 1000).toLong()) }
            },
            transitionDurationSec = durSec,
            onDurationChange = { sec ->
                selClip?.let { viewModel.setTransition(it.id, selTrans, (sec * 1000).toLong()) }
            },
            onStartDragTransition = { trans, durMs ->
                viewModel.startDraggingTransition(trans, durMs)
            },
            onStopDragTransition = {
                viewModel.stopDraggingTransition()
            },
            onApplyToSelectedClip = {
                selClip?.let { viewModel.setTransition(it.id, selTrans, (durSec * 1000).toLong()) }
            },
            onApplyToAllClips = {
                viewModel.applyTransitionToAllClips(selTrans, (durSec * 1000).toLong())
            },
            onClose = { viewModel.toggleTransitionsLibraryPanel(false) }
        )
    }

    if (uiState.showTextSheet) {
        TextEditorSheet(
            existingText = uiState.selectedText?.text ?: "",
            existingTextColor = uiState.selectedText?.textColorHex ?: 0xFFFFFFFF,
            existingBgColor = uiState.selectedText?.bgColorHex ?: 0xAA000000,
            onDismiss = { viewModel.toggleTextSheet(false) },
            onSaveText = { text, preset, textColor, bgColor ->
                if (uiState.selectedText != null) {
                    viewModel.updateTextOverlay(
                        uiState.selectedText!!.id,
                        text,
                        uiState.selectedText!!.fontSizeSp,
                        preset,
                        textColor,
                        bgColor
                    )
                } else {
                    viewModel.addTextOverlay(text, preset, textColor, bgColor)
                }
            }
        )
    }

    if (uiState.showStabilizationSheet && uiState.selectedClip != null) {
        StabilizationSheet(
            clip = uiState.selectedClip!!,
            onDismiss = { viewModel.toggleStabilizationSheet(false) },
            onUpdateStabilization = { settings ->
                viewModel.updateStabilization(uiState.selectedClip!!.id, settings)
            },
            onToggleSplitPreview = { isSplit ->
                viewModel.toggleStabilizationSplitPreview(isSplit)
            }
        )
    }

    if (uiState.showShapeSheet) {
        val existingShape = uiState.shapes.firstOrNull { it.id == uiState.selectedShapeId }
        ShapeEditorSheet(
            existingShape = existingShape,
            onDismiss = { viewModel.toggleShapeSheet(false) },
            onSaveShape = { shape ->
                if (existingShape != null) {
                    viewModel.updateShapeOverlay(shape)
                } else {
                    viewModel.addShapeOverlay(shape)
                }
            }
        )
    }

    if (uiState.showStickerSheet) {
        StickerPickerSheet(
            onDismiss = { viewModel.toggleStickerSheet(false) },
            onSelectSticker = { emoji, label -> viewModel.addSticker(emoji, label) },
            onSelectAnimatedSticker = { type, label, iconOrBadge ->
                viewModel.addAnimatedSticker(type, label, iconOrBadge)
            }
        )
    }

    if (uiState.showChromaKeySheet && uiState.selectedClip != null) {
        ChromaKeySheet(
            clip = uiState.selectedClip!!,
            onDismiss = { viewModel.toggleChromaKeySheet(false) },
            onToggleEnabled = { viewModel.toggleChromaKey(uiState.selectedClip!!.id, it) },
            onUpdateChromaKey = { colorHex, tolerance ->
                viewModel.updateChromaKey(uiState.selectedClip!!.id, colorHex, tolerance)
            }
        )
    }

    if (uiState.showAudioSheet) {
        AudioMixingSheet(
            uiState = uiState,
            onToggleAutoDuckingMaster = { viewModel.setAutoDuckingMaster(it) },
            onSetAutoDuckingLevel = { viewModel.setAutoDuckingLevel(it) },
            onToggleTrackDucking = { viewModel.toggleAudioDucking(it) },
            onSetTrackVolume = { audioId, vol, muted -> viewModel.setAudioTrackVolume(audioId, vol, muted) },
            onSetTrackPan = { audioId, pan -> viewModel.setAudioPan(audioId, pan) },
            onSetTrackCategory = { audioId, cat -> viewModel.setAudioTrackCategory(audioId, cat) },
            onSetClipVolume = { clipId, vol, muted -> viewModel.setClipVolume(clipId, vol, muted) },
            onToggleClipSpeech = { clipId, speech -> viewModel.toggleClipSpeech(clipId, speech) },
            onDismiss = { viewModel.toggleAudioSheet(false) }
        )
    }

    if (uiState.showCanvasBgSheet && uiState.selectedClip != null) {
        CanvasBackgroundSheet(
            clip = uiState.selectedClip!!,
            onDismiss = { viewModel.toggleCanvasBgSheet(false) },
            onSelectBgMode = { viewModel.setCanvasBackgroundMode(uiState.selectedClip!!.id, it) },
            onToggleVertical = { viewModel.setClipVerticalOrientation(uiState.selectedClip!!.id, it) },
            onSetBlurParams = { radius, bright -> viewModel.setClipCanvasBlur(uiState.selectedClip!!.id, radius, bright) }
        )
    }

    if (uiState.showKeyframeSheet && uiState.selectedClip != null) {
        val clip = uiState.selectedClip!!
        KeyframePropertyEditorSheet(
            clip = clip,
            currentTimelinePositionMs = uiState.currentPositionMs,
            onDismiss = { viewModel.toggleKeyframeSheet(false) },
            onAddKeyframe = { kf -> viewModel.addKeyframe(clip.id, kf) },
            onUpdateKeyframe = { kf -> viewModel.updateKeyframe(clip.id, kf) },
            onRemoveKeyframe = { kfId -> viewModel.removeKeyframe(clip.id, kfId) },
            onApplyPreset = { preset -> viewModel.applyKeyframePreset(clip.id, preset) },
            onClearKeyframes = { viewModel.clearAllKeyframes(clip.id) },
            onSeekTimeline = { timeMs -> viewModel.seekTo(timeMs) }
        )
    }

    // OpenShot Video Effects Sheet
    if (uiState.showEffectsSheet && uiState.selectedClip != null) {
        val clip = uiState.selectedClip!!
        OpenShotEffectsSheet(
            clip = clip,
            onDismiss = { viewModel.toggleEffectsSheet(false) },
            onAddEffect = { effectType -> viewModel.addVideoEffect(clip.id, effectType) },
            onRemoveEffect = { effectId -> viewModel.removeVideoEffect(clip.id, effectId) },
            onUpdateEffect = { effectId, intensity, secondary ->
                viewModel.updateVideoEffect(clip.id, effectId, intensity, secondary)
            },
            onToggleEffect = { effectId, enabled ->
                viewModel.toggleVideoEffect(clip.id, effectId, enabled)
            }
        )
    }

    // OpenShot Razor & Slicing Suite Sheet
    if (uiState.showRazorSheet) {
        OpenShotRazorSheet(
            selectedClip = uiState.selectedClip,
            currentPositionMs = uiState.currentPositionMs,
            isSnappingEnabled = uiState.isSnappingEnabled,
            onDismiss = { viewModel.toggleRazorSheet(false) },
            onSliceClip = { viewModel.splitSelectedClip() },
            onSliceKeepLeft = {
                uiState.selectedClipId?.let { viewModel.sliceKeepLeft(it) }
            },
            onSliceKeepRight = {
                uiState.selectedClipId?.let { viewModel.sliceKeepRight(it) }
            },
            onSliceAllTracks = { viewModel.sliceAllAtPlayhead() },
            onToggleSnapping = { viewModel.toggleSnapping() }
        )
    }

    // OpenShot 2D / 3D Title Templates Generator Sheet
    if (uiState.showTitleTemplatesSheet) {
        OpenShotTitleSheet(
            onDismiss = { viewModel.toggleTitleTemplatesSheet(false) },
            onAddTitle = { title, subtitle, template, is3D, fontSizeSp, textColorHex, bgColorHex, borderColorHex, borderWidthDp, durationMs ->
                viewModel.addOpenShotTitle(
                    title = title,
                    subtitle = subtitle,
                    template = template,
                    is3D = is3D,
                    fontSizeSp = fontSizeSp,
                    textColorHex = textColorHex,
                    bgColorHex = bgColorHex,
                    borderColorHex = borderColorHex,
                    borderWidthDp = borderWidthDp,
                    durationMs = durationMs
                )
            }
        )
    }

    // High-Performance Frame-Accurate Video Scrubber & Jog Dial
    if (uiState.showPrecisionScrubber && uiState.selectedClip != null) {
        val clip = uiState.selectedClip!!
        PrecisionVideoScrubberSheet(
            clip = clip,
            currentPlayheadMs = uiState.currentPositionMs,
            fps = 30,
            onDismiss = { viewModel.togglePrecisionScrubber(false) },
            onSeek = { viewModel.seekTo(it) },
            onApplyTrim = { inMs, outMs ->
                viewModel.updateTrim(clip.id, inMs, outMs)
                viewModel.togglePrecisionScrubber(false)
            }
        )
    }

    // DaVinci Resolve Style Professional Color Grading Studio Sheet
    if (uiState.showColorGradingSheet) {
        val targetClip = uiState.selectedClip ?: uiState.clips.firstOrNull()
        if (targetClip != null) {
            ColorGradingSheet(
                clip = targetClip,
                onUpdateGrading = { settings ->
                    viewModel.updateColorGrading(targetClip.id, settings)
                },
                onResetGrading = {
                    viewModel.resetColorGrading(targetClip.id)
                },
                isCompareSplit = uiState.isColorCompareSplit,
                onToggleCompareSplit = {
                    viewModel.toggleColorCompareSplit()
                },
                onDismiss = { viewModel.toggleColorGradingSheet(false) }
            )
        }
    }

    // AI Magic Video Studio Suite Sheet
    if (uiState.showAiStudioSheet) {
        AiVideoStudioSheet(
            uiState = uiState,
            onExecuteSilenceRemoval = { clipId, thresholdDb ->
                viewModel.executeAiSilenceRemoval(clipId, thresholdDb)
            },
            onGenerateAutoCaptions = { preset ->
                viewModel.generateAiAutoCaptions(preset)
            },
            onApplySmartReframe = { targetAspect ->
                viewModel.applyAiSmartReframe(targetAspect)
            },
            onDetectSceneCuts = { clipId ->
                viewModel.detectAiSceneCuts(clipId)
            },
            onEnhanceVoice = { clipId ->
                viewModel.enhanceAiVoice(clipId)
            },
            onDismiss = { viewModel.toggleAiStudioSheet(false) },
            onExecuteCopilotPrompt = { prompt ->
                viewModel.executeAiCopilotPrompt(prompt)
                viewModel.toggleAiStudioSheet(false)
            },
            onSeekToTime = { timeMs ->
                viewModel.seekTo(timeMs)
            },
            onRippleCutTranscript = { startMs, endMs, text ->
                viewModel.rippleCutTranscriptSegment(startMs, endMs, text)
            },
            onRemoveFillerWords = {
                viewModel.removeTranscriptFillerWords()
            },
            onSwitchMulticamAngle = { label, filter ->
                viewModel.switchMulticamAngle(label, filter)
            }
        )
    }

    // Room Editing History & Layer Metadata Sheet
    if (uiState.showHistorySheet) {
        ProjectHistorySheet(
            project = uiState.currentProject,
            history = uiState.editHistory,
            layers = uiState.projectLayers,
            currentPlayheadMs = uiState.currentPositionMs,
            onRestoreHistoryStep = { viewModel.restoreFromHistoryStep(it) },
            onToggleLayerVisibility = { layerId, isVisible ->
                viewModel.updateLayerVisibilityFromHistory(layerId, isVisible)
            },
            onToggleLayerLock = { layerId, isLocked ->
                viewModel.updateLayerLockFromHistory(layerId, isLocked)
            },
            onToggleLayerMute = { layerId, isMuted ->
                viewModel.updateLayerMuteFromHistory(layerId, isMuted)
            },
            onDismiss = { viewModel.toggleHistorySheet(false) }
        )
    }

    if (showRenameDialog && uiState.currentProject != null) {
        val currentProj = uiState.currentProject!!
        RenameProjectDialog(
            currentTitle = currentProj.title,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newTitle ->
                viewModel.renameProject(currentProj.id, newTitle)
                showRenameDialog = false
            }
        )
    }
}

/**
 * DaVinci Resolve Inspired Workspace Switcher Tab Bar.
 * Allows quick fluid navigation across dedicated professional studios:
 * EDIT, COLOR, AUDIO, MOTION, AI STUDIO, and DELIVER.
 */
@Composable
private fun WorkspaceSwitcherBar(
    currentWorkspace: EditorWorkspace,
    onSelectWorkspace: (EditorWorkspace) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditorWorkspace.entries.forEach { ws ->
                val isSelected = currentWorkspace == ws
                val tintColor = if (isSelected) AccentCyan else Color.LightGray.copy(alpha = 0.7f)
                val bgColor = if (isSelected) AccentCyan.copy(alpha = 0.15f) else Color.Transparent

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = bgColor,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)) else null,
                    modifier = Modifier
                        .clickable { onSelectWorkspace(ws) }
                        .testTag("workspace_tab_${ws.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val icon = when (ws) {
                            EditorWorkspace.EDIT -> Icons.Default.ContentCut
                            EditorWorkspace.COLOR -> Icons.Default.Palette
                            EditorWorkspace.AUDIO -> Icons.Default.GraphicEq
                            EditorWorkspace.MOTION_VFX -> Icons.Default.BlurOn
                            EditorWorkspace.AI_MAGIC -> Icons.Default.AutoAwesome
                            EditorWorkspace.DELIVER -> Icons.Default.FileDownload
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = ws.title,
                            tint = tintColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = ws.title,
                            color = tintColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

