package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import kotlin.math.roundToInt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.LinearProgressIndicator
import com.example.data.model.AspectRatio
import com.example.data.model.AudioCategory
import com.example.data.model.AudioTrackItem
import com.example.data.model.CanvasBgMode
import com.example.data.model.ImportProgressState
import com.example.data.model.SampleAudio
import com.example.data.model.SampleMediaRepository
import com.example.data.model.SampleVideo
import com.example.data.model.TextStylePreset
import com.example.data.model.TransitionType
import com.example.data.model.VideoClip
import com.example.data.model.VideoFilter
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackAudioColor
import com.example.viewmodel.VideoEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerSheet(
    onDismiss: () -> Unit,
    onSelectSample: (SampleVideo) -> Unit,
    onImportDeviceVideo: (uriString: String, name: String) -> Unit,
    onGenerateTestVideo: (() -> Unit)? = null,
    onImportMultipleVideos: ((List<Uri>) -> Unit)? = null,
    onImportImageOverlay: ((Uri, String) -> Unit)? = null,
    importProgress: ImportProgressState = ImportProgressState(),
    onCancelImport: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = androidx.compose.ui.platform.LocalContext.current

    // Android zero-permission multi-video picker (Gallery)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            if (onImportMultipleVideos != null) {
                onImportMultipleVideos(uris)
            } else {
                val first = uris.first()
                val fileName = com.example.util.DeviceMediaImporter.resolveFileName(context, first).ifBlank {
                    first.lastPathSegment?.split("/")?.lastOrNull() ?: "Device Video"
                }
                onImportDeviceVideo(first.toString(), fileName)
            }
            onDismiss()
        }
    }

    // Android Photo / Image Overlay picker (Gallery)
    val imageOverlayPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val fileName = com.example.util.DeviceMediaImporter.resolveFileName(context, uri).ifBlank { "Image Overlay" }
            onImportImageOverlay?.invoke(uri, fileName)
            onDismiss()
        }
    }

    // Android System Storage / Files / Downloads picker (SAF)
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            if (onImportMultipleVideos != null) {
                onImportMultipleVideos(listOf(uri))
            } else {
                val fileName = com.example.util.DeviceMediaImporter.resolveFileName(context, uri).ifBlank {
                    uri.lastPathSegment?.split("/")?.lastOrNull() ?: "Imported Video"
                }
                onImportDeviceVideo(uri.toString(), fileName)
            }
            onDismiss()
        }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Import Media & Clips",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            if (importProgress.isImporting) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0E1B2E),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = importProgress.stageLabel.ifBlank { "Importing ${importProgress.currentFileName}..." },
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onCancelImport) {
                                Text("Cancel", color = Color(0xFFEF4444), fontSize = 11.sp)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { importProgress.progress.coerceIn(0.05f, 1f) },
                            color = AccentCyan,
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (importProgress.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF450A0A),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = importProgress.errorMessage!!,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            var showSampleClips by remember { mutableStateOf(false) }

            // Option 1: Multi-Video Gallery (Photo Picker)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioCardDark,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                    .testTag("import_device_video_button")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AccentCyan.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VideoLibrary, "Device Video", tint = AccentCyan, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import from Device Gallery",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select any MP4/video from your camera roll or albums",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Icon(Icons.Default.Add, contentDescription = "Import", tint = AccentCyan)
                }
            }

            // Option 2: Files & Downloads (SAF)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioCardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        getContentLauncher.launch("video/*")
                    }
                    .testTag("import_device_files_button")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Movie, "Files", tint = Color(0xFF818CF8), modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Browse Device Files & Downloads",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Open system file manager to pick downloaded videos or SD card files",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Icon(Icons.Default.Add, contentDescription = "Browse", tint = Color(0xFF818CF8))
                }
            }

            // Option 2b: Add Photo / Image Overlay (PIP)
            if (onImportImageOverlay != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioCardDark,
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            imageOverlayPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("import_image_overlay_button")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0EA5E9).copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, "Image Overlay", tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Add Photo / Graphic Overlay (PIP)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Overlay PNG/JPG photos or channel logos onto timeline",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Icon(Icons.Default.Add, contentDescription = "Add Photo", tint = Color(0xFF38BDF8))
                    }
                }
            }

            // Option 3: Generate Camera Video (instant on-device MP4)
            if (onGenerateTestVideo != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            onGenerateTestVideo()
                            onDismiss()
                        }
                        .testTag("import_camera_sample_button")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, "Camera Test", tint = Color(0xFF34D399), modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Quick Test: Load Device Camera Footage",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Encodes a real 1080p MP4 file in device storage for instant testing",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF34D399))
                    }
                }
            }

            // Optional Demo Footage Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSampleClips = !showSampleClips }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showSampleClips) "Hide Demo Clips" else "Need test footage? Show sample demo clips",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = if (showSampleClips) "▲" else "▼",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            if (showSampleClips) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SampleMediaRepository.sampleVideos) { sample ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSample(sample)
                                onDismiss()
                            }
                            .testTag("sample_video_${sample.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(sample.colorGradientHex.first),
                                                Color(sample.colorGradientHex.second)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Movie, "Video", tint = Color.White.copy(alpha = 0.8f))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sample.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = sample.description,
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${VideoEditorViewModel.formatTime(sample.durationMs)} • ${sample.category}",
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Icon(Icons.Default.Add, "Add", tint = AccentCyan)
                        }
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPickerSheet(
    onDismiss: () -> Unit,
    onSelectAudio: (SampleAudio) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Soundtrack & Audio",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SampleMediaRepository.sampleAudioTracks) { audio ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D352B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectAudio(audio)
                                onDismiss()
                            }
                            .testTag("sample_audio_${audio.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = TrackAudioColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Audiotrack, "Audio", tint = TrackAudioColor)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = audio.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${audio.artist} • ${audio.category.name}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${VideoEditorViewModel.formatTime(audio.durationMs)}${if (audio.tempoBpm > 0) " • ${audio.tempoBpm} BPM" else ""}",
                                    color = TrackAudioColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Icon(Icons.Default.Add, "Add Audio", tint = TrackAudioColor)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onSelectFilter: (VideoFilter) -> Unit,
    onUpdateGrading: (brightness: Float, contrast: Float, saturation: Float) -> Unit,
    allClipsCount: Int = 1,
    onUpdateFilterIntensity: (Float) -> Unit = {},
    onUpdateColorGradingSettings: (com.example.data.model.ColorGradingSettings) -> Unit = {},
    onApplyPresetToAllClips: (VideoFilter, com.example.data.model.LutPreset) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var brightness by remember(clip) { mutableFloatStateOf(clip.brightness) }
    var contrast by remember(clip) { mutableFloatStateOf(clip.contrast) }
    var saturation by remember(clip) { mutableFloatStateOf(clip.saturation) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters & Color Grading",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            CinematicFilterSelectionPanel(
                clip = clip,
                allClipsCount = allClipsCount,
                onSelectFilter = onSelectFilter,
                onUpdateFilterIntensity = onUpdateFilterIntensity,
                onUpdateColorGrading = onUpdateColorGradingSettings,
                onUpdateBasicGrading = { b, c, s ->
                    brightness = b
                    contrast = c
                    saturation = s
                    onUpdateGrading(b, c, s)
                },
                onApplyPresetToAllClips = onApplyPresetToAllClips,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Preset Filters horizontal row
            Text(
                text = "Preset Looks:",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(VideoFilter.entries.toTypedArray()) { filter ->
                    val isSelected = clip.filter == filter
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelectFilter(filter) }
                            .padding(4.dp)
                            .testTag("filter_${filter.name}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    when (filter) {
                                        VideoFilter.CINEMATIC -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF9100)))
                                        VideoFilter.CYBERPUNK -> Brush.linearGradient(listOf(Color(0xFFE040FB), Color(0xFF00E5FF)))
                                        VideoFilter.FILM_NOIR -> Brush.linearGradient(listOf(Color.White, Color.Black))
                                        VideoFilter.GOLDEN_HOUR -> Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6D00)))
                                        VideoFilter.EMERALD -> Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
                                        VideoFilter.VINTAGE_VHS -> Brush.linearGradient(listOf(Color(0xFFFF4081), Color(0xFF7C4DFF)))
                                        VideoFilter.SEPIA -> Brush.linearGradient(listOf(Color(0xFFD7CCC8), Color(0xFF8D6E63)))
                                        else -> Brush.linearGradient(listOf(Color(0xFF424242), Color(0xFF616161)))
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) AccentCyan else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, "Selected", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(
                            text = filter.displayName,
                            color = if (isSelected) AccentCyan else Color.LightGray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sliders: Brightness, Contrast, Saturation
            Text(text = "Brightness: ${String.format("%.2f", brightness)}", color = Color.White, fontSize = 12.sp)
            Slider(
                value = brightness,
                onValueChange = {
                    brightness = it
                    onUpdateGrading(brightness, contrast, saturation)
                },
                valueRange = -0.4f..0.4f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Text(text = "Contrast: ${String.format("%.2f", contrast)}", color = Color.White, fontSize = 12.sp)
            Slider(
                value = contrast,
                onValueChange = {
                    contrast = it
                    onUpdateGrading(brightness, contrast, saturation)
                },
                valueRange = 0.5f..1.8f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Text(text = "Saturation: ${String.format("%.2f", saturation)}", color = Color.White, fontSize = 12.sp)
            Slider(
                value = saturation,
                onValueChange = {
                    saturation = it
                    onUpdateGrading(brightness, contrast, saturation)
                },
                valueRange = 0f..2.0f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onApplyTrim: (inMs: Long, outMs: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trimIn by remember(clip) { mutableLongStateOf(clip.trimInMs) }
    var trimOut by remember(clip) { mutableLongStateOf(clip.trimOutMs) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trim Clip: ${clip.name}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            // Duration display
            Text(
                text = "Trimmed Duration: ${VideoEditorViewModel.formatTime(trimOut - trimIn)} (Source: ${VideoEditorViewModel.formatTime(clip.sourceDurationMs)})",
                color = AccentCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Start In-point adjustment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Start In: ${VideoEditorViewModel.formatTime(trimIn)}", color = Color.White, fontSize = 13.sp)
                Row {
                    Button(
                        onClick = { trimIn = (trimIn - 200L).coerceAtLeast(0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark)
                    ) { Text("-0.2s", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { trimIn = (trimIn + 200L).coerceAtMost(trimOut - 300L) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark)
                    ) { Text("+0.2s", fontSize = 11.sp) }
                }
            }

            Slider(
                value = trimIn.toFloat(),
                onValueChange = { trimIn = it.toLong().coerceIn(0L, trimOut - 300L) },
                valueRange = 0f..clip.sourceDurationMs.toFloat(),
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // End Out-point adjustment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "End Out: ${VideoEditorViewModel.formatTime(trimOut)}", color = Color.White, fontSize = 13.sp)
                Row {
                    Button(
                        onClick = { trimOut = (trimOut - 200L).coerceAtLeast(trimIn + 300L) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark)
                    ) { Text("-0.2s", fontSize = 11.sp) }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { trimOut = (trimOut + 200L).coerceAtMost(clip.sourceDurationMs) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark)
                    ) { Text("+0.2s", fontSize = 11.sp) }
                }
            }

            Slider(
                value = trimOut.toFloat(),
                onValueChange = { trimOut = it.toLong().coerceIn(trimIn + 300L, clip.sourceDurationMs) },
                valueRange = 0f..clip.sourceDurationMs.toFloat(),
                colors = SliderDefaults.colors(thumbColor = AccentPink, activeTrackColor = AccentPink)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onApplyTrim(trimIn, trimOut)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier.fillMaxWidth().testTag("apply_trim_button")
            ) {
                Text("Apply Trim", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onSelectSpeed: (Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 4.0f)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Video Speed Control",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Text(
                text = "Current: ${clip.speed}x • Resulting Duration: ${VideoEditorViewModel.formatTime(clip.trimmedDurationMs)}",
                color = AccentCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                items(speeds) { s ->
                    val isSelected = clip.speed == s
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AccentCyan else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF383552)),
                        modifier = Modifier
                            .clickable {
                                onSelectSpeed(s)
                                onDismiss()
                            }
                            .testTag("speed_${s}x")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "${s}x",
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = when {
                                    s < 1.0f -> "Slow Mo"
                                    s == 1.0f -> "Normal"
                                    else -> "Fast"
                                },
                                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasBackgroundSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onSelectBgMode: (CanvasBgMode) -> Unit,
    onToggleVertical: ((Boolean) -> Unit)? = null,
    onSetBlurParams: ((radiusDp: Float, brightness: Float) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isVertical by remember(clip.isVertical) { mutableStateOf(clip.isVertical || clip.isVerticalVideo) }
    var blurRadius by remember(clip.canvasBlurRadiusDp) { mutableFloatStateOf(clip.canvasBlurRadiusDp) }
    var brightness by remember(clip.canvasBgBrightness) { mutableFloatStateOf(clip.canvasBgBrightness) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Canvas Background Style",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Text(
                text = "Controls how letterboxing / pillarboxing is filled when clip aspect ratio differs from project canvas.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Vertical Clip Orientation Switch
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = StudioCardDark,
                border = BorderStroke(1.dp, if (isVertical) AccentCyan else Color(0xFF2C2840)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isVertical = !isVertical
                        onToggleVertical?.invoke(isVertical)
                    }
                    .testTag("toggle_vertical_video_clip")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "9:16 Vertical Video Mode",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isVertical) "Vertical orientation active (Applies Gaussian blur background on horizontal canvas)" else "Standard landscape / native video orientation",
                            color = if (isVertical) AccentCyan else Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isVertical,
                        onCheckedChange = {
                            isVertical = it
                            onToggleVertical?.invoke(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = AccentCyan.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Background Mode Grid
            Text(
                text = "Background Fill Style",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(bottom = 12.dp)
            ) {
                items(CanvasBgMode.entries.toTypedArray()) { mode ->
                    val isSelected = clip.canvasBgMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C2840)),
                        modifier = Modifier
                            .clickable {
                                onSelectBgMode(mode)
                            }
                            .testTag("canvas_bg_${mode.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = mode.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (mode) {
                                    CanvasBgMode.BLUR -> "Gaussian blur replica"
                                    CanvasBgMode.BLACK -> "Clean minimal matte"
                                    CanvasBgMode.GRADIENT -> "Vibrant modern dusk"
                                    CanvasBgMode.DARK_VIOLET -> "Studio neon aesthetic"
                                },
                                color = if (isSelected) AccentCyan else Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Gaussian Blur Processor Parameter Sliders (When Blur Mode is selected)
            if (clip.canvasBgMode == CanvasBgMode.BLUR) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF161426),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gaussian Blur Radius",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${blurRadius.roundToInt()} dp",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = blurRadius,
                            onValueChange = {
                                blurRadius = it
                                onSetBlurParams?.invoke(blurRadius, brightness)
                            },
                            valueRange = 5f..60f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .testTag("canvas_blur_radius_slider")
                        )

                        // Quick blur presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10f to "Soft (10dp)", 25f to "Medium (25dp)", 45f to "Bokeh (45dp)", 60f to "Ultra (60dp)").forEach { (r, label) ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF221E38),
                                    modifier = Modifier.clickable {
                                        blurRadius = r
                                        onSetBlurParams?.invoke(blurRadius, brightness)
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.LightGray,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Background Dimming Scrim",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${((1f - brightness) * 100).roundToInt()}% Dimmed",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = brightness,
                            onValueChange = {
                                brightness = it
                                onSetBlurParams?.invoke(blurRadius, brightness)
                            },
                            valueRange = 0.2f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .testTag("canvas_blur_brightness_slider")
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyframeSheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onToggleKeyframe: (scale: Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scale by remember { mutableFloatStateOf(if (clip.hasKeyframe) clip.keyframeScale else 1.35f) }
    var enabled by remember { mutableStateOf(clip.hasKeyframe) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keyframe Zoom Punch-In",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Text(
                text = "Dynamic keyframing applies an automated push-in camera zoom effect across the clip.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enable Dynamic Zoom", color = Color.White, fontSize = 14.sp)
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        onToggleKeyframe(if (enabled) scale else 1.0f)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan, checkedTrackColor = Color(0xFF004D40))
                )
            }

            if (enabled) {
                Text(
                    text = "Target Zoom Scale: ${String.format("%.2f", scale)}x",
                    color = AccentCyan,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Slider(
                    value = scale,
                    onValueChange = {
                        scale = it
                        onToggleKeyframe(scale)
                    },
                    valueRange = 1.05f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorSheet(
    existingText: String = "",
    existingTextColor: Long = 0xFFFFFFFF,
    existingBgColor: Long = 0xAA000000,
    onDismiss: () -> Unit,
    onSaveText: (text: String, preset: TextStylePreset, textColor: Long, bgColor: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    var textInput by remember { mutableStateOf(existingText.ifBlank { "Cinematic Title" }) }
    var selectedPreset by remember { mutableStateOf(TextStylePreset.NEON_GLOW) }
    var textColorHex by remember { mutableLongStateOf(existingTextColor) }
    var bgColorHex by remember { mutableLongStateOf(existingBgColor) }
    var activeColorTab by remember { mutableIntStateOf(0) } // 0 = Text Color, 1 = Background Pill

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text Overlay & Styling",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            // Live Text Preview Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF090A0F),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232B3E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(bgColorHex)
                    ) {
                        Text(
                            text = textInput.ifBlank { "Sample Preview" },
                            color = Color(textColorHex),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Overlay Text") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("text_input_field")
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Style Preset:", color = Color.LightGray, fontSize = 12.sp)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                items(TextStylePreset.entries.toTypedArray()) { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF6200EA) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF383552)),
                        modifier = Modifier.clickable {
                            selectedPreset = preset
                            // Auto-adapt colors based on preset
                            when (preset) {
                                TextStylePreset.NEON_GLOW -> {
                                    textColorHex = 0xFF00E5FF
                                    bgColorHex = 0xAA0F172A
                                }
                                TextStylePreset.CINEMA_TITLE -> {
                                    textColorHex = 0xFFF59E0B
                                    bgColorHex = 0xDD000000
                                }
                                TextStylePreset.RETRO_VHS -> {
                                    textColorHex = 0xFFF43F5E
                                    bgColorHex = 0xCC1E1B4B
                                }
                                TextStylePreset.OUTLINE -> {
                                    textColorHex = 0xFFFFFFFF
                                    bgColorHex = 0x99374151
                                }
                                else -> {
                                    textColorHex = 0xFFFFFFFF
                                    bgColorHex = 0xAA000000
                                }
                            }
                        }
                    ) {
                        Text(
                            text = preset.styleName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color Target Selector: Text Color vs Background Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeColorTab == 0) AccentCyan else StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier.weight(1f).clickable { activeColorTab = 0 }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(textColorHex))
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Text Color",
                            color = if (activeColorTab == 0) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeColorTab == 1) AccentCyan else StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier.weight(1f).clickable { activeColorTab = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(bgColorHex))
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Background Pill",
                            color = if (activeColorTab == 1) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Integrated Color Picker Component
            ColorPickerComponent(
                selectedColorHex = if (activeColorTab == 0) textColorHex else bgColorHex,
                onColorChanged = { newColor ->
                    if (activeColorTab == 0) {
                        textColorHex = newColor
                    } else {
                        bgColorHex = newColor
                    }
                },
                title = if (activeColorTab == 0) "Text Color & Custom Hex" else "Background Pill & Opacity",
                showAlphaSlider = (activeColorTab == 1)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSaveText(textInput, selectedPreset, textColorHex, bgColorHex)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier.fillMaxWidth().testTag("save_text_button")
            ) {
                Text("Add to Timeline", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class AnimatedBadgePreset(
    val type: String,
    val title: String,
    val subtitle: String,
    val iconOrEmoji: String,
    val colorHex: Long
)

val animatedBadgePresets = listOf(
    AnimatedBadgePreset("SUBSCRIBE_BADGE", "Subscribe Pill", "Dynamic Bell & Button", "🔔 SUBSCRIBE", 0xFFEF4444),
    AnimatedBadgePreset("LIVE_STREAM", "Live Pulse", "Broadcasting Status", "🔴 LIVE", 0xFFFF0055),
    AnimatedBadgePreset("EQUALIZER_BEAT", "Beat Waveform", "Audio reactive bars", "📊 AUDIO BEAT", 0xFFF59E0B),
    AnimatedBadgePreset("NEON_STAR", "Neon Star", "Rotating Sparkle", "⭐ STAR", 0xFF8B5CF6),
    AnimatedBadgePreset("WATERMARK_BADGE", "4K Ultra Badge", "Corner Watermark", "⚡ 4K PRO", 0xFF06B6D4),
    AnimatedBadgePreset("LIKE_PULSE", "Pulsing Heart", "Social Engagement", "❤️ LIKE", 0xFFEC4899)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPickerSheet(
    onDismiss: () -> Unit,
    onSelectSticker: (emoji: String, label: String) -> Unit,
    onSelectAnimatedSticker: (animationType: String, label: String, iconOrBadge: String) -> Unit = { _, l, i -> onSelectSticker(i, l) }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Animated Overlays, 1 = Emojis

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stickers & Animated Overlays",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lottie-style vector animations & badges",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Switcher: Animated Badges vs Emojis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B2332), RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedTab == 0) AccentCyan else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "✨ Animated Badges",
                        color = if (selectedTab == 0) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (selectedTab == 1) AccentCyan else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "🎨 Emoji Stickers",
                        color = if (selectedTab == 1) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Animated Badges List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(animatedBadgePresets) { badge ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StudioCardDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(badge.colorHex).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectAnimatedSticker(badge.type, badge.title, badge.iconOrEmoji)
                                    onDismiss()
                                }
                                .testTag("animated_badge_${badge.type}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(badge.colorHex).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = badge.iconOrEmoji.take(2), fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = badge.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = badge.subtitle, color = Color.Gray, fontSize = 10.sp)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(badge.colorHex),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "Add Loop",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(bottom = 12.dp)
                ) {
                    items(SampleMediaRepository.sampleStickers) { sticker ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioCardDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                            modifier = Modifier
                                .clickable {
                                    onSelectSticker(sticker.emoji, sticker.name)
                                    onDismiss()
                                }
                                .testTag("sticker_${sticker.name}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(text = sticker.emoji, fontSize = 28.sp)
                                Text(text = sticker.name, color = Color.Gray, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioMixerSheet(
    clipVolume: Float,
    isClipMuted: Boolean,
    onDismiss: () -> Unit,
    onUpdateVolume: (volume: Float, isMuted: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var volume by remember { mutableFloatStateOf(clipVolume) }
    var isMuted by remember { mutableStateOf(isClipMuted) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Audio Volume & Mixing",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Mute Track Audio", color = Color.White, fontSize = 14.sp)
                Switch(
                    checked = isMuted,
                    onCheckedChange = {
                        isMuted = it
                        onUpdateVolume(volume, isMuted)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = AccentCyan, checkedTrackColor = Color(0xFF004D40))
                )
            }

            Text(
                text = "Volume Level: ${(volume * 100).toInt()}%",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Slider(
                value = volume,
                onValueChange = {
                    volume = it
                    onUpdateVolume(volume, isMuted)
                },
                valueRange = 0f..2.0f,
                enabled = !isMuted,
                colors = SliderDefaults.colors(thumbColor = TrackAudioColor, activeTrackColor = TrackAudioColor)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromaKeySheet(
    clip: VideoClip,
    onDismiss: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onUpdateChromaKey: (colorHex: Long, tolerance: Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEnabled by remember { mutableStateOf(clip.isChromaKeyEnabled) }
    var selectedColorHex by remember { mutableLongStateOf(clip.chromaKeyColorHex) }
    var tolerance by remember { mutableFloatStateOf(clip.chromaKeyTolerance) }

    val keyColorPresets = listOf(
        Pair("Green Screen", 0xFF00FF00),
        Pair("Blue Screen", 0xFF0055FF),
        Pair("Magenta", 0xFFFF00FF),
        Pair("Studio Cyan", 0xFF00E5FF)
    )

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chroma Key / Green Screen",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time background removal & color keying",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enable Chroma Key Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StudioCardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Enable Chroma Keying",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isEnabled) "Active: Key color removed in real-time" else "Inactive: Original video background",
                            color = if (isEnabled) AccentCyan else Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = {
                            isEnabled = it
                            onToggleEnabled(it)
                            if (it) onUpdateChromaKey(selectedColorHex, tolerance)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCyan,
                            checkedTrackColor = Color(0xFF004D40)
                        ),
                        modifier = Modifier.testTag("chroma_key_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Key Color to Remove",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Color Presets Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keyColorPresets.forEach { (name, hex) ->
                    val isSelected = selectedColorHex == hex
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AccentCyan else Color(0xFF2E2A42)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = isEnabled) {
                                selectedColorHex = hex
                                onUpdateChromaKey(hex, tolerance)
                            }
                            .testTag("chroma_color_${name.lowercase().replace(" ", "_")}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(hex), CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = name,
                                color = if (isEnabled) Color.White else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tolerance Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Keying Tolerance", color = Color.White, fontSize = 13.sp)
                Text(
                    text = "${(tolerance * 100).toInt()}%",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = tolerance,
                onValueChange = {
                    tolerance = it
                    if (isEnabled) {
                        onUpdateChromaKey(selectedColorHex, tolerance)
                    }
                },
                valueRange = 0.1f..0.9f,
                enabled = isEnabled,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                modifier = Modifier.testTag("chroma_tolerance_slider")
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
