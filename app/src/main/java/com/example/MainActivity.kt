package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.EditorWorkspace
import com.example.ui.components.AppNavigationDrawer
import com.example.ui.components.PlaybackVerificationDialog
import com.example.ui.screens.AccountAnalyticsScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ProjectsDashboardScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.OpenCutTheme
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "nav_tab_home"),
    EDIT("Edit", Icons.Default.ContentCut, "nav_tab_edit"),
    LIBRARY("Library", Icons.Default.VideoLibrary, "nav_tab_library"),
    STUDIO("Studio", Icons.Default.AutoFixHigh, "nav_tab_studio"),
    ACCOUNT("Account", Icons.Default.AccountCircle, "nav_tab_account")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenCutTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioDarkBg
                ) {
                    val viewModel: VideoEditorViewModel = viewModel()
                    val projects by viewModel.projects.collectAsState()
                    val importedMedia by viewModel.importedMedia.collectAsState()
                    val exportPresets by viewModel.exportPresets.collectAsState()
                    val uiState by viewModel.uiState.collectAsState()

                    var currentTab by remember { mutableStateOf(AppTab.HOME) }
                    var showExportFullScreen by remember { mutableStateOf(false) }

                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()

                    // Back handler for drawer or tabs
                    BackHandler(enabled = drawerState.isOpen || currentTab != AppTab.HOME || showExportFullScreen) {
                        if (drawerState.isOpen) {
                            coroutineScope.launch { drawerState.close() }
                        } else if (showExportFullScreen) {
                            showExportFullScreen = false
                        } else {
                            currentTab = AppTab.HOME
                        }
                    }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppNavigationDrawer(
                                projectCount = projects.size,
                                renderedVideoCount = uiState.renderedVideos.size,
                                storageUsageBytes = uiState.storageUsageBytes,
                                isSnappingEnabled = uiState.isSnappingEnabled,
                                onToggleSnapping = { enabled ->
                                    if (uiState.isSnappingEnabled != enabled) {
                                        viewModel.toggleSnapping()
                                    }
                                },
                                onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                                onNavigateTab = { tabIdx ->
                                    currentTab = AppTab.entries[tabIdx]
                                },
                                onOpenTransitions = {
                                    currentTab = AppTab.EDIT
                                    if (uiState.currentProject == null && projects.isNotEmpty()) {
                                        viewModel.openProject(projects.first().id)
                                    }
                                    viewModel.toggleTransitionsLibraryPanel(true)
                                },
                                onOpenColorStudio = {
                                    currentTab = AppTab.EDIT
                                    if (uiState.currentProject == null && projects.isNotEmpty()) {
                                        viewModel.openProject(projects.first().id)
                                    }
                                    viewModel.setWorkspace(EditorWorkspace.COLOR)
                                },
                                onClearCache = {
                                    viewModel.clearTempCacheBuffers()
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            bottomBar = {
                                if (!showExportFullScreen) {
                                    NavigationBar(
                                        containerColor = StudioSurfaceDark,
                                        contentColor = Color.White
                                    ) {
                                        AppTab.entries.forEach { tab ->
                                            val isSelected = currentTab == tab
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = {
                                                    currentTab = tab
                                                    // If switching to edit and no project opened yet, open first project
                                                    if (tab == AppTab.EDIT && uiState.currentProject == null && projects.isNotEmpty()) {
                                                        viewModel.openProject(projects.first().id)
                                                    }
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = tab.icon,
                                                        contentDescription = tab.title,
                                                        tint = if (isSelected) AccentCyan else Color.LightGray
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = tab.title,
                                                        color = if (isSelected) AccentCyan else Color.LightGray,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    indicatorColor = AccentCyan.copy(alpha = 0.18f)
                                                ),
                                                modifier = Modifier.testTag(tab.tag)
                                            )
                                        }
                                    }
                                }
                            },
                            containerColor = StudioDarkBg
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                if (showExportFullScreen) {
                                    ExportScreen(
                                        uiState = uiState,
                                        onBack = { showExportFullScreen = false },
                                        onStartExport = { resolution, fps ->
                                            viewModel.startRealtimeExport(resolution, fps)
                                        },
                                        onStartExportWithBitrate = { resolution, fps, bitrate ->
                                            viewModel.startRealtimeExport(resolution, fps, bitrate)
                                        },
                                        onStartExportWithSettings = { resolution, fps, bitrate, codec ->
                                            viewModel.startRealtimeExport(resolution, fps, bitrate, codec)
                                        },
                                        onSaveExportToUri = { destUri ->
                                            viewModel.exportToSafDestination(destUri)
                                        },
                                        onCancelExport = {
                                            viewModel.cancelExport()
                                        },
                                        exportPresets = exportPresets,
                                        onApplyExportPreset = { preset ->
                                            viewModel.applyExportPreset(preset)
                                        },
                                        onSaveCustomExportPreset = { name, platform, aspect, resolution, fps, bitrate, codec, notes, existingId ->
                                            viewModel.saveCustomExportPreset(
                                                name = name,
                                                platform = platform,
                                                aspectRatio = aspect,
                                                resolution = resolution,
                                                fps = fps,
                                                bitrate = bitrate,
                                                codec = codec,
                                                notes = notes,
                                                existingId = existingId
                                            )
                                        },
                                        onDeleteExportPreset = { presetId ->
                                            viewModel.deleteCustomExportPreset(presetId)
                                        },
                                        onUpdateExportSettings = { resolution, fps, bitrate, codec ->
                                            viewModel.setExportSettings(resolution, fps, bitrate, codec)
                                        },
                                        onSetAspectRatio = { aspect ->
                                            viewModel.setAspectRatio(aspect)
                                        }
                                    )
                                } else {
                                    when (currentTab) {
                                        AppTab.HOME -> {
                                            ProjectsDashboardScreen(
                                                projects = projects,
                                                onOpenProject = { projectId ->
                                                    viewModel.openProject(projectId)
                                                    currentTab = AppTab.EDIT
                                                },
                                                onCreateProject = { title, aspect, withSamples ->
                                                    viewModel.createProject(title, aspect, withSampleContent = withSamples)
                                                    currentTab = AppTab.EDIT
                                                },
                                                onImportDeviceVideo = { uri, name ->
                                                    viewModel.createProjectFromDeviceMedia(uri, name) {
                                                        currentTab = AppTab.EDIT
                                                    }
                                                },
                                                onImportMultipleVideos = { uris ->
                                                    viewModel.importMultipleDeviceVideos(
                                                        uris = uris,
                                                        addToCurrentTimeline = false,
                                                        openVerificationOnFinish = true
                                                    ) { importedList ->
                                                        if (importedList.size > 1) {
                                                            viewModel.requestPlaybackVerification(importedList.first())
                                                        }
                                                    }
                                                },
                                                importProgress = uiState.importProgress,
                                                onCancelImport = { viewModel.cancelImport() },
                                                onClearImportError = { viewModel.clearImportError() },
                                                onGenerateTestVideo = {
                                                    viewModel.createProjectFromDeviceTestVideo {
                                                        currentTab = AppTab.EDIT
                                                    }
                                                },
                                                onDuplicateProject = { projectId ->
                                                    viewModel.duplicateProject(projectId)
                                                },
                                                onDeleteProject = { projectId ->
                                                    viewModel.deleteProject(projectId)
                                                },
                                                onRenameProject = { projectId, newTitle ->
                                                    viewModel.renameProject(projectId, newTitle)
                                                },
                                                onOpenDrawer = {
                                                    coroutineScope.launch { drawerState.open() }
                                                },
                                                onNavigateAccount = {
                                                    currentTab = AppTab.ACCOUNT
                                                }
                                            )
                                        }

                                        AppTab.EDIT -> {
                                            EditorScreen(
                                                uiState = uiState,
                                                viewModel = viewModel,
                                                onBack = {
                                                    viewModel.pause()
                                                    viewModel.flushAutoSave()
                                                    currentTab = AppTab.HOME
                                                },
                                                onNavigateExport = {
                                                    viewModel.pause()
                                                    viewModel.flushAutoSave()
                                                    showExportFullScreen = true
                                                }
                                            )
                                        }

                                        AppTab.LIBRARY -> {
                                            LibraryScreen(
                                                importedMedia = importedMedia,
                                                importProgress = uiState.importProgress,
                                                onImportMedia = { uri, name, type ->
                                                    viewModel.importDeviceMedia(uri, name, type)
                                                },
                                                onImportMultipleVideos = { uris ->
                                                    viewModel.importMultipleDeviceVideos(
                                                        uris = uris,
                                                        addToCurrentTimeline = false
                                                    )
                                                },
                                                onCancelImport = { viewModel.cancelImport() },
                                                onClearImportError = { viewModel.clearImportError() },
                                                onDeleteImportedMedia = { id ->
                                                    viewModel.deleteImportedMedia(id)
                                                },
                                                onAddImportedToTimeline = { media ->
                                                    if (media.mediaType.equals("VIDEO", ignoreCase = true) && !media.isPlaybackVerified) {
                                                        viewModel.requestPlaybackVerification(media)
                                                    } else {
                                                        viewModel.addImportedMediaToTimeline(media)
                                                        currentTab = AppTab.EDIT
                                                    }
                                                },
                                                onAddSampleClip = { sample ->
                                                    viewModel.addSampleClip(sample)
                                                    currentTab = AppTab.EDIT
                                                },
                                                onAddAudio = { audio ->
                                                    viewModel.addAudioTrack(audio)
                                                    currentTab = AppTab.EDIT
                                                },
                                                onAddSticker = { emoji, label ->
                                                    viewModel.addSticker(emoji, label)
                                                    currentTab = AppTab.EDIT
                                                },
                                                onAddTextTitle = { text, preset ->
                                                    viewModel.addTextOverlay(text, preset)
                                                    currentTab = AppTab.EDIT
                                                },
                                                onOpenDrawer = {
                                                    coroutineScope.launch { drawerState.open() }
                                                }
                                            )
                                        }

                                        AppTab.STUDIO -> {
                                            StudioScreen(
                                                uiState = uiState,
                                                onStartExport = { resolution, fps ->
                                                    viewModel.startRealtimeExport(resolution, fps)
                                                },
                                                onCancelExport = {
                                                    viewModel.cancelExport()
                                                },
                                                onDeleteRenderedVideo = { id ->
                                                    viewModel.deleteRenderedVideo(id)
                                                },
                                                onOpenDrawer = {
                                                    coroutineScope.launch { drawerState.open() }
                                                },
                                                onNavigateToEditor = {
                                                    currentTab = AppTab.EDIT
                                                },
                                                onAutoFixQuality = {
                                                    viewModel.autoFixProjectQualityIssues()
                                                },
                                                onRunAutomationWorkflow = { macroPrompt ->
                                                    viewModel.executeAiCopilotPrompt(macroPrompt)
                                                    currentTab = AppTab.EDIT
                                                },
                                                exportPresets = exportPresets,
                                                onApplyExportPreset = { preset ->
                                                    viewModel.applyExportPreset(preset)
                                                },
                                                onSaveCustomExportPreset = { name, platform, aspect, resolution, fps, bitrate, codec, notes, existingId ->
                                                    viewModel.saveCustomExportPreset(
                                                        name = name,
                                                        platform = platform,
                                                        aspectRatio = aspect,
                                                        resolution = resolution,
                                                        fps = fps,
                                                        bitrate = bitrate,
                                                        codec = codec,
                                                        notes = notes,
                                                        existingId = existingId
                                                    )
                                                },
                                                onDeleteExportPreset = { presetId ->
                                                    viewModel.deleteCustomExportPreset(presetId)
                                                },
                                                onUpdateExportSettings = { resolution, fps, bitrate, codec ->
                                                    viewModel.setExportSettings(resolution, fps, bitrate, codec)
                                                },
                                                onStartExportWithSettings = { resolution, fps, bitrate, codec ->
                                                    viewModel.startRealtimeExport(resolution, fps, bitrate, codec)
                                                },
                                                onSaveExportToUri = { destUri ->
                                                    viewModel.exportToSafDestination(destUri)
                                                },
                                                onSetAspectRatio = { aspect ->
                                                    viewModel.setAspectRatio(aspect)
                                                },
                                                onCreateBoomerangLoop = {
                                                    viewModel.createBoomerangLoopOnTimeline()
                                                },
                                                onExtractAudioToM4a = { onComplete ->
                                                    viewModel.extractProjectAudioToM4a(onComplete)
                                                },
                                                onDetachSelectedClipAudio = {
                                                    val targetId = uiState.selectedClipId ?: uiState.clips.firstOrNull()?.id
                                                    if (targetId != null) {
                                                        viewModel.detachAudio(targetId)
                                                    }
                                                }
                                            )
                                        }

                                        AppTab.ACCOUNT -> {
                                            AccountAnalyticsScreen(
                                                projects = projects,
                                                uiState = uiState,
                                                onOpenDrawer = {
                                                    coroutineScope.launch { drawerState.open() }
                                                },
                                                onOpenProject = { projectId ->
                                                    viewModel.openProject(projectId)
                                                    currentTab = AppTab.EDIT
                                                }
                                            )
                                        }
                                    }
                                }

                                uiState.pendingVerificationMedia?.let { pendingMedia ->
                                    PlaybackVerificationDialog(
                                        media = pendingMedia,
                                        onVerifiedAndEnterEditor = { verifiedMedia ->
                                            viewModel.confirmMediaVerifiedAndEnterEditor(
                                                media = verifiedMedia,
                                                createNewProjectIfNone = currentTab == AppTab.HOME || uiState.currentProject == null
                                            ) {
                                                currentTab = AppTab.EDIT
                                            }
                                        },
                                        onCancel = {
                                            viewModel.dismissVerificationDialog()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
