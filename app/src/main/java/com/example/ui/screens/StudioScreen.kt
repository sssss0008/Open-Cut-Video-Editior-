package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.ExportPresetEntity
import com.example.data.model.AspectRatio
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportResolution
import com.example.data.model.ExportStage
import com.example.data.model.ExportVideoCodec
import com.example.data.model.RenderedVideoItem
import com.example.data.model.SocialPlatform
import com.example.data.model.VideoClip
import com.example.ui.components.ExportPresetsSection
import com.example.ui.components.ExportPreviewDialog
import com.example.ui.components.ExportProgressComponent
import com.example.ui.components.RealExoVideoSurface
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackAudioColor
import com.example.util.Media3VideoExporter
import com.example.util.StudioExportUtilities
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class StudioUtilityDialogType {
    AUDIO_EXTRACTOR,
    ASPECT_CONVERTER,
    BOOMERANG_LOOP,
    VIDEO_COMPRESSOR,
    SUBTITLE_EXPORT,
    FCPXML_EXPORT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    uiState: EditorUiState,
    onStartExport: (ExportResolution, Int) -> Unit,
    onCancelExport: () -> Unit,
    onDeleteRenderedVideo: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onAutoFixQuality: () -> Unit = {},
    onRunAutomationWorkflow: (String) -> Unit = {},
    exportPresets: List<ExportPresetEntity> = emptyList(),
    onApplyExportPreset: (ExportPresetEntity) -> Unit = {},
    onSaveCustomExportPreset: (
        name: String,
        platform: SocialPlatform,
        aspectRatio: AspectRatio,
        resolution: ExportResolution,
        fps: Int,
        bitrate: ExportBitrate,
        codec: ExportVideoCodec,
        notes: String,
        existingId: String?
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onDeleteExportPreset: (String) -> Unit = {},
    onUpdateExportSettings: ((ExportResolution, Int, ExportBitrate, ExportVideoCodec) -> Unit)? = null,
    onStartExportWithSettings: ((ExportResolution, Int, ExportBitrate, ExportVideoCodec) -> Unit)? = null,
    onSaveExportToUri: (Uri) -> Unit = {},
    onSetAspectRatio: (AspectRatio) -> Unit = {},
    onCreateBoomerangLoop: () -> Unit = {},
    onExtractAudioToM4a: ((Result<File>) -> Unit) -> Unit = {},
    onDetachSelectedClipAudio: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedRes by remember { mutableStateOf(uiState.exportResolution) }
    var selectedFps by remember { mutableIntStateOf(uiState.exportFps) }
    var selectedBitrate by remember { mutableStateOf(uiState.exportBitrate) }
    var selectedCodec by remember { mutableStateOf(uiState.exportCodec) }
    var activeUtilityDialog by remember { mutableStateOf<StudioUtilityDialogType?>(null) }
    var playingVideoItem by remember { mutableStateOf<RenderedVideoItem?>(null) }
    var showStudioExportPreview by remember { mutableStateOf(false) }

    // Utility dialog state
    var subtitleFormat by remember { mutableStateOf("SRT") }
    var pendingSafTextContent by remember { mutableStateOf<String?>(null) }
    var extractedAudioFile by remember { mutableStateOf<File?>(null) }
    var extractedAudioError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(
        uiState.activeExportPresetId,
        uiState.exportResolution,
        uiState.exportFps,
        uiState.exportBitrate,
        uiState.exportCodec
    ) {
        selectedRes = uiState.exportResolution
        selectedFps = uiState.exportFps
        selectedBitrate = uiState.exportBitrate
        selectedCodec = uiState.exportCodec
    }

    val triggerStudioExport: (ExportResolution, Int, ExportBitrate, ExportVideoCodec) -> Unit = { res, fps, bitrate, codec ->
        if (onStartExportWithSettings != null) {
            onStartExportWithSettings(res, fps, bitrate, codec)
        } else {
            onStartExport(res, fps)
        }
    }

    val saveMp4AsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { destUri ->
        if (destUri != null) {
            onSaveExportToUri(destUri)
        }
    }

    val saveTextDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/*")
    ) { destUri ->
        val payload = pendingSafTextContent
        if (destUri != null && payload != null) {
            coroutineScope.launch {
                StudioExportUtilities.writeTextToSafUri(context, destUri, payload)
                    .onSuccess { bytes ->
                        Toast.makeText(context, "Saved export file ($bytes bytes)", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { err ->
                        Toast.makeText(context, "Save failed: ${err.message}", Toast.LENGTH_SHORT).show()
                    }
                pendingSafTextContent = null
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Export Studio & Archive", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Social presets, MP4 rendering & NLE tools", color = AccentCyan, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("studio_drawer_button")) {
                        Icon(Icons.Default.Menu, "Open Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioSurfaceDark)
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. REAL-TIME EXPORT PROGRESS OR PRESETS + RENDER CARD
            if (uiState.exportData.isExporting || uiState.exportData.completed || uiState.exportData.stage == ExportStage.FAILED) {
                item {
                    Text(
                        text = "Active Export Pipeline",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExportProgressComponent(
                        exportData = uiState.exportData,
                        onCancelExport = onCancelExport,
                        onDismissCompleted = { onCancelExport() },
                        onSaveAsClick = {
                            val defaultName = uiState.exportData.outputFileName.ifBlank { "opencut_export.mp4" }
                            saveMp4AsLauncher.launch(defaultName)
                        },
                        onPlayExportedClick = {
                            val uri = Media3VideoExporter.getShareableFileUri(context, uiState.exportData.outputFilePath)
                            if (uri != null) {
                                val playIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "video/mp4")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(playIntent, "Play Exported MP4"))
                                }.onFailure {
                                    Toast.makeText(context, "No compatible video player found.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Exported MP4 file not found.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onShareExportedClick = {
                            val uri = Media3VideoExporter.getShareableFileUri(context, uiState.exportData.outputFilePath)
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Exported MP4"))
                            } else {
                                Toast.makeText(context, "Exported MP4 file not found to share.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            } else {
                // 1A. SOCIAL MEDIA EXPORT PRESETS
                item {
                    ExportPresetsSection(
                        presets = exportPresets,
                        activePresetId = uiState.activeExportPresetId,
                        currentAspectRatio = uiState.aspectRatio,
                        currentResolution = selectedRes,
                        currentFps = selectedFps,
                        currentBitrate = selectedBitrate,
                        currentCodec = selectedCodec,
                        onApplyPreset = { preset ->
                            selectedRes = preset.resolution
                            selectedFps = preset.fps
                            selectedBitrate = preset.bitrate
                            selectedCodec = preset.videoCodec
                            onApplyExportPreset(preset)
                        },
                        onSaveCustomPreset = onSaveCustomExportPreset,
                        onDeletePreset = onDeleteExportPreset
                    )
                }

                // 1B. EXPORT TRIGGER CARD
                item {
                    val durationSec = (uiState.totalDurationMs / 1000f).coerceAtLeast(1f)
                    val codecFactor = if (selectedCodec == ExportVideoCodec.HEVC) 0.72f else 1.0f
                    val estSizeMb = ((durationSec * selectedBitrate.mbps) / 8f) * codecFactor

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudioCardDark,
                        border = BorderStroke(1.dp, Color(0xFF2C2840)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AccentCyan.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.FileDownload, "Export", tint = AccentCyan, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Render Current Project", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${uiState.currentProject?.title ?: "OpenCut Project"} • ${uiState.aspectRatio.label.split(" ").first()}",
                                            color = AccentCyan,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                TextButton(onClick = onNavigateToEditor) {
                                    Text("Edit Timeline", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Resolution Chips
                            Text("Output Resolution:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExportResolution.entries.forEach { res ->
                                    val isSelected = selectedRes == res
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF381E72) else Color(0xFF1B182B),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) AccentCyan else Color(0xFF2E2A48)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedRes = res
                                                onUpdateExportSettings?.invoke(res, selectedFps, selectedBitrate, selectedCodec)
                                            }
                                            .testTag("studio_res_${res.name}")
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            Text(
                                                res.label.split(" ").first(),
                                                color = if (isSelected) Color.White else Color.LightGray,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("${res.width}x${res.height}", color = Color.Gray, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }

                            // FPS Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(24, 30, 60).forEach { fps ->
                                    val isSelected = selectedFps == fps
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AccentCyan else Color(0xFF1B182B),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedFps = fps
                                                onUpdateExportSettings?.invoke(selectedRes, fps, selectedBitrate, selectedCodec)
                                            }
                                            .testTag("studio_fps_$fps")
                                    ) {
                                        Text(
                                            text = "$fps FPS",
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            // Bitrate Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ExportBitrate.entries.forEach { br ->
                                    val isSelected = selectedBitrate == br
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Color(0xFF381E72) else Color(0xFF1B182B),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) AccentCyan else Color(0xFF2E2A48)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedBitrate = br
                                                onUpdateExportSettings?.invoke(selectedRes, selectedFps, br, selectedCodec)
                                            }
                                            .testTag("studio_bitrate_${br.name}")
                                    ) {
                                        Text(
                                            text = "${br.mbps} Mbps",
                                            color = if (isSelected) Color.White else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            // Real Hardware Codec Chips (H.264 / HEVC H.265)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExportVideoCodec.entries.forEach { codec ->
                                    val isSelected = selectedCodec == codec
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AccentEmerald.copy(alpha = 0.22f) else Color(0xFF1B182B),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) AccentEmerald else Color(0xFF2E2A48)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedCodec = codec
                                                onUpdateExportSettings?.invoke(selectedRes, selectedFps, selectedBitrate, codec)
                                            }
                                            .testTag("codec_chip_${codec.name.lowercase()}")
                                    ) {
                                        Text(
                                            text = codec.label,
                                            color = if (isSelected) AccentEmerald else Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Est. Output: ~%.1f MB • %s • %d FPS • %d Mbps",
                                    estSizeMb,
                                    selectedCodec.label,
                                    selectedFps,
                                    selectedBitrate.mbps
                                ),
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 6.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showStudioExportPreview = true },
                                    enabled = uiState.clips.isNotEmpty(),
                                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.75f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("studio_export_preview_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, "Preview Export", tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Preview", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { triggerStudioExport(selectedRes, selectedFps, selectedBitrate, selectedCodec) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(48.dp)
                                        .testTag("studio_start_export_button")
                                ) {
                                    Icon(Icons.Default.FileDownload, "Export", tint = Color.Black, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export MP4", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 1C. PRE-EXPORT PROJECT QUALITY ASSISTANT & AUTOMATION WORKFLOW RUNNER
            item {
                val missingTransitionsCount = uiState.clips.drop(1).count { it.transitionIn.name == "NONE" }
                val isDuckingReady = uiState.isAutoDuckingMasterEnabled
                val qualityScore = (100 - missingTransitionsCount * 10 - if (!isDuckingReady) 15 else 0).coerceIn(65, 100)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioCardDark,
                    border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_quality_assistant_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pre-Export Project Quality Assistant",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automated timeline inspection for gaps, audio ducking & transitions",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentEmerald.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "$qualityScore% READY",
                                    color = AccentEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Clips: ${uiState.clips.size} ready" to (uiState.clips.isNotEmpty()),
                                "Auto-Ducking: ${if (isDuckingReady) "ON" else "OFF"}" to isDuckingReady,
                                "Transitions: ${if (missingTransitionsCount == 0) "All Smooth" else "$missingTransitionsCount Hard Cuts"}" to (missingTransitionsCount == 0)
                            ).forEach { (checkLabel, passed) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (passed) Color(0xFF0F2922) else Color(0xFF2B1D16),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = checkLabel,
                                        color = if (passed) AccentEmerald else Color(0xFFFFB300),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onAutoFixQuality,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("auto_fix_quality_button")
                            ) {
                                Text("Auto-Fix Issues", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    onRunAutomationWorkflow("Fast TikTok viral cut with neon captions, beat sync & teal-orange LUT")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("run_macro_workflow_button")
                            ) {
                                Text("Run Pro Macro", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. RENDERED VIDEOS ARCHIVE (PHONE STORAGE)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, "Storage", tint = AccentEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rendered Videos on Phone (${uiState.renderedVideos.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Persisted on Disk",
                        color = AccentEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (uiState.renderedVideos.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioCardDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Movie, "No Videos", tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text(
                                text = "No exported MP4 files yet",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "When you export projects, the rendered MP4 videos are saved right here on your phone.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(uiState.renderedVideos, key = { it.id }) { video ->
                    RenderedVideoCard(
                        video = video,
                        formattedDate = dateFormat.format(Date(video.createdAt)),
                        onPlay = { playingVideoItem = video },
                        onShare = {
                            val shareUri = if (video.localUri.startsWith("content://")) {
                                Uri.parse(video.localUri)
                            } else {
                                Media3VideoExporter.getShareableFileUri(context, video.localUri)
                            }
                            if (shareUri != null) {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
                            } else {
                                Toast.makeText(context, "Exported MP4 file not found on device.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = { onDeleteRenderedVideo(video.id) }
                    )
                }
            }

            // 3. FUNCTIONAL STUDIO UTILITIES & DESKTOP NLE INTERCHANGE SUITE
            item {
                Text(
                    text = "Studio Utilities & Desktop Interchange",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioUtilityCard(
                            title = "Audio Extractor",
                            desc = "Export M4A or detach clip audio",
                            icon = Icons.Default.Audiotrack,
                            accent = TrackAudioColor,
                            onClick = {
                                extractedAudioFile = null
                                extractedAudioError = null
                                activeUtilityDialog = StudioUtilityDialogType.AUDIO_EXTRACTOR
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("utility_audio_extractor")
                        )
                        StudioUtilityCard(
                            title = "Aspect Converter",
                            desc = "Switch 16:9, 9:16, 4:5 & 1:1",
                            icon = Icons.Default.AspectRatio,
                            accent = AccentCyan,
                            onClick = { activeUtilityDialog = StudioUtilityDialogType.ASPECT_CONVERTER },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("utility_aspect_converter")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioUtilityCard(
                            title = "Boomerang Loop",
                            desc = "Build forward + reverse clip loop",
                            icon = Icons.Default.Loop,
                            accent = AccentPink,
                            onClick = { activeUtilityDialog = StudioUtilityDialogType.BOOMERANG_LOOP },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("utility_boomerang_loop")
                        )
                        StudioUtilityCard(
                            title = "Video Compressor",
                            desc = "Export compact 480p/720p MP4",
                            icon = Icons.Default.Speed,
                            accent = AccentEmerald,
                            onClick = { activeUtilityDialog = StudioUtilityDialogType.VIDEO_COMPRESSOR },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("utility_video_compressor")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioUtilityCard(
                            title = "SRT / VTT Export",
                            desc = "Save real subtitle caption file",
                            icon = Icons.Default.FileDownload,
                            accent = AccentCyan,
                            onClick = { activeUtilityDialog = StudioUtilityDialogType.SUBTITLE_EXPORT },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("utility_subtitle_export")
                        )
                        StudioUtilityCard(
                            title = "FCPXML Export",
                            desc = "DaVinci & Final Cut XML file",
                            icon = Icons.Default.Share,
                            accent = Color(0xFF8B5CF6),
                            onClick = { activeUtilityDialog = StudioUtilityDialogType.FCPXML_EXPORT },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("utility_fcpxml_export")
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    // FUNCTIONAL STUDIO UTILITY DIALOGS
    when (activeUtilityDialog) {
        StudioUtilityDialogType.AUDIO_EXTRACTOR -> {
            Dialog(onDismissRequest = { activeUtilityDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, TrackAudioColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_utility_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Audio Extractor & Detacher", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Extract the timeline's mixed audio track to a shareable AAC (.m4a) file via Media3, or detach the selected clip's audio onto your timeline.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        if (uiState.isExtractingAudio) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                CircularProgressIndicator(color = TrackAudioColor, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text("Extracting AAC audio track...", color = AccentCyan, fontSize = 12.sp)
                            }
                        }

                        extractedAudioFile?.let { audioFile ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F2922),
                                border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Extracted ${audioFile.name}", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("${audioFile.length() / 1024} KB • AAC M4A", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                    TextButton(onClick = {
                                        val shareUri = Media3VideoExporter.getShareableFileUri(context, audioFile.absolutePath)
                                        if (shareUri != null) {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "audio/mp4"
                                                putExtra(Intent.EXTRA_STREAM, shareUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Audio M4A"))
                                        }
                                    }) {
                                        Text("Share", color = AccentCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        extractedAudioError?.let { err ->
                            Text(err, color = AccentPink, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                extractedAudioError = null
                                onExtractAudioToM4a { result ->
                                    result.onSuccess { file ->
                                        extractedAudioFile = file
                                    }.onFailure { err ->
                                        extractedAudioError = err.message ?: "Audio extraction failed."
                                    }
                                }
                            },
                            enabled = uiState.clips.isNotEmpty() && !uiState.isExtractingAudio,
                            colors = ButtonDefaults.buttonColors(containerColor = TrackAudioColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("extract_m4a_action_button")
                        ) {
                            Text("Extract Project Audio (.M4A)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onDetachSelectedClipAudio()
                                activeUtilityDialog = null
                                onNavigateToEditor()
                            },
                            enabled = uiState.clips.isNotEmpty(),
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detach_clip_audio_action_button")
                        ) {
                            Text("Detach Clip Audio to Timeline", color = AccentCyan, fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { activeUtilityDialog = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close", color = Color.LightGray)
                        }
                    }
                }
            }
        }

        StudioUtilityDialogType.ASPECT_CONVERTER -> {
            Dialog(onDismissRequest = { activeUtilityDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_utility_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Aspect Ratio Converter", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Reformat canvas immediately for your target platform:",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        AspectRatio.entries.forEach { aspect ->
                            val isSelected = uiState.aspectRatio == aspect
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentCyan.copy(alpha = 0.18f) else StudioCardDark,
                                border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C2840)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSetAspectRatio(aspect)
                                        Toast.makeText(context, "Canvas updated to ${aspect.label}", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("aspect_dialog_option_${aspect.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(aspect.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    if (isSelected) {
                                        Text("ACTIVE", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { activeUtilityDialog = null },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Done", color = Color.LightGray)
                            }
                            Button(
                                onClick = {
                                    activeUtilityDialog = null
                                    onNavigateToEditor()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Open Editor", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        StudioUtilityDialogType.BOOMERANG_LOOP -> {
            val targetClip = uiState.clips.find { it.id == uiState.selectedClipId } ?: uiState.clips.firstOrNull()
            Dialog(onDismissRequest = { activeUtilityDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, AccentPink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_utility_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Boomerang & Loop Maker", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (targetClip != null) {
                                "Creates a snappy 1.5x forward + reversed clip loop from '${targetClip.name}' directly on your timeline with a smooth dissolve."
                            } else {
                                "Import or add a video clip on your timeline first to build a Boomerang loop."
                            },
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { activeUtilityDialog = null },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Cancel", color = Color.LightGray)
                            }
                            Button(
                                onClick = {
                                    onCreateBoomerangLoop()
                                    activeUtilityDialog = null
                                    onNavigateToEditor()
                                },
                                enabled = targetClip != null,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("create_boomerang_confirm_button")
                            ) {
                                Text("Create Loop", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        StudioUtilityDialogType.VIDEO_COMPRESSOR -> {
            val durationSec = (uiState.totalDurationMs / 1000f).coerceAtLeast(1f)
            val profiles = listOf(
                Triple("Compact Chat (480p • 2 Mbps H.264)", ExportResolution.RES_480P, Pair(ExportBitrate.LOW_2M, ExportVideoCodec.H264)),
                Triple("Social Saver (720p • 2 Mbps HEVC)", ExportResolution.RES_720P, Pair(ExportBitrate.LOW_2M, ExportVideoCodec.HEVC)),
                Triple("Balanced HD (1080p • 5 Mbps HEVC)", ExportResolution.RES_1080P, Pair(ExportBitrate.MEDIUM_5M, ExportVideoCodec.HEVC))
            )
            Dialog(onDismissRequest = { activeUtilityDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, AccentEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_utility_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Smart Video Compressor", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Pick a compression profile to render a lightweight MP4 right now via Media3 Transformer:",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        profiles.forEachIndexed { idx, (label, res, spec) ->
                            val (br, codec) = spec
                            val estMb = ((durationSec * br.mbps) / 8f) * if (codec == ExportVideoCodec.HEVC) 0.72f else 1.0f
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudioCardDark,
                                border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = uiState.clips.isNotEmpty()) {
                                        activeUtilityDialog = null
                                        triggerStudioExport(res, 30, br, codec)
                                    }
                                    .testTag("compress_profile_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = String.format(Locale.US, "Estimated size: ~%.1f MB", estMb),
                                            color = AccentEmerald,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Text("RENDER", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                        if (uiState.clips.isEmpty()) {
                            Text("Add a video clip on the timeline to compress.", color = AccentPink, fontSize = 11.sp)
                        }
                        TextButton(
                            onClick = { activeUtilityDialog = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close", color = Color.LightGray)
                        }
                    }
                }
            }
        }

        StudioUtilityDialogType.SUBTITLE_EXPORT -> {
            val projectTitle = uiState.currentProject?.title ?: "OpenCut_Project"
            val safeBase = projectTitle.lowercase(Locale.US).replace("[^a-z0-9]+".toRegex(), "_").trim('_').ifBlank { "opencut" }
            val captionContent = if (subtitleFormat == "SRT") {
                StudioExportUtilities.generateSrtContent(uiState.textOverlays, projectTitle)
            } else {
                StudioExportUtilities.generateVttContent(uiState.textOverlays, projectTitle)
            }
            val fileExt = if (subtitleFormat == "SRT") "srt" else "vtt"
            val fileName = "${safeBase}_captions.$fileExt"

            Dialog(onDismissRequest = { activeUtilityDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_utility_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Subtitle Caption Export", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("SRT", "VTT").forEach { fmt ->
                                val sel = subtitleFormat == fmt
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (sel) AccentCyan else StudioCardDark,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { subtitleFormat = fmt }
                                        .testTag("subtitle_fmt_$fmt")
                                ) {
                                    Text(
                                        text = if (fmt == "SRT") "SubRip (.srt)" else "WebVTT (.vtt)",
                                        color = if (sel) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0B0F19),
                            border = BorderStroke(1.dp, Color(0xFF28354E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                        ) {
                            Text(
                                text = captionContent,
                                color = AccentEmerald,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(10.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    pendingSafTextContent = captionContent
                                    saveTextDocumentLauncher.launch(fileName)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_subtitle_file_button")
                            ) {
                                Text("Save File", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        StudioExportUtilities.writeStudioTextExport(context, fileName, captionContent)
                                            .onSuccess { file ->
                                                val uri = Media3VideoExporter.getShareableFileUri(context, file.absolutePath)
                                                if (uri != null) {
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = if (fileExt == "vtt") "text/vtt" else "application/x-subrip"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "Share $fileName"))
                                                }
                                            }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("share_subtitle_file_button")
                            ) {
                                Text("Share .$fileExt", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        TextButton(
                            onClick = { activeUtilityDialog = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close", color = Color.LightGray)
                        }
                    }
                }
            }
        }

        StudioUtilityDialogType.FCPXML_EXPORT -> {
            val projectTitle = uiState.currentProject?.title ?: "OpenCut_Project"
            val safeBase = projectTitle.lowercase(Locale.US).replace("[^a-z0-9]+".toRegex(), "_").trim('_').ifBlank { "opencut" }
            val fcpxmlContent = StudioExportUtilities.generateFcpxmlContent(
                project = uiState.currentProject,
                clips = uiState.clips,
                audioClips = uiState.audioClips,
                textOverlays = uiState.textOverlays,
                aspectRatio = uiState.aspectRatio,
                fps = selectedFps
            )
            val fileName = "${safeBase}_timeline.fcpxml"

            Dialog(onDismissRequest = { activeUtilityDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_utility_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("FCPXML v1.10 Timeline Export", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Compatible with Final Cut Pro, DaVinci Resolve & Premiere Pro.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0B0F19),
                            border = BorderStroke(1.dp, Color(0xFF28354E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            Text(
                                text = fcpxmlContent,
                                color = AccentEmerald,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(10.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    pendingSafTextContent = fcpxmlContent
                                    saveTextDocumentLauncher.launch(fileName)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_fcpxml_file_button")
                            ) {
                                Text("Save XML", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        StudioExportUtilities.writeStudioTextExport(context, fileName, fcpxmlContent)
                                            .onSuccess { file ->
                                                val uri = Media3VideoExporter.getShareableFileUri(context, file.absolutePath)
                                                if (uri != null) {
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/xml"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "Share $fileName"))
                                                }
                                            }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("share_fcpxml_file_button")
                            ) {
                                Text("Share XML", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        TextButton(
                            onClick = { activeUtilityDialog = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close", color = Color.LightGray)
                        }
                    }
                }
            }
        }

        null -> Unit
    }

    // Video Player Playback Dialog
    if (playingVideoItem != null) {
        val video = playingVideoItem!!
        Dialog(onDismissRequest = { playingVideoItem = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF141222),
                border = BorderStroke(1.dp, AccentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(video.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { playingVideoItem = null }) {
                            Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        val previewClip = remember(video.id, video.localUri) {
                            VideoClip(
                                id = video.id,
                                name = video.fileName,
                                uri = video.localUri,
                                sourceDurationMs = video.durationMs.coerceAtLeast(1000L)
                            )
                        }
                        RealExoVideoSurface(
                            clip = previewClip,
                            currentPositionMs = 0L,
                            isPlaying = true,
                            playbackSpeed = 1.0f,
                            isVideoTrackMuted = false,
                            masterVolume = 1.0f,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${video.resolution} • ${video.fps} FPS • ${VideoEditorViewModel.formatTime(video.durationMs)} • ${String.format(Locale.US, "%.1f MB", video.fileSizeBytes / (1024f * 1024f))}",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { playingVideoItem = null },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Close Player", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showStudioExportPreview) {
        ExportPreviewDialog(
            uiState = uiState,
            selectedResolution = selectedRes,
            selectedFps = selectedFps,
            selectedBitrate = selectedBitrate,
            onDismiss = { showStudioExportPreview = false },
            onConfirmExport = {
                showStudioExportPreview = false
                triggerStudioExport(selectedRes, selectedFps, selectedBitrate, selectedCodec)
            }
        )
    }
}

@Composable
fun RenderedVideoCard(
    video: RenderedVideoItem,
    formattedDate: String,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = StudioCardDark,
        border = BorderStroke(1.dp, Color(0xFF2C2840)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rendered_video_${video.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))))
                    .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(video.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    text = "${video.resolution} • ${video.fps}fps • ${String.format(Locale.US, "%.1fMB", video.fileSizeBytes / (1024f * 1024f))}",
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = formattedDate,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row {
                IconButton(onClick = onShare, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Share, "Share", tint = AccentCyan, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun StudioUtilityCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = StudioCardDark,
        border = BorderStroke(1.dp, Color(0xFF2C2840)),
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, title, tint = accent, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
