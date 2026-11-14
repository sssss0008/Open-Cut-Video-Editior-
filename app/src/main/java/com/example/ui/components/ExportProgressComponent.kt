package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.ExportProgressData
import com.example.data.model.ExportStage
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark

@Composable
fun ExportProgressComponent(
    exportData: ExportProgressData,
    onCancelExport: () -> Unit,
    onDismissCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSavedToast by remember { mutableStateOf(false) }

    // Pulsing shimmer animation for rendering activity
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (exportData.completed) AccentEmerald else AccentCyan),
        modifier = modifier
            .fillMaxWidth()
            .testTag("export_progress_component")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Title & Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (exportData.completed) AccentEmerald.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (exportData.completed) Icons.Default.CheckCircle else Icons.Default.Movie,
                                contentDescription = "Export Status",
                                tint = if (exportData.completed) AccentEmerald else AccentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (exportData.completed) "Render Completed!" else "Exporting Video",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${exportData.resolution.label} • ${exportData.fps} FPS • MP4",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                if (exportData.isExporting && !exportData.completed) {
                    OutlinedButton(
                        onClick = onCancelExport,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("cancel_export_button")
                    ) {
                        Icon(Icons.Default.Close, "Cancel", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar & Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = exportData.stage.stageName,
                    color = if (exportData.completed) AccentEmerald else AccentCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${exportData.progressPercent}%",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { exportData.progress.coerceIn(0f, 1f) },
                color = if (exportData.completed) AccentEmerald else AccentCyan,
                trackColor = Color(0xFF1E1B2E),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .testTag("export_progress_bar")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Real-Time Metrics Grid: ETA Countdown, Frame Counter, Speed, and Size
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF141222),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28253E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Row 1: Estimated Time Remaining (ETA) & Elapsed Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ETA Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "ETA",
                                tint = AccentPink,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("ESTIMATED REMAINING", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (exportData.completed) "00:00 (Complete)" else exportData.formattedEta,
                                    color = if (exportData.completed) AccentEmerald else AccentPink,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.testTag("export_eta_display")
                                )
                            }
                        }

                        // Elapsed Time Section
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ELAPSED TIME", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = exportData.formattedElapsed,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Divider
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF232035)))

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Frame Progress & Render Speed FPS & Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Frames Counter
                        Column {
                            Text("FRAME RENDERED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${exportData.currentFrame} / ${exportData.totalFrames.coerceAtLeast(1)}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Rendering Speed
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RENDER SPEED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, "Speed", tint = AccentCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%.1f fps", exportData.currentFps),
                                    color = AccentCyan,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Approx File Size
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EST. OUTPUT", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("~%.1f MB", exportData.estimatedSizeMb),
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pipeline Stage Tracker: Mini step pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val stages = listOf(
                    ExportStage.PREPROCESSING,
                    ExportStage.RENDERING_TRANSITIONS,
                    ExportStage.APPLYING_FILTERS,
                    ExportStage.MIXING_AUDIO,
                    ExportStage.ENCODING_CONTAINER
                )
                stages.forEachIndexed { index, st ->
                    val isPast = exportData.stage.ordinal > st.ordinal || exportData.completed
                    val isCurrent = exportData.stage == st && !exportData.completed
                    val barColor = when {
                        isPast -> AccentEmerald
                        isCurrent -> AccentCyan.copy(alpha = pulseAlpha)
                        else -> Color(0xFF2C2840)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                    )
                }
            }

            Text(
                text = exportData.stage.description,
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            // Completed State Actions: Play, Save to Gallery, Share
            if (exportData.completed) {
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = showSavedToast, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentEmerald.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "✓ Saved directly to phone internal storage: /Movies/OpenCut/${exportData.outputFileName}",
                            color = AccentEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showSavedToast = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("export_save_phone_button")
                    ) {
                        Icon(Icons.Default.Download, "Save", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to Phone", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Exported with OpenCut Studio (100% on-device offline video editor): ${exportData.outputFileName}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Exported Video"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("export_share_button")
                    ) {
                        Icon(Icons.Default.Share, "Share", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Video", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismissCompleted,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("dismiss_export_complete_button")
                ) {
                    Text("Done / Back to Timeline", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
