package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExportResolution
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.components.ExportProgressComponent
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    uiState: EditorUiState,
    onBack: () -> Unit,
    onStartExport: (resolution: ExportResolution, fps: Int) -> Unit,
    onCancelExport: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedResolution by remember { mutableStateOf(ExportResolution.RES_1080P) }
    var selectedFps by remember { mutableIntStateOf(30) }
    var savedToGallery by remember { mutableStateOf(false) }

    // Calculate approximate file size in MB
    val durationSeconds = (uiState.totalDurationMs / 1000f).coerceAtLeast(1f)
    val estimatedSizeMb = (durationSeconds * selectedResolution.bitrateMbps) / 8f

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Video Preview Card & Specs
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Movie, "Export", tint = AccentCyan, modifier = Modifier.size(28.dp))
                            }
                        }

                        Text(
                            text = uiState.currentProject?.title ?: "OpenCut Project",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Text(
                            text = "Aspect: ${uiState.aspectRatio.label} • Duration: ${VideoEditorViewModel.formatTime(uiState.totalDurationMs)}",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "Clips: ${uiState.clips.size} • Audio tracks: ${uiState.audioClips.size} • Text: ${uiState.textOverlays.size}",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                if (!uiState.isExporting && !uiState.exportCompleted) {
                    // Export Settings
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
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedResolution = res }
                                    .testTag("resolution_${res.name}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = res.label.split(" ").first(),
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${res.width}x${res.height}",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedFps = fps }
                                    .testTag("fps_${fps}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "${fps} FPS",
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = label.split(" ").getOrNull(1) ?: "",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
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
                            Text(text = "Estimated File Size:", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                text = String.format("~%.1f MB (MP4)", estimatedSizeMb),
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    ExportProgressComponent(
                        exportData = uiState.exportData,
                        onCancelExport = onCancelExport,
                        onDismissCompleted = onBack,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            // Bottom Primary Action
            if (!uiState.isExporting && !uiState.exportCompleted && !uiState.exportData.isExporting && !uiState.exportData.completed) {
                Button(
                    onClick = { onStartExport(selectedResolution, selectedFps) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_export_render_button")
                ) {
                    Text("Render & Export Video", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
