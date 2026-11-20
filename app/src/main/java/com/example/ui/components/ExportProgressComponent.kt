package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.util.Media3VideoExporter
import java.io.File
import java.util.Locale

@Composable
fun ExportProgressComponent(
    exportData: ExportProgressData,
    onCancelExport: () -> Unit,
    onDismissCompleted: () -> Unit,
    onSaveAsClick: (() -> Unit)? = null,
    onPlayExportedClick: (() -> Unit)? = null,
    onShareExportedClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFailed = exportData.stage == ExportStage.FAILED

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

    val borderColor = when {
        isFailed -> Color(0xFFEF4444)
        exportData.completed -> AccentEmerald
        else -> AccentCyan
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardDark),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
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
                        color = borderColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isFailed -> Icons.Default.ErrorOutline
                                    exportData.completed -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Movie
                                },
                                contentDescription = "Export Status",
                                tint = borderColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when {
                                isFailed -> "Export Failed"
                                exportData.completed -> "MP4 Render Completed!"
                                else -> "Exporting Video"
                            },
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${exportData.resolution.label} • ${exportData.fps} FPS • ${exportData.bitrateMbps} Mbps MP4",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                if (exportData.isExporting && !exportData.completed && !isFailed) {
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

            // Error Banner if Failed
            if (isFailed) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF450A0A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = exportData.errorMessage ?: "Export failed during hardware encoding.",
                        color = Color(0xFFFCA5A5),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancelExport,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("export_error_reset_button")
                ) {
                    Text("Reset & Try Another Resolution", color = Color.White, fontSize = 12.sp)
                }
            }

            // Completed State Actions: Real File Verification, Play, Save As, Share MP4
            if (exportData.completed) {
                Spacer(modifier = Modifier.height(14.dp))

                val outputFile = remember(exportData.outputFilePath) {
                    if (exportData.outputFilePath.isNotBlank()) File(exportData.outputFilePath) else null
                }
                val actualSizeText = remember(exportData.outputFilePath) {
                    val bytes = outputFile?.takeIf { it.exists() }?.length() ?: 0L
                    if (bytes >= 1024L * 1024L) {
                        String.format(Locale.US, "%.2f MB", bytes / (1024f * 1024f))
                    } else if (bytes > 0L) {
                        "${(bytes / 1024L).coerceAtLeast(1L)} KB"
                    } else {
                        "Saved"
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentEmerald.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "✓ Rendered MP4 (${actualSizeText}): ${exportData.outputFileName}\nPublished to Movies/OpenCut on device storage",
                        color = AccentEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (onPlayExportedClick != null) {
                                onPlayExportedClick()
                            } else {
                                val uri = Media3VideoExporter.getShareableFileUri(context, exportData.outputFilePath)
                                if (uri != null) {
                                    val playIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "video/mp4")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    runCatching {
                                        context.startActivity(Intent.createChooser(playIntent, "Play Exported MP4"))
                                    }.onFailure {
                                        Toast.makeText(context, "No video player app installed.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Exported MP4 file not found.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_play_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (onSaveAsClick != null) {
                                onSaveAsClick()
                            } else {
                                Toast.makeText(context, "Saved to Movies/OpenCut/${exportData.outputFileName}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("export_save_phone_button")
                    ) {
                        Icon(Icons.Default.Download, "Save As", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save As", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (onShareExportedClick != null) {
                                onShareExportedClick()
                            } else {
                                val uri = Media3VideoExporter.getShareableFileUri(context, exportData.outputFilePath)
                                if (uri != null) {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share MP4 Video"))
                                } else {
                                    Toast.makeText(context, "Exported MP4 file not found to share.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_share_button")
                    ) {
                        Icon(Icons.Default.Share, "Share", tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismissCompleted,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("dismiss_export_complete_button")
                ) {
                    Text("Done / Back to Timeline", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
