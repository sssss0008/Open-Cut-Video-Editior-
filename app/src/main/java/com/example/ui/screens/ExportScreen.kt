package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.db.ExportPresetEntity
import com.example.data.model.AspectRatio
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportResolution
import com.example.data.model.ExportStage
import com.example.data.model.ExportVideoCodec
import com.example.data.model.SocialPlatform
import com.example.ui.components.ExportPresetsSection
import com.example.ui.components.ExportPreviewDialog
import com.example.ui.components.ExportProgressComponent
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.util.Media3VideoExporter
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    uiState: EditorUiState,
    onBack: () -> Unit,
    onStartExport: (resolution: ExportResolution, fps: Int) -> Unit,
    onCancelExport: () -> Unit = {},
    onStartExportWithBitrate: ((resolution: ExportResolution, fps: Int, bitrate: ExportBitrate) -> Unit)? = null,
    onStartExportWithSettings: ((resolution: ExportResolution, fps: Int, bitrate: ExportBitrate, codec: ExportVideoCodec) -> Unit)? = null,
    onSaveExportToUri: (Uri) -> Unit = {},
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
    onSetAspectRatio: (AspectRatio) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedResolution by remember { mutableStateOf(uiState.exportResolution) }
    var selectedFps by remember { mutableIntStateOf(uiState.exportFps) }
    var selectedBitrate by remember { mutableStateOf(uiState.exportBitrate) }
    var selectedCodec by remember { mutableStateOf(uiState.exportCodec) }
    var showExportPreviewDialog by remember { mutableStateOf(false) }

    val isHevcSupported = remember { Media3VideoExporter.isHevcHardwareEncodingSupported() }

    LaunchedEffect(
        uiState.activeExportPresetId,
        uiState.exportResolution,
        uiState.exportFps,
        uiState.exportBitrate,
        uiState.exportCodec
    ) {
        selectedResolution = uiState.exportResolution
        selectedFps = uiState.exportFps
        selectedBitrate = uiState.exportBitrate
        selectedCodec = uiState.exportCodec
    }

    val triggerExport = {
        when {
            onStartExportWithSettings != null ->
                onStartExportWithSettings(selectedResolution, selectedFps, selectedBitrate, selectedCodec)
            onStartExportWithBitrate != null ->
                onStartExportWithBitrate(selectedResolution, selectedFps, selectedBitrate)
            else ->
                onStartExport(selectedResolution, selectedFps)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        triggerExport()
    }

    val launchExportWithPermissionCheck = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            triggerExport()
        }
    }

    val saveAsDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { destUri ->
        if (destUri != null) {
            onSaveExportToUri(destUri)
        }
    }

    val durationSeconds = (uiState.totalDurationMs / 1000f).coerceAtLeast(1f)
    val codecEfficiencyFactor = if (selectedCodec == ExportVideoCodec.HEVC) 0.72f else 1.0f
    val estimatedSizeMb = ((durationSeconds * selectedBitrate.mbps) / 8f) * codecEfficiencyFactor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Video Studio", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("export_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Video Preview Card & Specs
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioCardDark,
                    border = BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Movie, "Export", tint = AccentCyan, modifier = Modifier.size(26.dp))
                            }
                        }

                        Text(
                            text = uiState.currentProject?.title ?: "OpenCut Project",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Text(
                            text = "Aspect: ${uiState.aspectRatio.label} • Duration: ${VideoEditorViewModel.formatTime(uiState.totalDurationMs)}",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "Clips: ${uiState.clips.size} • Audio: ${uiState.audioClips.size} • Text: ${uiState.textOverlays.size} • Photos: ${uiState.imageOverlays.size}",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                if (!uiState.isExporting && !uiState.exportCompleted && uiState.exportData.stage != ExportStage.FAILED) {
                    // Social Media Export Presets (Built-in + User Custom)
                    ExportPresetsSection(
                        presets = exportPresets,
                        activePresetId = uiState.activeExportPresetId,
                        currentAspectRatio = uiState.aspectRatio,
                        currentResolution = selectedResolution,
                        currentFps = selectedFps,
                        currentBitrate = selectedBitrate,
                        currentCodec = selectedCodec,
                        onApplyPreset = { preset ->
                            selectedResolution = preset.resolution
                            selectedFps = preset.fps
                            selectedBitrate = preset.bitrate
                            selectedCodec = preset.videoCodec
                            onApplyExportPreset(preset)
                        },
                        onSaveCustomPreset = onSaveCustomExportPreset,
                        onDeletePreset = onDeleteExportPreset,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Aspect Ratio Selector
                    Text(
                        text = "Aspect Ratio & Framing",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AspectRatio.entries.forEach { aspect ->
                            val isSelected = uiState.aspectRatio == aspect
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .clickable { onSetAspectRatio(aspect) }
                                    .testTag("export_aspect_${aspect.name}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = aspect.label.split(" ").first(),
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = aspect.label.substringAfter("(").substringBefore(")"),
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Export Resolution
                    Text(
                        text = "Export Resolution",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportResolution.entries.forEach { res ->
                            val isSelected = selectedResolution == res
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedResolution = res
                                        onUpdateExportSettings?.invoke(res, selectedFps, selectedBitrate, selectedCodec)
                                    }
                                    .testTag("resolution_${res.name}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = res.label.split(" ").first(),
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${res.width}x${res.height}",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    if (selectedResolution == ExportResolution.RES_4K && !uiState.is4kSupported) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF451A03),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "4K Warning", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "4K H.264 hardware encoder not reported on this device. Export may fail or run slowly — 1080p is recommended.",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Frame Rate (FPS)
                    Text(
                        text = "Frame Rate (FPS)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(24 to "24 fps (Film)", 30 to "30 fps (Standard)", 60 to "60 fps (Smooth)").forEach { (fps, label) ->
                            val isSelected = selectedFps == fps
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedFps = fps
                                        onUpdateExportSettings?.invoke(selectedResolution, fps, selectedBitrate, selectedCodec)
                                    }
                                    .testTag("fps_${fps}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${fps} FPS",
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = label.substringAfter("(").substringBefore(")"),
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bitrate Quality Selector
                    Text(
                        text = "Bitrate & Quality",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportBitrate.entries.forEach { bitrate ->
                            val isSelected = selectedBitrate == bitrate
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedBitrate = bitrate
                                        onUpdateExportSettings?.invoke(selectedResolution, selectedFps, bitrate, selectedCodec)
                                    }
                                    .testTag("bitrate_${bitrate.name}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${bitrate.mbps} Mbps",
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = bitrate.label.substringAfter("(").substringBefore(")"),
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Video Codec Selector
                    Text(
                        text = "Video Codec",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportVideoCodec.entries.forEach { codec ->
                            val isSelected = selectedCodec == codec
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentEmerald.copy(alpha = 0.2f) else StudioCardDark,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentEmerald else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedCodec = codec
                                        onUpdateExportSettings?.invoke(selectedResolution, selectedFps, selectedBitrate, codec)
                                    }
                                    .testTag("codec_${codec.name}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = codec.label,
                                        color = if (isSelected) AccentEmerald else Color.LightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (codec == ExportVideoCodec.H264) "Universal MP4" else "Compact H.265",
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    if (selectedCodec == ExportVideoCodec.HEVC && !isHevcSupported) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Note: HEVC encoder not detected on this device; Media3 will automatically fall back to H.264 MP4.",
                            color = Color(0xFFFDE68A),
                            fontSize = 10.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estimated File Size Info
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x3300E5FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Estimated MP4 Size:", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "~%.1f MB (%s/AAC)",
                                    estimatedSizeMb,
                                    if (selectedCodec == ExportVideoCodec.HEVC) "HEVC" else "H.264"
                                ),
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    ExportProgressComponent(
                        exportData = uiState.exportData,
                        onCancelExport = onCancelExport,
                        onDismissCompleted = onBack,
                        onSaveAsClick = {
                            val defaultName = uiState.exportData.outputFileName.ifBlank { "opencut_export.mp4" }
                            saveAsDocumentLauncher.launch(defaultName)
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
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Bottom Primary Actions: Export Preview + Render & Export MP4
            if (!uiState.isExporting && !uiState.exportCompleted && !uiState.exportData.isExporting && !uiState.exportData.completed && uiState.exportData.stage != ExportStage.FAILED) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showExportPreviewDialog = true },
                    enabled = uiState.clips.isNotEmpty(),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.75f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_export_preview_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Export Preview",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export Preview (5s Segment)",
                        color = AccentCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = launchExportWithPermissionCheck,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_export_render_button")
                ) {
                    Text("Render & Export MP4", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showExportPreviewDialog) {
        ExportPreviewDialog(
            uiState = uiState,
            selectedResolution = selectedResolution,
            selectedFps = selectedFps,
            selectedBitrate = selectedBitrate,
            onDismiss = { showExportPreviewDialog = false },
            onConfirmExport = {
                showExportPreviewDialog = false
                launchExportWithPermissionCheck()
            }
        )
    }
}
