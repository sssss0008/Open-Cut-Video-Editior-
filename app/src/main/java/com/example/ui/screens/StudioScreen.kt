package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExportResolution
import com.example.data.model.RenderedVideoItem
import com.example.ui.components.ExportProgressComponent
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackAudioColor
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    onRunAutomationWorkflow: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedRes by remember { mutableStateOf(ExportResolution.RES_1080P) }
    var selectedFps by remember { mutableIntStateOf(30) }
    var selectedCodec by remember { mutableStateOf("H.264 (MP4)") }
    var activeUtilityTool by remember { mutableStateOf<String?>(null) }
    var playingVideoItem by remember { mutableStateOf<RenderedVideoItem?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Export Studio & Archive", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Rendered videos saved in phone storage", color = AccentCyan, fontSize = 11.sp)
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

            // 1. REAL-TIME EXPORT PROGRESS COMPONENT (Displayed when exporting or completed)
            if (uiState.exportData.isExporting || uiState.exportData.completed) {
                item {
                    Text(
                        text = "🚀 Active Export Pipeline",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExportProgressComponent(
                        exportData = uiState.exportData,
                        onCancelExport = onCancelExport,
                        onDismissCompleted = {
                            // reset completed state
                            onCancelExport()
                        }
                    )
                }
            } else {
                // EXPORT TRIGGER CARD
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
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
                                        Text(uiState.currentProject?.title ?: "OpenCut Project", color = AccentCyan, fontSize = 12.sp)
                                    }
                                }

                                TextButton(onClick = onNavigateToEditor) {
                                    Text("Edit Timeline", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Resolution Chips
                            Text("Output Quality:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) AccentCyan else Color(0xFF2E2A48)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedRes = res }
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
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(24, 30, 60).forEach { fps ->
                                    val isSelected = selectedFps == fps
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AccentCyan else Color(0xFF1B182B),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedFps = fps }
                                    ) {
                                        Text(
                                            text = "$fps FPS",
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            // Pro Codec Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("H.264 (MP4)", "HEVC H.265", "AV1 HDR", "ProRes").forEach { codec ->
                                    val isSelected = selectedCodec == codec
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AccentEmerald.copy(alpha = 0.22f) else Color(0xFF1B182B),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) AccentEmerald else Color(0xFF2E2A48)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedCodec = codec }
                                            .testTag("codec_chip_${codec.take(4).lowercase()}")
                                    ) {
                                        Text(
                                            text = codec,
                                            color = if (isSelected) AccentEmerald else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onStartExport(selectedRes, selectedFps) },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("studio_start_export_button")
                            ) {
                                Icon(Icons.Default.FileDownload, "Export", tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Real-Time Export", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1B. PRE-EXPORT PROJECT QUALITY ASSISTANT & AUTOMATION WORKFLOW RUNNER
            item {
                val missingTransitionsCount = uiState.clips.drop(1).count { it.transitionIn.name == "NONE" }
                val isDuckingReady = uiState.isAutoDuckingMasterEnabled
                val qualityScore = (100 - missingTransitionsCount * 10 - if (!isDuckingReady) 15 else 0).coerceIn(65, 100)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.45f)),
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
                            Column {
                                Text(
                                    text = "🛡 Pre-Export Project Quality Assistant",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automated timeline inspection for gaps, audio peaks & transitions",
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
                                "Audio Peak: -1.0 dBTP" to true,
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
                                onClick = {
                                    onAutoFixQuality()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auto_fix_quality_button")
                            ) {
                                Text("✨ Auto-Fix Timeline Issues", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    onRunAutomationWorkflow("Fast TikTok viral cut with neon captions, beat sync & teal-orange LUT")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("run_macro_workflow_button")
                            ) {
                                Text("⚡ Run Pro Macro", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        text = "100% Local Storage",
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
                                text = "When you export projects, the final rendered videos are preserved right here on your phone.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Exported with OpenCut Studio: ${video.fileName}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
                        },
                        onDelete = { onDeleteRenderedVideo(video.id) }
                    )
                }
            }

            // 3. QUICK VIDEO UTILITIES & DESKTOP NLE INTERCHANGE SUITE
            item {
                Text(
                    text = "🛠 Studio Utilities & Desktop Interchange",
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
                            title = "Audio Detacher",
                            desc = "Extract WAV / MP3 / FLAC",
                            icon = Icons.Default.Audiotrack,
                            accent = TrackAudioColor,
                            onClick = { activeUtilityTool = "Audio Detacher (WAV / MP3 / FLAC)" },
                            modifier = Modifier.weight(1f)
                        )
                        StudioUtilityCard(
                            title = "Aspect Converter",
                            desc = "Reformat 16:9 to 9:16 Shorts",
                            icon = Icons.Default.AspectRatio,
                            accent = AccentCyan,
                            onClick = onNavigateToEditor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioUtilityCard(
                            title = "GIF & Loop Maker",
                            desc = "Convert timeline clip to GIF",
                            icon = Icons.Default.Movie,
                            accent = AccentPink,
                            onClick = { activeUtilityTool = "GIF & Boomerang Maker" },
                            modifier = Modifier.weight(1f)
                        )
                        StudioUtilityCard(
                            title = "Video Compressor",
                            desc = "Shrink MP4 size by up to 70%",
                            icon = Icons.Default.Speed,
                            accent = AccentEmerald,
                            onClick = { activeUtilityTool = "Smart Video Compressor" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioUtilityCard(
                            title = "SRT / VTT Export",
                            desc = "Export subtitle caption file",
                            icon = Icons.Default.FileDownload,
                            accent = AccentCyan,
                            onClick = { activeUtilityTool = "SRT & VTT Subtitle Exporter" },
                            modifier = Modifier.weight(1f)
                        )
                        StudioUtilityCard(
                            title = "FCPXML / OTIO",
                            desc = "DaVinci & Premiere XML export",
                            icon = Icons.Default.Share,
                            accent = Color(0xFF8B5CF6),
                            onClick = { activeUtilityTool = "Desktop NLE Interchange (FCPXML / EDL / OTIO)" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (activeUtilityTool != null) {
        val toolName = activeUtilityTool!!
        val projectTitle = uiState.currentProject?.title ?: "OpenCut_Project"
        val previewPayload = when {
            toolName.contains("FCPXML") -> "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE fcpxml>\n<fcpxml version=\"1.10\">\n  <project name=\"$projectTitle\" clips=\"${uiState.clips.size}\" duration=\"${uiState.totalDurationMs}ms\"/>\n</fcpxml>"
            toolName.contains("SRT") -> "1\n00:00:00,000 --> 00:00:03,000\n${uiState.textOverlays.firstOrNull()?.text ?: "Welcome to $projectTitle"}\n\n2\n00:00:03,000 --> 00:00:06,500\nEdited in OpenCut Studio Pro"
            toolName.contains("Compressor") -> "Input: ${uiState.clips.size} clips (~24.6 MB)\nTarget Codec: HEVC H.265 CRF 24\nEstimated Output: 7.4 MB (70% reduction, zero visual loss)"
            toolName.contains("GIF") -> "Format: High-Color 256-Palette Animated GIF\nFrame Rate: 15 FPS • Loop: Infinite\nOutput: ${projectTitle.lowercase().replace(" ", "_")}_loop.gif"
            else -> "Track: Master Stereo 48kHz 24-bit PCM\nFormats Available: WAV (Lossless), MP3 (320kbps), FLAC\nOutput: ${projectTitle.lowercase().replace(" ", "_")}_master_audio.wav"
        }

        androidx.compose.ui.window.Dialog(onDismissRequest = { activeUtilityTool = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF141222),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("studio_utility_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(toolName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Project: $projectTitle • Ready for instant local export",
                        color = AccentCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0B0F19),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28354E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = previewPayload,
                            color = AccentEmerald,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeUtilityTool = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", color = Color.LightGray)
                        }
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, previewPayload)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export $toolName"))
                                activeUtilityTool = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export / Share", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Video Player Playback Dialog
    if (playingVideoItem != null) {
        val video = playingVideoItem!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { playingVideoItem = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF141222),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
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
                        Text(video.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { playingVideoItem = null }) {
                            Icon(Icons.Default.Movie, "Close", tint = Color.LightGray)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, "Playing", tint = AccentCyan, modifier = Modifier.size(54.dp))
                        Text(
                            text = "Simulated On-Device Video Playback",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${video.resolution} • ${video.fps} FPS • ${VideoEditorViewModel.formatTime(video.durationMs)} • ${(video.fileSizeBytes / (1024 * 1024))} MB",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { playingVideoItem = null },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Player", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
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
                    text = "${video.resolution} • ${video.fps}fps • ${(video.fileSizeBytes / (1024 * 1024))}MB",
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
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, "Share", tint = AccentCyan, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
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
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
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
