package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ProjectEditHistoryEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectLayerEntity
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modal Bottom Sheet providing interactive Editing History navigation and
 * Layer Metadata inspection backed by Room Database persistence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectHistorySheet(
    project: ProjectEntity?,
    history: List<ProjectEditHistoryEntity>,
    layers: List<ProjectLayerEntity>,
    currentPlayheadMs: Long,
    onRestoreHistoryStep: (historyId: String) -> Unit,
    onToggleLayerVisibility: (layerId: String, isVisible: Boolean) -> Unit,
    onToggleLayerLock: (layerId: String, isLocked: Boolean) -> Unit,
    onToggleLayerMute: (layerId: String, isMuted: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("project_history_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.History else Icons.Default.Layers,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedTab == 0) "Editing History" else "Project Layer Metadata",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${project?.title ?: "OpenCut"} • Room Database Persistence",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_history_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = StudioDarkBg,
                contentColor = AccentCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AccentCyan
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("History Checkpoints (${history.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Layer Metadata (${layers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            when (selectedTab) {
                0 -> HistoryCheckpointsTab(
                    history = history,
                    onRestore = onRestoreHistoryStep
                )
                1 -> LayerMetadataTab(
                    layers = layers,
                    onToggleVisibility = onToggleLayerVisibility,
                    onToggleLock = onToggleLayerLock,
                    onToggleMute = onToggleLayerMute
                )
            }
        }
    }
}

@Composable
private fun HistoryCheckpointsTab(
    history: List<ProjectEditHistoryEntity>,
    onRestore: (historyId: String) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No editing history checkpoints recorded yet", color = Color.Gray, fontSize = 13.sp)
                Text("Edits like splits, trims, color grades, and filters are saved automatically", color = Color.DarkGray, fontSize = 11.sp)
            }
        }
    } else {
        val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(horizontal = 16.dp)
                .testTag("history_checkpoints_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history.reversed(), key = { it.historyId }) { entry ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (entry.isCurrentStep) AccentCyan.copy(alpha = 0.12f) else StudioCardDark,
                    border = if (entry.isCurrentStep) androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = if (entry.isCurrentStep) AccentCyan else Color(0xFF374151),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${entry.stepIndex}",
                                        color = if (entry.isCurrentStep) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.actionDescription,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (entry.isCurrentStep) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AccentCyan.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "CURRENT",
                                                color = AccentCyan,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${entry.actionType} • ${dateFormat.format(Date(entry.timestamp))}",
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (!entry.isCurrentStep) {
                            Button(
                                onClick = { onRestore(entry.historyId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("restore_step_${entry.stepIndex}")
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerMetadataTab(
    layers: List<ProjectLayerEntity>,
    onToggleVisibility: (layerId: String, isVisible: Boolean) -> Unit,
    onToggleLock: (layerId: String, isLocked: Boolean) -> Unit,
    onToggleMute: (layerId: String, isMuted: Boolean) -> Unit
) {
    if (layers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No layers stored in Room for this project", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(horizontal = 16.dp)
                .testTag("layer_metadata_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(layers, key = { it.layerId }) { layer ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (layer.layerType) {
                                        "VIDEO" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                        "AUDIO" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        "TEXT" -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                        "STICKER" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                        "SHAPE" -> Color(0xFFEC4899).copy(alpha = 0.2f)
                                        else -> Color(0xFF6B7280).copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = "TRACK ${layer.layerIndex + 1} • ${layer.layerType}",
                                        color = when (layer.layerType) {
                                            "VIDEO" -> Color(0xFF60A5FA)
                                            "AUDIO" -> Color(0xFF34D399)
                                            "TEXT" -> Color(0xFFA78BFA)
                                            "STICKER" -> Color(0xFFFBBF24)
                                            "SHAPE" -> Color(0xFFF472B6)
                                            else -> Color.White
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = layer.layerName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${layer.itemCount} item(s)",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF1F2937), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Layer Metadata Controls (Mute, Lock, Visibility)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Blend Mode: ${layer.blendMode}",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Opacity: ${(layer.opacity * 100).toInt()}%",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Visibility toggle
                                IconButton(
                                    onClick = { onToggleVisibility(layer.layerId, !layer.isVisible) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = if (layer.isVisible) AccentCyan else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Lock toggle
                                IconButton(
                                    onClick = { onToggleLock(layer.layerId, !layer.isLocked) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Toggle Lock",
                                        tint = if (layer.isLocked) AccentPink else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Mute toggle (for audio and video tracks)
                                if (layer.layerType == "AUDIO" || layer.layerType == "VIDEO") {
                                    IconButton(
                                        onClick = { onToggleMute(layer.layerId, !layer.isMuted) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Toggle Mute",
                                            tint = if (layer.isMuted) Color.Red else Color.Gray,
                                            modifier = Modifier.size(16.dp)
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
}
