package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackTransitionColor

@Composable
fun AppNavigationDrawer(
    projectCount: Int,
    renderedVideoCount: Int,
    storageUsageBytes: Long = 0L,
    isSnappingEnabled: Boolean = true,
    onToggleSnapping: (Boolean) -> Unit = {},
    onCloseDrawer: () -> Unit,
    onNavigateTab: (Int) -> Unit,
    onOpenTransitions: () -> Unit = { onNavigateTab(1) },
    onOpenColorStudio: () -> Unit = { onNavigateTab(1) },
    onClearCache: () -> Unit
) {
    var showCacheCleanedMessage by remember { mutableStateOf(false) }
    var showGesturesDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        drawerContainerColor = StudioSurfaceDark,
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("app_navigation_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // App Branding Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo_icon),
                    contentDescription = "OpenCut Studio Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("OpenCut Studio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("v4.0 Pro • Multi-Track NLE", color = Color.Gray, fontSize = 11.sp)
                }
            }

            // On-Device Privacy Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentEmerald.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, "Lock", tint = AccentEmerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("On-Device Room Storage • Optional Sync", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(color = Color(0xFF26223B))
            Spacer(modifier = Modifier.height(14.dp))

            // PHONE STORAGE MANAGER
            Text("PHONE LOCAL STORAGE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioCardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF28253E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Projects (Room):", color = Color.LightGray, fontSize = 12.sp)
                        Text("$projectCount drafts", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Exported MP4 Videos:", color = Color.LightGray, fontSize = 12.sp)
                        Text("$renderedVideoCount saved", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Actual App Disk Usage:", color = Color.LightGray, fontSize = 12.sp)
                        Text(
                            text = com.example.util.AppStorageBreakdown.formatBytes(storageUsageBytes),
                            color = AccentEmerald,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1A33),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClearCache()
                                showCacheCleanedMessage = true
                            }
                            .testTag("clear_cache_drawer_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CleaningServices, "Clean", tint = AccentPink, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clean Temp Buffers", color = AccentPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showCacheCleanedMessage) {
                        Text(
                            text = "✓ Temp cache & thumbnails cleared",
                            color = AccentEmerald,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // STUDIO FEATURES QUICK LINKS
            Text("VIDEO EDITOR FEATURES", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            DrawerItem(
                icon = Icons.Default.Transform,
                title = "Transitions Library",
                subtitle = "16 effects: Drag & drop onto clip seams",
                accent = TrackTransitionColor,
                onClick = {
                    onOpenTransitions()
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Palette,
                title = "Cinematic Color LUTs",
                subtitle = "12 aesthetic presets & color wheels",
                accent = AccentCyan,
                onClick = {
                    onOpenColorStudio()
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Storage,
                title = "Export Studio & Presets",
                subtitle = "Social presets, MP4 archive & utilities",
                accent = AccentEmerald,
                onClick = {
                    onNavigateTab(3) // Studio tab
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // SETTINGS & GESTURES
            Text("PREFERENCES & CONTROLS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Timeline Snapping", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Magnetic snap to cuts & markers", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = isSnappingEnabled,
                    onCheckedChange = { onToggleSnapping(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan, checkedTrackColor = Color(0xFF004D40)),
                    modifier = Modifier.testTag("drawer_snapping_switch")
                )
            }

            DrawerItem(
                icon = Icons.Default.TouchApp,
                title = "Touch & Gestures Guide",
                subtitle = "Pinch zoom • Drag seams • Keyframes",
                accent = Color(0xFFFFB300),
                onClick = { showGesturesDialog = true }
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF26223B))
            Spacer(modifier = Modifier.height(14.dp))

            // Studio Info & Privacy
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, "About", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hardware-Accelerated Media3 Engine", color = Color.Gray, fontSize = 10.sp)
            }
            Text(
                text = "Local Room auto-save • Optional Firebase cloud backup in Account",
                color = Color.DarkGray,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (showGesturesDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showGesturesDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = StudioSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gestures_guide_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Touch & Editor Gestures", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    listOf(
                        "Scrub Ruler" to "Drag along the timecode ruler for frame-accurate playhead scrubbing.",
                        "Timeline Zoom" to "Use +/- zoom controls or pinch gesture to expand for frame cuts.",
                        "Drag Transitions" to "Open Transition tool, then drag any card directly onto a clip seam.",
                        "Trim Clip Handles" to "Select a video clip and drag its left/right trim bars to adjust in/out.",
                        "Animate Keyframes" to "Select a video or image clip and tap Keyframe to animate scale, opacity & rotation."
                    ).forEach { (title, desc) ->
                        Column {
                            Text(title, color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(desc, color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.Button(
                        onClick = { showGesturesDialog = false },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got It", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, title, tint = accent, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}
