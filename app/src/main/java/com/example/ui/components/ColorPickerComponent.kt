package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioCardDark
import kotlin.math.roundToInt

data class ColorThemePalette(
    val name: String,
    val colors: List<Long>
)

object ColorPalettes {
    val presetThemes = listOf(
        ColorThemePalette(
            name = "Neon Cyber",
            colors = listOf(
                0xFF00E5FF, // Cyan
                0xFFF43F5E, // Neon Pink
                0xFF10B981, // Emerald Green
                0xFF8B5CF6, // Electric Violet
                0xFFFACC15, // Cyber Yellow
                0xFF06B6D4, // Aqua
                0xFFEC4899, // Magenta
                0xFF3B82F6  // Laser Blue
            )
        ),
        ColorThemePalette(
            name = "Cinema Gold",
            colors = listOf(
                0xFFF59E0B, // Amber Gold
                0xFFD97706, // Rich Gold
                0xFFB45309, // Bronze
                0xFF92400E, // Copper
                0xFFFEF3C7, // Champagne
                0xFFFBBF24, // Warm Sun
                0xFF78350F, // Deep Mocha
                0xFFEAB308  // Cinematic Yellow
            )
        ),
        ColorThemePalette(
            name = "Sunset Pastel",
            colors = listOf(
                0xFFFB7185, // Pastel Rose
                0xFFF472B6, // Soft Fuchsia
                0xFFC084FC, // Lavender
                0xFFFDBA74, // Peach
                0xFF38BDF8, // Soft Sky
                0xFFA7F3D0, // Mint
                0xFFFED7AA, // Apricot
                0xFFDDD6FE  // Lilac
            )
        ),
        ColorThemePalette(
            name = "Monochromatic",
            colors = listOf(
                0xFFFFFFFF, // Pure White
                0xFFF1F5F9, // Off White
                0xFFCBD5E1, // Silver
                0xFF94A3B8, // Slate Gray
                0xFF64748B, // Steel
                0xFF334155, // Charcoal
                0xFF1E293B, // Deep Navy Slate
                0xFF000000  // Jet Black
            )
        ),
        ColorThemePalette(
            name = "Bold Retro",
            colors = listOf(
                0xFFEF4444, // Retro Red
                0xFFF97316, // Orange
                0xFF84CC16, // Olive Lime
                0xFF065F46, // Vintage Pine
                0xFF1E40AF, // Cobalt
                0xFF701A75, // Plum
                0xFFD946EF, // Hot Orchid
                0xFFCA8A04  // Mustard
            )
        ),
        ColorThemePalette(
            name = "Minimalist Studio",
            colors = listOf(
                0xFFE0E7FF, // Soft Indigo
                0xFFCCFBF1, // Pale Teal
                0xFFFEF9C3, // Muted Cream
                0xFFFEE2E2, // Soft Coral
                0xFF475569, // Studio Mid-gray
                0xFF0F172A, // Studio Night
                0xFF14B8A6, // Soft Turquoise
                0xFF6366F1  // Deep Indigo
            )
        ),
        ColorThemePalette(
            name = "Social Creator",
            colors = listOf(
                0xFFFF0055, // TikTok Pink
                0xFF00F2FE, // Aqua Blue
                0xFFFF5722, // YouTube Orange-Red
                0xFF25D366, // WhatsApp Green
                0xFF9B51E0, // Twitch Purple
                0xFF5865F2, // Discord Blurple
                0xFFE1306C, // Instagram Magenta
                0xFFFFFC00  // Snapchat Yellow
            )
        )
    )
}

/**
 * Utility: format color Long to Hex String (e.g., "#00E5FF" or "#CC00E5FF")
 */
fun formatColorToHex(colorLong: Long, includeAlpha: Boolean = true): String {
    val a = ((colorLong shr 24) and 0xFF).toInt()
    val r = ((colorLong shr 16) and 0xFF).toInt()
    val g = ((colorLong shr 8) and 0xFF).toInt()
    val b = (colorLong and 0xFF).toInt()
    return if (includeAlpha && a != 255) {
        String.format("#%02X%02X%02X%02X", a, r, g, b)
    } else {
        String.format("#%02X%02X%02X", r, g, b)
    }
}

/**
 * Utility: parse user typed hex to ARGB color Long
 */
fun parseHexToColor(hex: String): Long? {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> {
                val rgb = clean.toLong(16)
                0xFF000000L or rgb
            }
            8 -> {
                clean.toLong(16)
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Sophisticated Color Picker Component supporting:
 * 1. Preset themed palettes (Neon Cyber, Cinema Gold, Sunset Pastel, Monochromatic, etc.)
 * 2. Real-time Hue Rainbow slider
 * 3. Alpha / Opacity slider
 * 4. Custom Hex input with instant validation and copy/paste
 * 5. Visual color swatch checkmarks
 */
@Composable
fun ColorPickerComponent(
    selectedColorHex: Long,
    onColorChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Color Palette & Custom Hex",
    showAlphaSlider: Boolean = true
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    var selectedThemeIndex by remember { mutableIntStateOf(0) }
    val currentTheme = ColorPalettes.presetThemes[selectedThemeIndex]

    // Hex text field state
    var hexInputText by remember(selectedColorHex) {
        mutableStateOf(formatColorToHex(selectedColorHex, includeAlpha = showAlphaSlider))
    }
    var hexError by remember { mutableStateOf(false) }

    // Hue (0..360f) and Alpha (0..1f)
    var currentHue by remember {
        val hsv = FloatArray(3)
        val color = Color(selectedColorHex)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        mutableFloatStateOf(hsv[0])
    }

    var currentAlpha by remember(selectedColorHex) {
        val a = ((selectedColorHex shr 24) and 0xFF).toFloat() / 255f
        mutableFloatStateOf(a)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioCardDark)
            .border(1.dp, Color(0xFF2C2840), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Section Header with Live Swatch & Hex Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live Swatch Circle with double border
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(selectedColorHex))
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .testTag("color_picker_live_swatch")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatColorToHex(selectedColorHex, includeAlpha = showAlphaSlider),
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Copy Hex Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Color Hex", formatColorToHex(selectedColorHex, showAlphaSlider))
                        clipboard.setPrimaryClip(clip)
                    },
                    modifier = Modifier.size(36.dp).testTag("copy_hex_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Hex",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                        parseHexToColor(clipText)?.let { parsed ->
                            onColorChanged(parsed)
                            hexInputText = formatColorToHex(parsed, showAlphaSlider)
                            hexError = false
                        }
                    },
                    modifier = Modifier.size(36.dp).testTag("paste_hex_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste Hex",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Hex Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hexInputText,
                onValueChange = { input ->
                    val upper = input.uppercase().trim()
                    hexInputText = upper
                    val parsed = parseHexToColor(upper)
                    if (parsed != null) {
                        hexError = false
                        onColorChanged(parsed)
                    } else {
                        hexError = upper.isNotEmpty() && !upper.startsWith("#") && upper.length > 8
                    }
                },
                label = { Text("Custom Hex Color (#RRGGBB / #AARRGGBB)", fontSize = 11.sp) },
                singleLine = true,
                isError = hexError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = Color(0xFF3B4252),
                    errorBorderColor = Color(0xFFEF4444)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("custom_hex_input")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hue Rainbow Slider
        Text(
            text = "Hue Spectrum",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF0000),
                            Color(0xFFFFFF00),
                            Color(0xFF00FF00),
                            Color(0xFF00FFFF),
                            Color(0xFF0000FF),
                            Color(0xFFFF00FF),
                            Color(0xFFFF0000)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(13.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        currentHue = ratio * 360f
                        val hsv = floatArrayOf(currentHue, 1f, 1f)
                        val rgb = android.graphics.Color.HSVToColor((currentAlpha * 255).toInt(), hsv)
                        onColorChanged(rgb.toLong() and 0xFFFFFFFFL)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        currentHue = ratio * 360f
                        val hsv = floatArrayOf(currentHue, 1f, 1f)
                        val rgb = android.graphics.Color.HSVToColor((currentAlpha * 255).toInt(), hsv)
                        onColorChanged(rgb.toLong() and 0xFFFFFFFFL)
                    }
                }
                .testTag("hue_rainbow_slider")
        ) {
            val thumbOffsetXDp = (maxWidth - 20.dp) * (currentHue / 360f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetXDp)
                    .size(20.dp)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
            )
        }

        // Optional Alpha Slider
        if (showAlphaSlider) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opacity / Alpha",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(currentAlpha * 100).roundToInt()}%",
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(selectedColorHex and 0x00FFFFFFL),
                                Color(selectedColorHex or 0xFF000000L)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                            currentAlpha = ratio
                            val alphaInt = (ratio * 255).toInt().coerceIn(0, 255)
                            val rgb = selectedColorHex and 0x00FFFFFFL
                            val newColor = ((alphaInt.toLong() and 0xFF) shl 24) or rgb
                            onColorChanged(newColor)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                            currentAlpha = ratio
                            val alphaInt = (ratio * 255).toInt().coerceIn(0, 255)
                            val rgb = selectedColorHex and 0x00FFFFFFL
                            val newColor = ((alphaInt.toLong() and 0xFF) shl 24) or rgb
                            onColorChanged(newColor)
                        }
                    }
                    .testTag("alpha_opacity_slider")
            ) {
                val thumbOffsetXDp = (maxWidth - 20.dp) * currentAlpha.coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffsetXDp)
                        .size(20.dp)
                        .align(Alignment.CenterStart)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.Black, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Theme Palette Selector Tabs
        Text(
            text = "Curated Preset Themes",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))

        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ColorPalettes.presetThemes.forEachIndexed { index, palette ->
                val isSelected = selectedThemeIndex == index
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) AccentCyan else Color(0xFF1E2638),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) AccentCyan else Color(0xFF333E54)
                    ),
                    modifier = Modifier
                        .clickable { selectedThemeIndex = index }
                        .testTag("theme_tab_${palette.name.lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = palette.name,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Swatches for Active Theme Palette
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            items(currentTheme.colors) { colorLong ->
                val isCurrent = (selectedColorHex and 0x00FFFFFFL) == (colorLong and 0x00FFFFFFL)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(colorLong))
                        .border(
                            width = if (isCurrent) 2.5.dp else 1.dp,
                            color = if (isCurrent) Color.White else Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable {
                            // Maintain alpha if showAlphaSlider is true
                            val finalColor = if (showAlphaSlider) {
                                val a = ((selectedColorHex shr 24) and 0xFF)
                                (a shl 24) or (colorLong and 0x00FFFFFFL)
                            } else {
                                colorLong
                            }
                            onColorChanged(finalColor)
                            hexInputText = formatColorToHex(finalColor, showAlphaSlider)
                            hexError = false
                        }
                        .testTag("swatch_${formatColorToHex(colorLong, false).removePrefix("#")}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrent) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (Color(colorLong).luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
