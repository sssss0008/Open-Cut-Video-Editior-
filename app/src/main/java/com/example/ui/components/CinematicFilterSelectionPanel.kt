package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorGradingSettings
import com.example.data.model.LutPreset
import com.example.data.model.VideoClip
import com.example.data.model.VideoFilter
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark

data class CinematicPresetOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val filter: VideoFilter,
    val lut: LutPreset,
    val gradientColors: List<Color>,
    val defaultBrightness: Float = 0f,
    val defaultContrast: Float = 1.0f,
    val defaultSaturation: Float = 1.0f,
    val defaultExposure: Float = 0f,
    val defaultTemp: Float = 0f
)

val CINEMATIC_PRESET_CATALOG = listOf(
    CinematicPresetOption(
        id = "original",
        title = "Original Rec.709",
        subtitle = "Unmodified neutral grade",
        category = "All",
        filter = VideoFilter.NONE,
        lut = LutPreset.NONE,
        gradientColors = listOf(Color(0xFF334155), Color(0xFF64748B))
    ),
    CinematicPresetOption(
        id = "teal_orange",
        title = "Teal & Orange",
        subtitle = "Blockbuster cinema contrast",
        category = "Film",
        filter = VideoFilter.CINEMATIC,
        lut = LutPreset.TEAL_ORANGE,
        gradientColors = listOf(Color(0xFF0D9488), Color(0xFFF97316)),
        defaultContrast = 1.15f,
        defaultSaturation = 1.18f,
        defaultTemp = 0.12f
    ),
    CinematicPresetOption(
        id = "kodak_2383",
        title = "Kodak 2383 Print",
        subtitle = "Warm halation & rich shadows",
        category = "Film",
        filter = VideoFilter.WARM_DRAMA,
        lut = LutPreset.VINTAGE_KODAK,
        gradientColors = listOf(Color(0xFF7C2D12), Color(0xFFFBBF24)),
        defaultContrast = 1.12f,
        defaultSaturation = 1.10f,
        defaultTemp = 0.25f
    ),
    CinematicPresetOption(
        id = "cyberpunk_neon",
        title = "Cyberpunk 2077",
        subtitle = "Electric cyan & magenta split",
        category = "Stylized",
        filter = VideoFilter.CYBERPUNK,
        lut = LutPreset.CYBERPUNK_NEON,
        gradientColors = listOf(Color(0xFF00E5FF), Color(0xFFEC4899)),
        defaultContrast = 1.22f,
        defaultSaturation = 1.35f,
        defaultTemp = -0.18f
    ),
    CinematicPresetOption(
        id = "golden_hour",
        title = "Anamorphic Gold",
        subtitle = "Sunset warmth & soft highlights",
        category = "Mood",
        filter = VideoFilter.GOLDEN_HOUR,
        lut = LutPreset.GOLDEN_HOUR,
        gradientColors = listOf(Color(0xFFD97706), Color(0xFFFDE047)),
        defaultExposure = 0.15f,
        defaultSaturation = 1.20f,
        defaultTemp = 0.35f
    ),
    CinematicPresetOption(
        id = "bleach_bypass",
        title = "Bleach Bypass",
        subtitle = "Gritty silver retention look",
        category = "Film",
        filter = VideoFilter.HIGH_CONTRAST,
        lut = LutPreset.BLEACH_BYPASS,
        gradientColors = listOf(Color(0xFF1E293B), Color(0xFF94A3B8)),
        defaultContrast = 1.32f,
        defaultSaturation = 0.65f
    ),
    CinematicPresetOption(
        id = "film_noir",
        title = "Silver Noir B&W",
        subtitle = "Classic monochrome contrast",
        category = "Vintage",
        filter = VideoFilter.FILM_NOIR,
        lut = LutPreset.FILM_NOIR,
        gradientColors = listOf(Color(0xFF090D16), Color(0xFFE2E8F0)),
        defaultContrast = 1.28f,
        defaultSaturation = 0.0f
    ),
    CinematicPresetOption(
        id = "emerald_matrix",
        title = "Emerald Sci-Fi",
        subtitle = "Deep green shadow tint",
        category = "Stylized",
        filter = VideoFilter.EMERALD,
        lut = LutPreset.MATRIX_GREEN,
        gradientColors = listOf(Color(0xFF064E3B), Color(0xFF10B981)),
        defaultContrast = 1.15f,
        defaultTemp = -0.15f
    ),
    CinematicPresetOption(
        id = "nordic_chill",
        title = "Arctic Indie",
        subtitle = "Crisp slate-blue atmosphere",
        category = "Mood",
        filter = VideoFilter.COLD_NORDIC,
        lut = LutPreset.CINE_LOG_FLAT,
        gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF38BDF8)),
        defaultContrast = 1.06f,
        defaultSaturation = 0.88f,
        defaultTemp = -0.32f
    ),
    CinematicPresetOption(
        id = "retro_vhs",
        title = "90s Analog VHS",
        subtitle = "Faded tape warmth & chroma",
        category = "Vintage",
        filter = VideoFilter.VINTAGE_VHS,
        lut = LutPreset.VINTAGE_KODAK,
        gradientColors = listOf(Color(0xFF831843), Color(0xFFF59E0B)),
        defaultBrightness = 0.05f,
        defaultContrast = 0.94f,
        defaultSaturation = 1.15f
    ),
    CinematicPresetOption(
        id = "pastel_dream",
        title = "Pastel Dream",
        subtitle = "Airy lifted shadows & soft bloom",
        category = "Stylized",
        filter = VideoFilter.PASTEL,
        lut = LutPreset.GOLDEN_HOUR,
        gradientColors = listOf(Color(0xFFF472B6), Color(0xFF818CF8)),
        defaultBrightness = 0.08f,
        defaultContrast = 0.92f,
        defaultSaturation = 1.08f
    )
)

@Composable
fun CinematicFilterSelectionPanel(
    clip: VideoClip,
    allClipsCount: Int = 1,
    onSelectFilter: (VideoFilter) -> Unit,
    onUpdateFilterIntensity: (Float) -> Unit,
    onUpdateColorGrading: (ColorGradingSettings) -> Unit,
    onUpdateBasicGrading: (brightness: Float, contrast: Float, saturation: Float) -> Unit,
    onApplyPresetToAllClips: (VideoFilter, LutPreset) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Film", "Mood", "Stylized", "Vintage")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == "All") {
            CINEMATIC_PRESET_CATALOG
        } else {
            CINEMATIC_PRESET_CATALOG.filter { it.category == selectedCategory || it.id == "original" }
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioSurfaceDark,
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cinematic_filter_selection_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AccentCyan.copy(alpha = 0.18f),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = "Cinematic Filters",
                                tint = AccentCyan,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Cinematic Video Filters & LUT Presets",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target: ${clip.name.take(18)} • Active: ${clip.filter.displayName}",
                            color = AccentCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                // Reset filter button
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StudioCardDark,
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .clickable {
                            onSelectFilter(VideoFilter.NONE)
                            onUpdateFilterIntensity(1.0f)
                            onUpdateBasicGrading(0f, 1f, 1f)
                            onUpdateColorGrading(ColorGradingSettings())
                        }
                        .testTag("reset_cinematic_filter_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Filter",
                            tint = Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.22f) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF334155)),
                        modifier = Modifier
                            .clickable { selectedCategory = category }
                            .testTag("filter_category_chip_${category.lowercase()}")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) AccentCyan else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Horizontal Scrollable Cinematic Filter Swatches
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cinematic_filter_presets_row")
            ) {
                items(filteredPresets, key = { it.id }) { preset ->
                    val isSelected = clip.filter == preset.filter &&
                        (preset.id == "original" || clip.colorGrading.lut == preset.lut || clip.filter == preset.filter)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF17253B) else StudioCardDark,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AccentCyan else Color(0xFF2D3748)
                        ),
                        modifier = Modifier
                            .width(118.dp)
                            .clickable {
                                onSelectFilter(preset.filter)
                                onUpdateBasicGrading(
                                    preset.defaultBrightness,
                                    preset.defaultContrast,
                                    preset.defaultSaturation
                                )
                                onUpdateColorGrading(
                                    clip.colorGrading.copy(
                                        lut = preset.lut,
                                        exposure = preset.defaultExposure,
                                        temperature = preset.defaultTemp
                                    )
                                )
                            }
                            .testTag("cinematic_preset_card_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Visual Color Grading Swatch
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(preset.gradientColors))
                            ) {
                                // Simulated film frame bars
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .align(Alignment.TopCenter)
                                        .background(Color.Black.copy(alpha = 0.45f))
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.45f))
                                )

                                // Category badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = preset.category.uppercase(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }

                                if (isSelected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AccentCyan,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = preset.title,
                                color = if (isSelected) AccentCyan else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = preset.subtitle,
                                color = Color.Gray,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Intensity Slider + Apply to All Clips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Preset Blend Intensity",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(clip.filterIntensity * 100).toInt()}%",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = clip.filterIntensity,
                        onValueChange = onUpdateFilterIntensity,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan
                        ),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("cinematic_filter_intensity_slider")
                    )
                }

                if (allClipsCount > 1) {
                    OutlinedButton(
                        onClick = {
                            onApplyPresetToAllClips(clip.filter, clip.colorGrading.lut)
                        },
                        border = BorderStroke(1.dp, AccentPink.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("apply_filter_all_clips_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Apply to All Clips",
                            tint = AccentPink,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Apply All ($allClipsCount)",
                            color = AccentPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
