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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StabilizationMode
import com.example.data.model.StabilizationSettings
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StabilizationSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onUpdateStabilization: (StabilizationSettings) -> Unit,
    onToggleSplitPreview: (Boolean) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var isEnabled by remember { mutableStateOf(clip.stabilization.isEnabled) }
    var selectedMode by remember { mutableStateOf(clip.stabilization.mode) }
    var smoothness by remember { mutableFloatStateOf(clip.stabilization.smoothness) }
    var cropMarginPercent by remember { mutableFloatStateOf(clip.stabilization.cropMarginPercent) }
    var reduceRollingShutter by remember { mutableStateOf(clip.stabilization.reduceRollingShutter) }
    var isSplitPreviewActive by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .verticalScroll(scrollState)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF004D40)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Stabilize",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Video Stabilization Engine",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = clip.name,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hardware Acceleration Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F2332),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Hardware Accelerated",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Hardware Acceleration Active",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "100% on-device MediaCodec gyro & trajectory filter processing",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Master Enable Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioCardDark)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Anti-Shake Stabilization",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEnabled) "Active - Dampening camera vibration" else "Disabled - Original clip motion",
                        color = if (isEnabled) AccentCyan else Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = Color(0xFF004D40)
                    ),
                    modifier = Modifier.testTag("stabilization_enable_switch")
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(14.dp))

                // Stabilization Processing Mode Selector
                Text(
                    text = "Stabilization Algorithm Mode",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StabilizationMode.entries.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF1E2A4A) else StudioCardDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AccentCyan else Color(0xFF2C2840)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMode = mode }
                                .testTag("stabilization_mode_${mode.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val modeIcon = when (mode) {
                                    StabilizationMode.SMOOTH_MOTION -> Icons.Default.PanTool
                                    StabilizationMode.TRIPOD_LOCK -> Icons.Default.Lock
                                    StabilizationMode.HIGH_ACTION -> Icons.Default.DirectionsRun
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) AccentCyan else Color(0xFF242C40)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = mode.displayName,
                                        tint = if (isSelected) Color.Black else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = mode.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mode.description,
                                        color = if (isSelected) AccentCyan else Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smoothness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Smoothness Damping Strength",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(smoothness * 100).roundToInt()}%",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = smoothness,
                    onValueChange = { smoothness = it },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = Color(0xFF2C2840)
                    ),
                    modifier = Modifier.testTag("stabilization_smoothness_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Crop Margin Buffer Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto-Crop Border Buffer",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Buffer eliminates black frame edges during camera counter-rotations",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = "${(cropMarginPercent * 100).roundToInt()}%",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = cropMarginPercent,
                    onValueChange = { cropMarginPercent = it },
                    valueRange = 0.02f..0.25f,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = Color(0xFF2C2840)
                    ),
                    modifier = Modifier.testTag("stabilization_crop_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Rolling Shutter Compensation Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioCardDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rolling Shutter Correction",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Corrects CMOS sensor gelatinous wobble on rapid pans",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = reduceRollingShutter,
                        onCheckedChange = { reduceRollingShutter = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = Color(0xFF004D40)
                        ),
                        modifier = Modifier.testTag("rolling_shutter_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Before / After Split Screen Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B2436))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Split View",
                            tint = if (isSplitPreviewActive) AccentCyan else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Split Screen Comparison",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Preview Unstabilized Raw vs Stabilized side-by-side",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Switch(
                        checked = isSplitPreviewActive,
                        onCheckedChange = {
                            isSplitPreviewActive = it
                            onToggleSplitPreview(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = Color(0xFF004D40)
                        ),
                        modifier = Modifier.testTag("split_preview_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apply Button
            Button(
                onClick = {
                    val settings = StabilizationSettings(
                        isEnabled = isEnabled,
                        mode = selectedMode,
                        smoothness = smoothness,
                        cropMarginPercent = cropMarginPercent,
                        reduceRollingShutter = reduceRollingShutter
                    )
                    onUpdateStabilization(settings)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_stabilization_button")
            ) {
                Text(
                    text = if (isEnabled) "Apply Hardware Stabilization" else "Save Settings",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
