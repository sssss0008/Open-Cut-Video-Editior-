package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DiagnosticSeverity
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.util.MediaDiagnostics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CropAndResizeSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onApplyCrop: (left: Float, top: Float, right: Float, bottom: Float) -> Unit,
    onResetCrop: () -> Unit,
    onApplyResize: (width: Int, height: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var left by remember(clip.id) { mutableFloatStateOf(clip.cropLeft) }
    var top by remember(clip.id) { mutableFloatStateOf(clip.cropTop) }
    var right by remember(clip.id) { mutableFloatStateOf(clip.cropRight) }
    var bottom by remember(clip.id) { mutableFloatStateOf(clip.cropBottom) }
    var resizeW by remember(clip.id) { mutableIntStateOf(clip.resizeWidth) }
    var resizeH by remember(clip.id) { mutableIntStateOf(clip.resizeHeight) }

    val resizeOptions = listOf(
        Triple("Native Source", 0, 0),
        Triple("9:16 (1080×1920)", 1080, 1920),
        Triple("16:9 (1920×1080)", 1920, 1080),
        Triple("HD (1280×720)", 1280, 720),
        Triple("Square (1080×1080)", 1080, 1080)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("crop_resize_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Crop, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crop & Resize Clip",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Text(
                text = clip.name,
                color = AccentCyan,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Quick Aspect Crop Presets
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "Full" to floatArrayOf(0f, 0f, 1f, 1f),
                    "1:1 Square" to floatArrayOf(0.15f, 0.15f, 0.85f, 0.85f),
                    "Center Tight" to floatArrayOf(0.10f, 0.10f, 0.90f, 0.90f),
                    "Cinema 21:9" to floatArrayOf(0f, 0.20f, 1f, 0.80f),
                    "Pillar 9:16" to floatArrayOf(0.22f, 0f, 0.78f, 1f)
                ).forEach { (label, coords) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioCardDark,
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.clickable {
                            left = coords[0]
                            top = coords[1]
                            right = coords[2]
                            bottom = coords[3]
                        }
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Normalized Edge Crop Sliders
            CropEdgeSlider("Left Crop", "${(left * 100).toInt()}%", left, 0f..0.45f) {
                left = it.coerceAtMost(right - 0.15f)
            }
            CropEdgeSlider("Right Edge", "${(right * 100).toInt()}%", right, 0.55f..1f) {
                right = it.coerceAtLeast(left + 0.15f)
            }
            CropEdgeSlider("Top Crop", "${(top * 100).toInt()}%", top, 0f..0.45f) {
                top = it.coerceAtMost(bottom - 0.15f)
            }
            CropEdgeSlider("Bottom Edge", "${(bottom * 100).toInt()}%", bottom, 0.55f..1f) {
                bottom = it.coerceAtLeast(top + 0.15f)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Output Clip Dimensions (Resize)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                resizeOptions.forEach { (label, w, h) ->
                    val selected = resizeW == w && resizeH == h
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) AccentCyan.copy(alpha = 0.2f) else StudioCardDark,
                        border = BorderStroke(1.dp, if (selected) AccentCyan else Color(0xFF334155)),
                        modifier = Modifier.clickable {
                            resizeW = w
                            resizeH = h
                        }
                    ) {
                        Text(
                            text = label,
                            color = if (selected) AccentCyan else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onResetCrop()
                        onApplyResize(0, 0)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_crop_button")
                ) {
                    Text("Reset", color = Color.White)
                }
                Button(
                    onClick = {
                        onApplyCrop(left, top, right, bottom)
                        onApplyResize(resizeW, resizeH)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("apply_crop_resize_button")
                ) {
                    Text("Apply Crop & Resize", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CropEdgeSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.width(86.dp)
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            color = AccentCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(42.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipAudioFadesSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onUpdateFades: (fadeInMs: Long, fadeOutMs: Long) -> Unit,
    onUpdateClipVolume: (volume: Float, isMuted: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fadeInMs by remember(clip.id) { mutableLongStateOf(clip.fadeInMs) }
    var fadeOutMs by remember(clip.id) { mutableLongStateOf(clip.fadeOutMs) }
    var volume by remember(clip.id) { mutableFloatStateOf(clip.volume) }
    var isMuted by remember(clip.id) { mutableStateOf(clip.isMuted) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("clip_audio_fades_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clip Audio & Fades",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mute Clip Audio", color = Color.LightGray, fontSize = 13.sp)
                Switch(
                    checked = isMuted,
                    onCheckedChange = {
                        isMuted = it
                        onUpdateClipVolume(volume, it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentPink)
                )
            }

            Text(
                text = "Clip Volume: ${(volume * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Slider(
                value = volume,
                onValueChange = {
                    volume = it
                    onUpdateClipVolume(it, isMuted)
                },
                valueRange = 0f..1.5f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Fade In Duration: ${String.format("%.1f", fadeInMs / 1000f)}s",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Slider(
                value = fadeInMs.toFloat(),
                onValueChange = { fadeInMs = it.toLong() },
                valueRange = 0f..5000f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Text(
                text = "Fade Out Duration: ${String.format("%.1f", fadeOutMs / 1000f)}s",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Slider(
                value = fadeOutMs.toFloat(),
                onValueChange = { fadeOutMs = it.toLong() },
                valueRange = 0f..5000f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0L to "Off", 500L to "0.5s", 1500L to "1.5s", 3000L to "3.0s").forEach { (ms, label) ->
                    OutlinedButton(
                        onClick = {
                            fadeInMs = ms
                            fadeOutMs = ms
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Button(
                onClick = {
                    onUpdateFades(fadeInMs, fadeOutMs)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 16.dp)
                    .testTag("apply_clip_fades_button")
            ) {
                Text("Apply Audio Envelope", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val logs by MediaDiagnostics.logs.collectAsStateWithLifecycle()
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("diagnostics_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BugReport, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Media Diagnostics Log (${logs.size})",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { MediaDiagnostics.clear() },
                        modifier = Modifier.testTag("clear_diagnostics_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Logs", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", color = Color.LightGray, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                    }
                }
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No import, codec, playback, or export events logged yet.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { entry ->
                        val badgeColor = when (entry.severity) {
                            DiagnosticSeverity.ERROR -> Color(0xFFEF4444)
                            DiagnosticSeverity.WARNING -> Color(0xFFF59E0B)
                            DiagnosticSeverity.INFO -> AccentCyan
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StudioCardDark,
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = badgeColor.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "${entry.severity.name} • ${entry.category}",
                                            color = badgeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = formatter.format(Date(entry.timestampMs)),
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.message,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (entry.details.isNotBlank()) {
                                    Text(
                                        text = entry.details,
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 2.dp)
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
