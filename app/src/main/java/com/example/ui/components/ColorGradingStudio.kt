package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorGradingSettings
import com.example.data.model.LutPreset
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ColorGradingSubTab(val title: String) {
    WHEELS("Color Wheels"),
    PRIMARY("Primary Balance"),
    LUTS("Cinematic LUTs"),
    SCOPES("Video Scopes")
}

/**
 * DaVinci Resolve Style Professional Color Grading Studio Component.
 * Features 3-Way Lift/Gamma/Gain color wheels, Primary balance sliders,
 * Cinematic 3D LUT presets, and real-time Waveform / Vectorscope / Histogram scopes.
 */
@Composable
fun ColorGradingStudio(
    clip: VideoClip,
    onUpdateGrading: (ColorGradingSettings) -> Unit,
    onResetGrading: () -> Unit,
    isCompareSplit: Boolean,
    onToggleCompareSplit: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ColorGradingSubTab.WHEELS) }
    val grading = clip.colorGrading

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfaceDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("color_grading_studio")
    ) {
        // Studio Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFB300).copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color Grading",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Color Grading Studio",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Clip: ${clip.name.take(16)} • Rec.709 10-bit",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Split-screen Before/After compare toggle
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCompareSplit) AccentPink.copy(alpha = 0.25f) else StudioCardDark,
                    border = BorderStroke(1.dp, if (isCompareSplit) AccentPink else Color(0xFF374151)),
                    modifier = Modifier
                        .clickable { onToggleCompareSplit() }
                        .testTag("color_compare_split_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Compare",
                            tint = if (isCompareSplit) AccentPink else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCompareSplit) "A/B SPLIT" else "COMPARE",
                            color = if (isCompareSplit) AccentPink else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Reset
                IconButton(onClick = onResetGrading, modifier = Modifier.size(28.dp).testTag("color_reset_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Close
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Studio Navigation Sub-Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StudioDarkBg)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ColorGradingSubTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) Color(0xFFFFB300) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = tab }
                        .testTag("color_tab_${tab.name.lowercase()}")
                ) {
                    Text(
                        text = tab.title,
                        color = if (isSelected) Color.Black else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area based on selected sub-tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            when (selectedTab) {
                ColorGradingSubTab.WHEELS -> {
                    ColorWheelsPane(
                        grading = grading,
                        onUpdate = onUpdateGrading
                    )
                }
                ColorGradingSubTab.PRIMARY -> {
                    PrimaryBalancePane(
                        clip = clip,
                        grading = grading,
                        onUpdate = onUpdateGrading
                    )
                }
                ColorGradingSubTab.LUTS -> {
                    LutPacksPane(
                        selectedLut = grading.lut,
                        onSelectLut = { newLut ->
                            onUpdateGrading(grading.copy(lut = newLut))
                        }
                    )
                }
                ColorGradingSubTab.SCOPES -> {
                    VideoScopesPane(
                        grading = grading
                    )
                }
            }
        }
    }
}

/**
 * 3-Way Lift / Gamma / Gain Color Wheels Pane.
 */
@Composable
fun ColorWheelsPane(
    grading: ColorGradingSettings,
    onUpdate: (ColorGradingSettings) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Lift (Shadows)
        ColorWheelItem(
            title = "LIFT (Shadows)",
            offsetR = grading.liftR,
            offsetB = grading.liftB,
            pedestal = grading.liftPedestal,
            accentColor = Color(0xFF60A5FA),
            onWheelChange = { r, b ->
                onUpdate(grading.copy(liftR = r, liftB = b))
            },
            onPedestalChange = { ped ->
                onUpdate(grading.copy(liftPedestal = ped))
            },
            modifier = Modifier.weight(1f)
        )

        // Gamma (Midtones)
        ColorWheelItem(
            title = "GAMMA (Mids)",
            offsetR = grading.gammaR,
            offsetB = grading.gammaB,
            pedestal = grading.gammaPedestal,
            accentColor = Color(0xFF34D399),
            onWheelChange = { r, b ->
                onUpdate(grading.copy(gammaR = r, gammaB = b))
            },
            onPedestalChange = { ped ->
                onUpdate(grading.copy(gammaPedestal = ped))
            },
            modifier = Modifier.weight(1f)
        )

        // Gain (Highlights)
        ColorWheelItem(
            title = "GAIN (Highs)",
            offsetR = grading.gainR,
            offsetB = grading.gainB,
            pedestal = grading.gainPedestal,
            accentColor = Color(0xFFFBBF24),
            onWheelChange = { r, b ->
                onUpdate(grading.copy(gainR = r, gainB = b))
            },
            onPedestalChange = { ped ->
                onUpdate(grading.copy(gainPedestal = ped))
            },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Interactive 2D Color Balance Wheel.
 */
@Composable
fun ColorWheelItem(
    title: String,
    offsetR: Float,
    offsetB: Float,
    pedestal: Float,
    accentColor: Color,
    onWheelChange: (r: Float, b: Float) -> Unit,
    onPedestalChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StudioCardDark,
        border = BorderStroke(1.dp, Color(0xFF2B3346)),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Circular 2D Interactive Trackpad
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F1522))
                    .border(1.dp, Color(0xFF374151), CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val sensitivity = 0.005f
                            val newR = (offsetR + dragAmount.x * sensitivity).coerceIn(-0.4f, 0.4f)
                            val newB = (offsetB + dragAmount.y * sensitivity).coerceIn(-0.4f, 0.4f)
                            onWheelChange(newR, newB)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Crosshairs
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    drawLine(Color(0x33FFFFFF), Offset(cx, 0f), Offset(cx, size.height), strokeWidth = 1f)
                    drawLine(Color(0x33FFFFFF), Offset(0f, cy), Offset(size.width, cy), strokeWidth = 1f)
                    drawCircle(Color(0x22FFFFFF), radius = size.width * 0.35f, style = Stroke(1f))

                    // Indicator dot positioned by offsetR & offsetB
                    val dotX = cx + offsetR * (size.width * 0.9f)
                    val dotY = cy + offsetB * (size.height * 0.9f)
                    drawCircle(Color.White, radius = 4.5f, center = Offset(dotX, dotY))
                    drawCircle(accentColor, radius = 2.5f, center = Offset(dotX, dotY))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Pedestal / Master Level Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Y", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = String.format("%+.2f", pedestal),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Slider(
                value = pedestal,
                onValueChange = onPedestalChange,
                valueRange = -0.5f..0.5f,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF374151)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )

            // Double tap to zero out
            Text(
                text = "Tap to reset",
                color = Color(0xFF6B7280),
                fontSize = 8.sp,
                modifier = Modifier
                    .clickable {
                        onWheelChange(0f, 0f)
                        onPedestalChange(0f)
                    }
                    .padding(top = 2.dp)
            )
        }
    }
}

/**
 * Primary Balance Sliders Pane (Exposure, Temp, Tint, Vibrance, Saturation, Contrast).
 */
@Composable
fun PrimaryBalancePane(
    clip: VideoClip,
    grading: ColorGradingSettings,
    onUpdate: (ColorGradingSettings) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Exposure (-2.0 to +2.0 EV)
        BalanceSliderRow(
            label = "Exposure",
            valueText = String.format("%+.1f EV", grading.exposure),
            value = grading.exposure,
            range = -2.0f..2.0f,
            activeColor = Color(0xFFFBBF24),
            onValueChange = { onUpdate(grading.copy(exposure = it)) },
            onReset = { onUpdate(grading.copy(exposure = 0f)) }
        )

        // Temperature (Cool Blue -1.0 to Warm Amber +1.0)
        BalanceSliderRow(
            label = "Temperature",
            valueText = when {
                grading.temperature > 0.05f -> String.format("+%dK Warm", (grading.temperature * 1500).toInt())
                grading.temperature < -0.05f -> String.format("%dK Cool", (grading.temperature * 1500).toInt())
                else -> "6500K Neutral"
            },
            value = grading.temperature,
            range = -1.0f..1.0f,
            activeColor = if (grading.temperature >= 0f) Color(0xFFF97316) else Color(0xFF38BDF8),
            onValueChange = { onUpdate(grading.copy(temperature = it)) },
            onReset = { onUpdate(grading.copy(temperature = 0f)) }
        )

        // Tint (Green -1.0 to Magenta +1.0)
        BalanceSliderRow(
            label = "Tint",
            valueText = String.format("%+.2f", grading.tint),
            value = grading.tint,
            range = -1.0f..1.0f,
            activeColor = if (grading.tint >= 0f) Color(0xFFEC4899) else Color(0xFF10B981),
            onValueChange = { onUpdate(grading.copy(tint = it)) },
            onReset = { onUpdate(grading.copy(tint = 0f)) }
        )

        // Vibrance (-1.0 to +1.0)
        BalanceSliderRow(
            label = "Vibrance",
            valueText = "${(grading.vibrance * 100).toInt()}%",
            value = grading.vibrance,
            range = -1.0f..1.0f,
            activeColor = AccentCyan,
            onValueChange = { onUpdate(grading.copy(vibrance = it)) },
            onReset = { onUpdate(grading.copy(vibrance = 0f)) }
        )
    }
}

@Composable
fun BalanceSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StudioCardDark,
        border = BorderStroke(1.dp, Color(0xFF283042)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = valueText,
                        color = activeColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset",
                        color = Color.Gray,
                        fontSize = 9.sp,
                        modifier = Modifier.clickable { onReset() }
                    )
                }
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = activeColor,
                    activeTrackColor = activeColor,
                    inactiveTrackColor = Color(0xFF374151)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
        }
    }
}

/**
 * Cinematic 3D LUT Packs Pane.
 */
@Composable
fun LutPacksPane(
    selectedLut: LutPreset,
    onSelectLut: (LutPreset) -> Unit
) {
    val luts = LutPreset.entries.toList()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select 3D Film Emulation & Grading Look:",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(luts) { lut ->
                val isSelected = selectedLut == lut
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF2E2213) else StudioCardDark,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Color(0xFFFFB300) else Color(0xFF2C3548)
                    ),
                    modifier = Modifier
                        .width(135.dp)
                        .clickable { onSelectLut(lut) }
                        .testTag("lut_preset_${lut.name}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Category Pill
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFB300).copy(alpha = 0.2f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = lut.category.uppercase(),
                                color = Color(0xFFFFB300),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = lut.displayName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = lut.description,
                            color = Color.LightGray,
                            fontSize = 9.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) Color(0xFFFFB300) else Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isSelected) "APPLIED" else "APPLY",
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Real-Time Video Scopes Pane (Waveform Monitor, Vectorscope, Histogram).
 */
@Composable
fun VideoScopesPane(
    grading: ColorGradingSettings
) {
    var scopeType by remember { mutableStateOf("WAVEFORM") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Scope Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("WAVEFORM" to "Waveform (IRE)", "VECTORSCOPE" to "Vectorscope (Hue)", "HISTOGRAM" to "RGB Histogram").forEach { (type, label) ->
                val isSel = scopeType == type
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSel) AccentCyan else StudioCardDark,
                    modifier = Modifier
                        .clickable { scopeType = type }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSel) Color.Black else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Live Scopes Canvas
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF090D14),
            border = BorderStroke(1.dp, Color(0xFF1F2937)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (scopeType) {
                "WAVEFORM" -> LiveWaveformScope(grading = grading)
                "VECTORSCOPE" -> LiveVectorscopeScope(grading = grading)
                else -> LiveHistogramScope(grading = grading)
            }
        }
    }
}

@Composable
fun LiveWaveformScope(grading: ColorGradingSettings) {
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val w = size.width
        val h = size.height

        // Reference Grids: 0 IRE, 50 IRE, 100 IRE
        drawLine(Color(0x33FFFFFF), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
        drawLine(Color(0x33FFFFFF), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)
        drawLine(Color(0x33FFFFFF), Offset(0f, h), Offset(w, h), strokeWidth = 1f)

        // Waveform luminance trace simulation influenced by exposure, temperature, and lift/gain
        val expShift = grading.exposure * (h * 0.12f)
        val tempShift = grading.temperature * 15f
        val points = 60
        val stepX = w / points

        val rPath = Path()
        val gPath = Path()
        val bPath = Path()

        for (i in 0..points) {
            val x = i * stepX
            val baseline = h * 0.5f - expShift
            val wave = sin((i * 0.35f).toDouble()).toFloat() * (h * 0.25f)
            val noise = (sin((i * 1.7f).toDouble()).toFloat() * 12f)

            val yR = (baseline + wave * 0.85f + noise - tempShift).coerceIn(4f, h - 4f)
            val yG = (baseline + wave * 0.95f + noise).coerceIn(4f, h - 4f)
            val yB = (baseline + wave * 1.05f + noise + tempShift).coerceIn(4f, h - 4f)

            if (i == 0) {
                rPath.moveTo(x, yR)
                gPath.moveTo(x, yG)
                bPath.moveTo(x, yB)
            } else {
                rPath.lineTo(x, yR)
                gPath.lineTo(x, yG)
                bPath.lineTo(x, yB)
            }
        }

        drawPath(rPath, Color(0x99EF4444), style = Stroke(1.5f))
        drawPath(gPath, Color(0x9910B981), style = Stroke(1.5f))
        drawPath(bPath, Color(0x993B82F6), style = Stroke(1.5f))
    }
}

@Composable
fun LiveVectorscopeScope(grading: ColorGradingSettings) {
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = (minOf(size.width, size.height) / 2f) * 0.85f

        // Outer chromatic boundary
        drawCircle(Color(0x33FFFFFF), radius = r, style = Stroke(1.2f))
        drawCircle(Color(0x1AFFFFFF), radius = r * 0.5f, style = Stroke(1f))

        // Skin Tone Indicator Axis Line (~10 o'clock)
        val skinAngle = -0.65 // radians (~140 deg)
        val skinX = cx + cos(skinAngle).toFloat() * r
        val skinY = cy + sin(skinAngle).toFloat() * r
        drawLine(Color(0xFFFFB300), Offset(cx, cy), Offset(skinX, skinY), strokeWidth = 1.2f)

        // Color saturation blob offset by temperature and tint
        val shiftX = grading.temperature * (r * 0.4f)
        val shiftY = grading.tint * (r * 0.4f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x8800E5FF), Color(0x44EC4899), Color.Transparent),
                center = Offset(cx + shiftX, cy + shiftY),
                radius = r * 0.45f
            ),
            radius = r * 0.45f,
            center = Offset(cx + shiftX, cy + shiftY)
        )
    }
}

@Composable
fun LiveHistogramScope(grading: ColorGradingSettings) {
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val w = size.width
        val h = size.height
        val bins = 40
        val binWidth = w / bins

        for (i in 0 until bins) {
            val norm = i / bins.toFloat()
            val heightR = (sin(norm * Math.PI) * (h * 0.8f) * (1f + grading.exposure * 0.2f)).toFloat().coerceIn(2f, h)
            val heightB = (sin((norm + 0.1) * Math.PI) * (h * 0.75f)).toFloat().coerceIn(2f, h)

            val x = i * binWidth
            drawRect(Color(0x66EF4444), Offset(x, h - heightR), androidx.compose.ui.geometry.Size(binWidth * 0.8f, heightR))
            drawRect(Color(0x663B82F6), Offset(x, h - heightB), androidx.compose.ui.geometry.Size(binWidth * 0.8f, heightB))
        }
    }
}

/**
 * Modal Bottom Sheet container for ColorGradingStudio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorGradingSheet(
    clip: VideoClip,
    onUpdateGrading: (ColorGradingSettings) -> Unit,
    onResetGrading: () -> Unit,
    isCompareSplit: Boolean,
    onToggleCompareSplit: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        ColorGradingStudio(
            clip = clip,
            onUpdateGrading = onUpdateGrading,
            onResetGrading = onResetGrading,
            isCompareSplit = isCompareSplit,
            onToggleCompareSplit = onToggleCompareSplit,
            onClose = onDismiss
        )
    }
}
