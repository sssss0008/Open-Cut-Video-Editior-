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
    onCloseDrawer: () -> Unit,
    onNavigateTab: (Int) -> Unit,
    onClearCache: () -> Unit
) {
    var snappingEnabled by remember { mutableStateOf(true) }
    var showCacheCleanedMessage by remember { mutableStateOf(false) }

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
                    Text("v3.0 Ultimate Pro • NLE & AI", color = Color.Gray, fontSize = 11.sp)
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
                    Text("100% On-Device & Offline Storage", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                        Text("Estimated Device Usage:", color = Color.LightGray, fontSize = 12.sp)
                        Text("${(projectCount * 2) + (renderedVideoCount * 18)} MB", color = AccentEmerald, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                            text = "✓ Temp frame cache cleared!",
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
                subtitle = "16 effects: Crossfade, wipes, slides, zoom, iris",
                accent = TrackTransitionColor,
                onClick = {
                    onNavigateTab(1) // Edit tab
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Palette,
                title = "Cinematic Color LUTs",
                subtitle = "12 aesthetic presets & contrast controls",
                accent = AccentCyan,
                onClick = {
                    onNavigateTab(1)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Storage,
                title = "Rendered Videos Archive",
                subtitle = "Direct playback and gallery export",
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
                    Text("Snap playhead to clip cuts", color = Color.Gray, fontSize = 10.sp)
                }
                Switch(
                    checked = snappingEnabled,
                    onCheckedChange = { snappingEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan, checkedTrackColor = Color(0xFF004D40))
                )
            }

            DrawerItem(
                icon = Icons.Default.TouchApp,
                title = "Touch & Gestures Guide",
                subtitle = "Pinch timeline to zoom • Double tap to play",
                accent = Color(0xFFFFB300),
                onClick = {}
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFF26223B))
            Spacer(modifier = Modifier.height(14.dp))

            // Studio Info & Privacy
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, "About", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("100% On-Device Offline Editor", color = Color.Gray, fontSize = 10.sp)
            }
            Text(
                text = "Zero tracking • No cloud dependencies • Private on your phone",
                color = Color.DarkGray,
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
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
