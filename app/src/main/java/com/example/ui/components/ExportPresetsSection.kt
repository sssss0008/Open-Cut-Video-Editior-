package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.ExportPresetEntity
import com.example.data.model.AspectRatio
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportResolution
import com.example.data.model.ExportVideoCodec
import com.example.data.model.SocialPlatform
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark

@Composable
fun ExportPresetsSection(
    presets: List<ExportPresetEntity>,
    activePresetId: String?,
    currentAspectRatio: AspectRatio,
    currentResolution: ExportResolution,
    currentFps: Int,
    currentBitrate: ExportBitrate,
    currentCodec: ExportVideoCodec,
    onApplyPreset: (ExportPresetEntity) -> Unit,
    onSaveCustomPreset: (
        name: String,
        platform: SocialPlatform,
        aspectRatio: AspectRatio,
        resolution: ExportResolution,
        fps: Int,
        bitrate: ExportBitrate,
        codec: ExportVideoCodec,
        notes: String,
        existingId: String?
    ) -> Unit,
    onDeletePreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<ExportPresetEntity?>(null) }

    val customCount = remember(presets) { presets.count { !it.isBuiltIn } }

    val filteredPresets = remember(presets, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> presets
            "CUSTOM" -> presets.filter { !it.isBuiltIn }
            else -> presets.filter { it.platformName == selectedFilter }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = StudioCardDark,
        border = BorderStroke(1.dp, Color(0xFF2C2840)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("export_presets_section")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AccentCyan.copy(alpha = 0.16f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdded,
                                contentDescription = "Social Export Presets",
                                tint = AccentCyan,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Social Export Presets",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${presets.size} presets ($customCount custom saved)",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentCyan.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable {
                            editingPreset = null
                            showCreateDialog = true
                        }
                        .testTag("save_custom_export_preset_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save Custom Preset",
                            tint = AccentCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save Preset",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Platform Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetFilterChip(
                    label = "All (${presets.size})",
                    selected = selectedFilter == "ALL",
                    accent = AccentCyan,
                    onClick = { selectedFilter = "ALL" },
                    tag = "preset_filter_all"
                )
                PresetFilterChip(
                    label = "My Custom ($customCount)",
                    selected = selectedFilter == "CUSTOM",
                    accent = AccentEmerald,
                    onClick = { selectedFilter = "CUSTOM" },
                    tag = "preset_filter_custom"
                )
                SocialPlatform.entries.filter { platform ->
                    presets.any { it.platform == platform }
                }.forEach { platform ->
                    PresetFilterChip(
                        label = platform.displayName,
                        selected = selectedFilter == platform.name,
                        accent = Color(platform.accentColorHex),
                        onClick = { selectedFilter = platform.name },
                        tag = "preset_filter_${platform.name.lowercase()}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredPresets.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF141222),
                    border = BorderStroke(1.dp, Color(0xFF28243E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No custom presets saved yet",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Save your current resolution, FPS, aspect ratio & bitrate for 1-tap reuse.",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        TextButton(
                            onClick = {
                                editingPreset = null
                                showCreateDialog = true
                            },
                            modifier = Modifier.testTag("empty_save_preset_cta")
                        ) {
                            Text("+ Create", color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(filteredPresets, key = { it.id }) { preset ->
                        val isActive = preset.id == activePresetId
                        val platformAccent = Color(preset.platform.accentColorHex)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isActive) Color(0xFF261C45) else Color(0xFF141222),
                            border = BorderStroke(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) platformAccent else Color(0xFF2A2642)
                            ),
                            modifier = Modifier
                                .width(210.dp)
                                .clickable { onApplyPreset(preset) }
                                .testTag("export_preset_card_${preset.id}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = platformAccent.copy(alpha = 0.22f)
                                        ) {
                                            Text(
                                                text = preset.platform.shortBadge,
                                                color = platformAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (!preset.isBuiltIn) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = AccentEmerald.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "CUSTOM",
                                                    color = AccentEmerald,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active Preset",
                                            tint = AccentEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = preset.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = preset.platform.displayName,
                                    color = platformAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = preset.summarySpec(),
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (preset.notes.isNotBlank()) {
                                    Text(
                                        text = preset.notes,
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 3.dp)
                                    )
                                }

                                if (!preset.isBuiltIn) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingPreset = preset
                                                showCreateDialog = true
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("edit_preset_${preset.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Preset",
                                                tint = AccentCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeletePreset(preset.id) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("delete_preset_${preset.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Preset",
                                                tint = AccentPink,
                                                modifier = Modifier.size(14.dp)
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
    }

    if (showCreateDialog) {
        SaveExportPresetDialog(
            initialPreset = editingPreset,
            defaultAspectRatio = currentAspectRatio,
            defaultResolution = currentResolution,
            defaultFps = currentFps,
            defaultBitrate = currentBitrate,
            defaultCodec = currentCodec,
            onDismiss = {
                showCreateDialog = false
                editingPreset = null
            },
            onSave = { name, platform, aspect, resolution, fps, bitrate, codec, notes ->
                onSaveCustomPreset(
                    name,
                    platform,
                    aspect,
                    resolution,
                    fps,
                    bitrate,
                    codec,
                    notes,
                    editingPreset?.id
                )
                showCreateDialog = false
                editingPreset = null
            }
        )
    }
}

@Composable
private fun PresetFilterChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) accent.copy(alpha = 0.22f) else Color(0xFF141222),
        border = BorderStroke(1.dp, if (selected) accent else Color(0xFF2B2742)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Text(
            text = label,
            color = if (selected) accent else Color.LightGray,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SaveExportPresetDialog(
    initialPreset: ExportPresetEntity?,
    defaultAspectRatio: AspectRatio,
    defaultResolution: ExportResolution,
    defaultFps: Int,
    defaultBitrate: ExportBitrate,
    defaultCodec: ExportVideoCodec,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        platform: SocialPlatform,
        aspectRatio: AspectRatio,
        resolution: ExportResolution,
        fps: Int,
        bitrate: ExportBitrate,
        codec: ExportVideoCodec,
        notes: String
    ) -> Unit
) {
    var name by remember(initialPreset) {
        mutableStateOf(initialPreset?.name ?: "")
    }
    var platform by remember(initialPreset) {
        mutableStateOf(initialPreset?.platform ?: SocialPlatform.YOUTUBE)
    }
    var aspectRatio by remember(initialPreset) {
        mutableStateOf(initialPreset?.aspectRatio ?: defaultAspectRatio)
    }
    var resolution by remember(initialPreset) {
        mutableStateOf(initialPreset?.resolution ?: defaultResolution)
    }
    var fps by remember(initialPreset) {
        mutableIntStateOf(initialPreset?.fps ?: defaultFps)
    }
    var bitrate by remember(initialPreset) {
        mutableStateOf(initialPreset?.bitrate ?: defaultBitrate)
    }
    var codec by remember(initialPreset) {
        mutableStateOf(initialPreset?.videoCodec ?: defaultCodec)
    }
    var notes by remember(initialPreset) {
        mutableStateOf(initialPreset?.notes ?: "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = StudioSurfaceDark,
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_export_preset_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure Preset",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (initialPreset == null) "Save Custom Export Preset" else "Edit Export Preset",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name", color = Color.LightGray, fontSize = 12.sp) },
                    placeholder = { Text("e.g., My 4K YouTube Vlog", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preset_name_input")
                )

                // Social Platform Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Social Platform", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    TextButton(
                        onClick = {
                            aspectRatio = platform.recommendedAspectRatio
                            resolution = platform.recommendedResolution
                            fps = platform.recommendedFps
                            bitrate = platform.recommendedBitrate
                            if (name.isBlank()) {
                                name = "${platform.displayName} Custom"
                            }
                        },
                        modifier = Modifier.testTag("apply_platform_recommended_button")
                    ) {
                        Text("Apply Recommended", color = AccentCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SocialPlatform.entries.forEach { p ->
                        val isSel = platform == p
                        val accent = Color(p.accentColorHex)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) accent.copy(alpha = 0.25f) else StudioCardDark,
                            border = BorderStroke(1.dp, if (isSel) accent else Color(0xFF2C2840)),
                            modifier = Modifier
                                .clickable { platform = p }
                                .testTag("dialog_platform_${p.name.lowercase()}")
                        ) {
                            Text(
                                text = p.displayName,
                                color = if (isSel) accent else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // Aspect Ratio
                Text("Aspect Ratio", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AspectRatio.entries.forEach { ar ->
                        val isSel = aspectRatio == ar
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AccentCyan.copy(alpha = 0.2f) else StudioCardDark,
                            border = BorderStroke(1.dp, if (isSel) AccentCyan else Color(0xFF2C2840)),
                            modifier = Modifier.clickable { aspectRatio = ar }
                        ) {
                            Text(
                                text = ar.label.split(" ").first(),
                                color = if (isSel) AccentCyan else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Resolution
                Text("Resolution", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportResolution.entries.forEach { res ->
                        val isSel = resolution == res
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF381E72) else StudioCardDark,
                            border = BorderStroke(1.dp, if (isSel) AccentCyan else Color(0xFF2C2840)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { resolution = res }
                        ) {
                            Text(
                                text = res.label.split(" ").first(),
                                color = if (isSel) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                // FPS & Codec
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(24, 30, 60).forEach { f ->
                        val isSel = fps == f
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AccentCyan else StudioCardDark,
                            border = BorderStroke(1.dp, if (isSel) AccentCyan else Color(0xFF2C2840)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { fps = f }
                        ) {
                            Text(
                                text = "$f FPS",
                                color = if (isSel) Color.Black else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                // Bitrate
                Text("Bitrate", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportBitrate.entries.forEach { br ->
                        val isSel = bitrate == br
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF381E72) else StudioCardDark,
                            border = BorderStroke(1.dp, if (isSel) AccentCyan else Color(0xFF2C2840)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { bitrate = br }
                        ) {
                            Text(
                                text = "${br.mbps}M",
                                color = if (isSel) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                // Codec
                Text("Video Codec", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportVideoCodec.entries.forEach { c ->
                        val isSel = codec == c
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AccentEmerald.copy(alpha = 0.2f) else StudioCardDark,
                            border = BorderStroke(1.dp, if (isSel) AccentEmerald else Color(0xFF2C2840)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { codec = c }
                        ) {
                            Text(
                                text = c.label,
                                color = if (isSel) AccentEmerald else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)", color = Color.LightGray, fontSize = 12.sp) },
                    placeholder = { Text("e.g., Best for daily Reels", color = Color.Gray, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preset_notes_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = Color.LightGray)
                    }
                    Button(
                        onClick = {
                            onSave(name, platform, aspectRatio, resolution, fps, bitrate, codec, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("confirm_save_preset_button")
                    ) {
                        Text("Save Preset", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
