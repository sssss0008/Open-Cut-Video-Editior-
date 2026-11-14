package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.VideoEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenShotRazorSheet(
    selectedClip: VideoClip?,
    currentPositionMs: Long,
    isSnappingEnabled: Boolean,
    onDismiss: () -> Unit,
    onSliceClip: () -> Unit,
    onSliceKeepLeft: () -> Unit,
    onSliceKeepRight: () -> Unit,
    onSliceAllTracks: () -> Unit,
    onToggleSnapping: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AccentPink.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ContentCut, "Razor Tool", tint = AccentPink, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OpenShot Razor & Slice Tool",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Playhead: ${VideoEditorViewModel.formatTime(currentPositionMs)}",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_razor_sheet")) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Snapping Magnet Toggle Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A33)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38315E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSnappingEnabled) AccentCyan.copy(alpha = 0.2f) else Color(0xFF2C2840)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Hub, "Magnet", tint = if (isSnappingEnabled) AccentCyan else Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Timeline Magnet Snapping",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Snaps playhead to adjacent cuts and boundaries",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = isSnappingEnabled,
                        onCheckedChange = { onToggleSnapping() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentCyan,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2C2840)
                        ),
                        modifier = Modifier.testTag("toggle_magnet_snapping")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Razor Slicing Actions",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 1. Slice / Split at Playhead
            RazorActionCard(
                icon = Icons.Default.ContentCut,
                iconColor = AccentPink,
                title = "Slice Clip at Playhead",
                description = "Divides selected clip into two distinct clips (Left & Right)",
                enabled = selectedClip != null,
                testTag = "razor_action_split",
                onClick = {
                    onSliceClip()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Slice Keep Left
            RazorActionCard(
                icon = Icons.Default.KeyboardArrowLeft,
                iconColor = AccentCyan,
                title = "Slice & Keep Left (Discard Tail)",
                description = "Trims clip at playhead and removes everything to the right",
                enabled = selectedClip != null,
                testTag = "razor_action_keep_left",
                onClick = {
                    onSliceKeepLeft()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Slice Keep Right
            RazorActionCard(
                icon = Icons.Default.KeyboardArrowRight,
                iconColor = Color(0xFF10B981),
                title = "Slice & Keep Right (Discard Head)",
                description = "Trims clip at playhead and removes everything to the left",
                enabled = selectedClip != null,
                testTag = "razor_action_keep_right",
                onClick = {
                    onSliceKeepRight()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Slice All Tracks (Universal Razor)
            RazorActionCard(
                icon = Icons.Default.Layers,
                iconColor = Color(0xFFFFB300),
                title = "Slice All Tracks (Universal Cut)",
                description = "Simultaneously cuts video, audio, text, and overlays at current playhead",
                enabled = true,
                testTag = "razor_action_slice_all",
                onClick = {
                    onSliceAllTracks()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun RazorActionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) StudioCardDark else StudioCardDark.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (enabled) Color(0xFF2C2840) else Color(0xFF1E1B2E)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (enabled) iconColor.copy(alpha = 0.2f) else Color(0xFF221F33)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, title, tint = if (enabled) iconColor else Color.Gray, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = if (enabled) Color.White else Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
