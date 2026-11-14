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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Waves
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoClip
import com.example.data.model.VideoEffectItem
import com.example.data.model.VideoEffectType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenShotEffectsSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onAddEffect: (VideoEffectType) -> Unit,
    onRemoveEffect: (String) -> Unit,
    onUpdateEffect: (id: String, intensity: Float, secondary: Float) -> Unit,
    onToggleEffect: (id: String, enabled: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Color", "Stylize", "Lens", "Layout", "Distort", "Blur")

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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, "Effects", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OpenShot Video Effects",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apply real-time effects to \"${clip.name}\"",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_effects_sheet")) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Applied Effects Section
            if (clip.effects.isNotEmpty()) {
                Text(
                    text = "Applied Effects (${clip.effects.size})",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (clip.effects.size > 2) 200.dp else 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clip.effects, key = { it.id }) { fx ->
                        ActiveEffectCard(
                            effect = fx,
                            onRemove = { onRemoveEffect(fx.id) },
                            onToggle = { onToggleEffect(fx.id, it) },
                            onUpdate = { intensity, secondary ->
                                onUpdateEffect(fx.id, intensity, secondary)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AccentCyan else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentCyan else Color(0xFF38315E)
                        ),
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .testTag("effect_cat_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Effects Library Grid
            val filteredEffects = VideoEffectType.entries.filter {
                selectedCategory == "All" || it.category == selectedCategory
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                items(filteredEffects) { effectType ->
                    val isAlreadyApplied = clip.effects.any { it.type == effectType }
                    EffectCatalogItem(
                        effectType = effectType,
                        isApplied = isAlreadyApplied,
                        onAdd = { onAddEffect(effectType) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ActiveEffectCard(
    effect: VideoEffectItem,
    onRemove: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onUpdate: (Float, Float) -> Unit
) {
    var intensity by remember(effect.intensity) { mutableFloatStateOf(effect.intensity) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A33)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38315E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = effect.type.displayName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF2A2347)
                    ) {
                        Text(
                            text = effect.type.category,
                            color = AccentCyan,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = effect.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentCyan,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2C2840)
                        ),
                        modifier = Modifier.size(width = 36.dp, height = 24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp).testTag("remove_effect_${effect.id}")
                    ) {
                        Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Intensity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intensity: ${(intensity * 100).toInt()}%",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(100.dp)
                )
                Slider(
                    value = intensity,
                    onValueChange = {
                        intensity = it
                        onUpdate(it, effect.secondaryParam)
                    },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentCyan,
                        inactiveTrackColor = Color(0xFF38315E)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EffectCatalogItem(
    effectType: VideoEffectType,
    isApplied: Boolean,
    onAdd: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = StudioCardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isApplied) { onAdd() }
            .testTag("catalog_effect_${effectType.name}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(getEffectBrush(effectType)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(getEffectIcon(effectType), effectType.displayName, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = effectType.displayName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = effectType.description,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onAdd,
                enabled = !isApplied,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApplied) Color(0xFF2C2840) else AccentCyan,
                    contentColor = if (isApplied) Color.Gray else Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                if (isApplied) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, "Applied", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, "Add", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getEffectIcon(type: VideoEffectType): ImageVector {
    return when (type) {
        VideoEffectType.COLOR_SHIFT -> Icons.Default.Palette
        VideoEffectType.NEGATIVE -> Icons.Default.InvertColors
        VideoEffectType.PIXELATE -> Icons.Default.Tv
        VideoEffectType.VIGNETTE -> Icons.Default.Lens
        VideoEffectType.LETTERBOX_BARS -> Icons.Default.Tv
        VideoEffectType.WAVE_DISTORT -> Icons.Default.Waves
        VideoEffectType.BLUR_BOX -> Icons.Default.Lens
        VideoEffectType.SEPIA_GRAIN -> Icons.Default.AutoAwesome
    }
}

fun getEffectBrush(type: VideoEffectType): Brush {
    return when (type) {
        VideoEffectType.COLOR_SHIFT -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF00E5FF)))
        VideoEffectType.NEGATIVE -> Brush.linearGradient(listOf(Color(0xFF000000), Color(0xFFFFFFFF)))
        VideoEffectType.PIXELATE -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
        VideoEffectType.VIGNETTE -> Brush.radialGradient(listOf(Color(0xFF475569), Color(0xFF0F172A)))
        VideoEffectType.LETTERBOX_BARS -> Brush.verticalGradient(listOf(Color.Black, Color(0xFF334155), Color.Black))
        VideoEffectType.WAVE_DISTORT -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF3B82F6)))
        VideoEffectType.BLUR_BOX -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFFA855F7)))
        VideoEffectType.SEPIA_GRAIN -> Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFF78350F)))
    }
}
