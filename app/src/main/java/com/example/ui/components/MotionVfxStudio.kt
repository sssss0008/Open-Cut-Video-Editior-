package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FrameInterpolationMode
import com.example.data.model.SpeedRampPreset
import com.example.data.model.VfxParticlePreset
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark

enum class MotionVfxTab(val title: String, val icon: ImageVector) {
    SPEED_RAMP("Speed Ramp", Icons.Default.Speed),
    PARTICLE_VFX("Particle VFX", Icons.Default.Flare),
    NODE_GRAPH("Node Graph", Icons.Default.AccountTree),
    MOTION_TRACK("Motion Track", Icons.Default.CenterFocusStrong)
}

@Composable
fun MotionVfxStudio(
    activeClip: VideoClip?,
    onApplySpeedRamp: (SpeedRampPreset, FrameInterpolationMode, Boolean) -> Unit,
    onApplyParticleVfx: (VfxParticlePreset) -> Unit,
    onToggleNodeStage: (String, Boolean) -> Unit,
    onApplyMotionTrack: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MotionVfxTab.SPEED_RAMP) }
    var selectedSpeedPreset by remember { mutableStateOf(SpeedRampPreset.MONTAGE_RAMP) }
    var selectedInterpolation by remember { mutableStateOf(FrameInterpolationMode.OPTICAL_FLOW) }
    var preserveAudioPitch by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfaceDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("motion_vfx_studio")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = AccentPink.copy(alpha = 0.22f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.BlurOn,
                            contentDescription = "Motion & VFX Studio",
                            tint = AccentPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Motion Graphics, VFX & Speed Studio",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = activeClip?.let { "Target: ${it.name} • ${it.speed}x" } ?: "Optical Flow • Node Compositor • Planar Tracker",
                        color = AccentCyan,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sub-Tab Selector
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(MotionVfxTab.entries.toList()) { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) AccentCyan else StudioCardDark,
                    border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C3345)),
                    modifier = Modifier
                        .clickable { selectedTab = tab }
                        .testTag("motion_tab_${tab.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) Color.Black else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F1523),
            border = BorderStroke(1.dp, Color(0xFF232B3E)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                MotionVfxTab.SPEED_RAMP -> {
                    SpeedRampPane(
                        selectedPreset = selectedSpeedPreset,
                        selectedInterpolation = selectedInterpolation,
                        preservePitch = preserveAudioPitch,
                        onSelectPreset = { selectedSpeedPreset = it },
                        onSelectInterpolation = { selectedInterpolation = it },
                        onTogglePitch = { preserveAudioPitch = it },
                        onApply = {
                            onApplySpeedRamp(selectedSpeedPreset, selectedInterpolation, preserveAudioPitch)
                        }
                    )
                }

                MotionVfxTab.PARTICLE_VFX -> {
                    ParticleVfxPane(
                        onSelectVfx = onApplyParticleVfx
                    )
                }

                MotionVfxTab.NODE_GRAPH -> {
                    NodeCompositorPane(
                        activeClip = activeClip,
                        onToggleNodeStage = onToggleNodeStage
                    )
                }

                MotionVfxTab.MOTION_TRACK -> {
                    MotionTrackerPane(
                        onApplyTrack = onApplyMotionTrack
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedRampPane(
    selectedPreset: SpeedRampPreset,
    selectedInterpolation: FrameInterpolationMode,
    preservePitch: Boolean,
    onSelectPreset: (SpeedRampPreset) -> Unit,
    onSelectInterpolation: (FrameInterpolationMode) -> Unit,
    onTogglePitch: (Boolean) -> Unit,
    onApply: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("speed_ramp_pane"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            // Interactive Speed Curve Visualizer
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF161D2F),
                border = BorderStroke(1.dp, Color(0xFF28354E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pts = selectedPreset.curvePoints
                        val w = size.width
                        val h = size.height
                        // Grid line 1.0x
                        val baseOneY = h * 0.65f
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(0f, baseOneY),
                            end = Offset(w, baseOneY),
                            strokeWidth = 1.dp.toPx()
                        )

                        val path = Path()
                        val offsets = pts.mapIndexed { idx, speedVal ->
                            val x = (idx.toFloat() / (pts.size - 1).coerceAtLeast(1)) * w
                            val norm = ((speedVal - 0.2f) / 3.8f).coerceIn(0f, 1f)
                            val y = h - (norm * (h * 0.85f)) - (h * 0.08f)
                            Offset(x, y)
                        }

                        offsets.forEachIndexed { idx, pt ->
                            if (idx == 0) path.moveTo(pt.x, pt.y)
                            else {
                                val prev = offsets[idx - 1]
                                val midX = (prev.x + pt.x) / 2f
                                path.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                            }
                        }

                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(AccentCyan, AccentPink, AccentEmerald)),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        offsets.forEach { pt ->
                            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                            drawCircle(color = AccentCyan, radius = 2.5.dp.toPx(), center = pt)
                        }
                    }

                    Text(
                        text = "${selectedPreset.displayName} • Avg ${selectedPreset.targetSpeed}x",
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                }
            }
        }

        item {
            Text("1-Tap Speed Ramp Curve Presets", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SpeedRampPreset.entries.toList()) { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C3345)),
                        modifier = Modifier
                            .clickable { onSelectPreset(preset) }
                            .testTag("speed_preset_${preset.name.lowercase()}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(
                                text = preset.displayName,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preset.targetSpeed}x ramp",
                                color = AccentCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Optical Flow & Frame Interpolation", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(FrameInterpolationMode.entries.toList()) { mode ->
                            val isSelected = selectedInterpolation == mode
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AccentEmerald.copy(alpha = 0.22f) else StudioCardDark,
                                border = BorderStroke(1.dp, if (isSelected) AccentEmerald else Color(0xFF2C3345)),
                                modifier = Modifier.clickable { onSelectInterpolation(mode) }
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) AccentEmerald else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Preserve Audio Pitch", color = Color.LightGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = preservePitch,
                        onCheckedChange = onTogglePitch,
                        colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan),
                        modifier = Modifier.height(24.dp)
                    )
                }

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("apply_speed_ramp_button")
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Speed Ramp", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ParticleVfxPane(
    onSelectVfx: (VfxParticlePreset) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("particle_vfx_pane"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            Text(
                text = "Tap any GPU Particle / Atmospheric VFX to layer onto your timeline:",
                color = Color.LightGray,
                fontSize = 11.sp
            )
        }

        items(VfxParticlePreset.entries.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioCardDark,
                        border = BorderStroke(1.dp, Color(preset.colorHex).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectVfx(preset) }
                            .testTag("vfx_preset_${preset.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(preset.colorHex).copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(preset.badgeEmoji, fontSize = 15.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = preset.displayName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = preset.category,
                                    color = Color(preset.colorHex),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeCompositorPane(
    activeClip: VideoClip?,
    onToggleNodeStage: (String, Boolean) -> Unit
) {
    val activeNodes = remember {
        mutableStateListOf("MediaIn", "ColorGrade", "ParticleVFX", "MediaOut")
    }
    val allNodes = listOf(
        "MediaIn" to AccentCyan,
        "ChromaKey" to AccentEmerald,
        "ColorGrade" to Color(0xFFF59E0B),
        "ParticleVFX" to AccentPink,
        "Stabilizer" to Color(0xFF8B5CF6),
        "MediaOut" to AccentCyan
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("node_compositor_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Fusion Node-Based Compositing Pipeline",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any processing node below to bypass or activate it in the real-time render graph",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }

        // Visual Node Pipeline Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(allNodes) { (nodeName, nodeColor) ->
                val isEnabled = activeNodes.contains(nodeName)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isEnabled) nodeColor.copy(alpha = 0.2f) else Color(0xFF161B26),
                    border = BorderStroke(1.5.dp, if (isEnabled) nodeColor else Color.DarkGray),
                    modifier = Modifier
                        .clickable {
                            if (nodeName != "MediaIn" && nodeName != "MediaOut") {
                                val next = !isEnabled
                                if (next) activeNodes.add(nodeName) else activeNodes.remove(nodeName)
                                onToggleNodeStage(nodeName, next)
                            }
                        }
                        .testTag("node_item_${nodeName.lowercase()}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(if (isEnabled) nodeColor else Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = nodeName,
                                color = if (isEnabled) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = if (isEnabled) "ACTIVE" else "BYPASSED",
                            color = if (isEnabled) nodeColor else Color.DarkGray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "Active Pipeline: ${activeNodes.joinToString(" → ")}",
            color = AccentCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun MotionTrackerPane(
    onApplyTrack: (mode: String, targetLabel: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf("AI Face & Subject Lock") }
    var selectedTarget by remember { mutableStateOf("Attach Callout Text") }

    val modes = listOf("AI Face & Subject Lock", "Planar Surface Pin", "Point Object Follow")
    val targets = listOf("Attach Callout Text", "Pin Neon Badge", "Spotlight Follow Mask")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("motion_tracker_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("AI Planar & Point Motion Tracker", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(modes) { mode ->
                    val isSelected = selectedMode == mode
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.22f) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C3345)),
                        modifier = Modifier.clickable { selectedMode = mode }
                    ) {
                        Text(
                            text = mode,
                            color = if (isSelected) AccentCyan else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(targets) { target ->
                    val isSelected = selectedTarget == target
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) AccentPink.copy(alpha = 0.22f) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentPink else Color(0xFF2C3345)),
                        modifier = Modifier.clickable { selectedTarget = target }
                    ) {
                        Text(
                            text = target,
                            color = if (isSelected) AccentPink else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onApplyTrack(selectedMode, selectedTarget) },
            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .testTag("start_motion_track_button")
        ) {
            Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Track Subject & $selectedTarget",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
