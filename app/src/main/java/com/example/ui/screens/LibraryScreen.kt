package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ImportedMediaEntity
import com.example.data.model.AudioCategory
import com.example.data.model.ImportProgressState
import com.example.data.model.SampleAudio
import com.example.data.model.SampleMediaRepository
import com.example.data.model.SampleVideo
import com.example.data.model.TextStylePreset
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackAudioColor
import com.example.util.DeviceMediaImporter
import com.example.viewmodel.VideoEditorViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    importedMedia: List<ImportedMediaEntity> = emptyList(),
    importProgress: ImportProgressState = ImportProgressState(),
    onImportMedia: (uri: String, name: String, type: String) -> Unit = { _, _, _ -> },
    onImportMultipleVideos: ((List<Uri>) -> Unit)? = null,
    onCancelImport: () -> Unit = {},
    onClearImportError: () -> Unit = {},
    onDeleteImportedMedia: (String) -> Unit = {},
    onAddImportedToTimeline: (ImportedMediaEntity) -> Unit = {},
    onAddSampleClip: (SampleVideo) -> Unit,
    onAddAudio: (SampleAudio) -> Unit,
    onAddSticker: (emoji: String, label: String) -> Unit,
    onAddTextTitle: (text: String, preset: TextStylePreset) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var previewingAudioName by remember { mutableStateOf<String?>(null) }
    val tabTitles = listOf("Imported (${importedMedia.size})", "B-Roll Video", "Soundtracks", "Sound FX", "Stickers & Titles")

    // Android 100% offline local file pickers
    // 1. Multi-Video Picker (Zero-permission Android Photo Picker)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            if (onImportMultipleVideos != null) {
                onImportMultipleVideos(uris)
            } else {
                uris.forEach { uri ->
                    val fileName = DeviceMediaImporter.resolveFileName(context, uri)
                    onImportMedia(uri.toString(), fileName, "VIDEO")
                }
            }
        }
    }

    // 2. Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val fileName = DeviceMediaImporter.resolveFileName(context, uri)
            onImportMedia(uri.toString(), fileName, "IMAGE")
        }
    }

    // 3. Audio File Picker (MP3, WAV, AAC, M4A, OGG)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val fileName = DeviceMediaImporter.resolveFileName(context, uri)
            onImportMedia(uri.toString(), fileName, "AUDIO")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Media Assets Library", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("100% Offline device media & cinematic assets", color = AccentCyan, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("library_drawer_button")) {
                        Icon(Icons.Default.Menu, "Open Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioSurfaceDark)
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Local Device Storage Import Quick Action Strip
            Surface(
                color = Color(0xFF131728),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232B42)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderOpen, "Storage", tint = AccentCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import from Device Storage",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Zero Cloud",
                            color = AccentEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pick Video
                        Button(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("pick_device_video_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Videocam, "Video", tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Video", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Pick Image / Graphic
                        Button(
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF831843)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("pick_device_image_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Image, "Image", tint = Color(0xFFF472B6), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Photo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Pick Audio File
                        Button(
                            onClick = {
                                audioPickerLauncher.launch("audio/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("pick_device_audio_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Audiotrack, "Audio", tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Audio", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (importProgress.isImporting) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("library_import_progress_banner")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Importing (${importProgress.currentIndex}/${importProgress.totalCount.coerceAtLeast(1)}): ${importProgress.currentFileName}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = importProgress.stageLabel.ifBlank { "Copying & validating..." },
                                    color = AccentCyan,
                                    fontSize = 10.sp
                                )
                            }
                            TextButton(
                                onClick = onCancelImport,
                                modifier = Modifier.testTag("library_cancel_import_button")
                            ) {
                                Text("Cancel", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        LinearProgressIndicator(
                            progress = { importProgress.progress.coerceIn(0f, 1f) },
                            color = AccentCyan,
                            trackColor = Color(0xFF1F2937),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            } else if (!importProgress.errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF450A0A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("library_import_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = "Import Error", tint = Color(0xFFFCA5A5), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = importProgress.errorMessage,
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearImportError, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss Error", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Category Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = StudioSurfaceDark,
                contentColor = AccentCyan,
                indicator = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) AccentCyan else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // 1. Device Imported Media Assets
                    if (importedMedia.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = StudioCardDark,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.FolderOpen, "Empty Storage", tint = AccentCyan, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Device Media Imported Yet",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Use the buttons above to select videos, photos, and audio files directly from your phone storage to edit on the multi-track timeline.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(importedMedia, key = { it.id }) { item ->
                                val thumbBitmap = remember(item.thumbnailPath) {
                                    if (item.thumbnailPath.isNotBlank() && File(item.thumbnailPath).exists()) {
                                        runCatching { BitmapFactory.decodeFile(item.thumbnailPath)?.asImageBitmap() }.getOrNull()
                                    } else null
                                }
                                val (typeColor, typeIcon, typeLabel) = when (item.mediaType.uppercase()) {
                                    "IMAGE" -> Triple(Color(0xFFEC4899), Icons.Default.Image, "PHOTO")
                                    "AUDIO" -> Triple(Color(0xFF10B981), Icons.Default.Audiotrack, "AUDIO")
                                    else -> Triple(Color(0xFF3B82F6), Icons.Default.Videocam, "VIDEO")
                                }
                                val metadataSummary = buildString {
                                    if (item.mediaType.equals("VIDEO", ignoreCase = true)) {
                                        if (item.width > 0 && item.height > 0) append("${item.width}x${item.height} • ")
                                        if (item.durationMs > 0L) append("${VideoEditorViewModel.formatTime(item.durationMs)} • ")
                                        if (item.frameRate > 0f) append("${item.frameRate.toInt()}fps")
                                        if (item.videoCodec.isNotBlank()) append(" • ${item.videoCodec}")
                                    } else {
                                        if (item.durationMs > 0L) append("${VideoEditorViewModel.formatTime(item.durationMs)} • ")
                                        append(item.mimeType.ifBlank { typeLabel })
                                    }
                                }
                                val secondarySummary = buildString {
                                    if (item.audioInfo.isNotBlank()) append("${item.audioInfo} • ")
                                    if (item.sizeBytes > 0L) {
                                        val kb = (item.sizeBytes / 1024L).coerceAtLeast(1L)
                                        append(if (kb >= 1024L) String.format(Locale.US, "%.1f MB", kb / 1024f) else "$kb KB")
                                    } else {
                                        append("Added ${dateFormat.format(Date(item.addedAt))}")
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = StudioCardDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                                    modifier = Modifier.fillMaxWidth().testTag("imported_media_${item.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 68.dp, height = 52.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(typeColor.copy(alpha = 0.18f))
                                                .border(1.dp, typeColor, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (thumbBitmap != null) {
                                                Image(
                                                    bitmap = thumbBitmap,
                                                    contentDescription = item.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.7f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(3.dp)
                                                ) {
                                                    Text(
                                                        text = typeLabel,
                                                        color = typeColor,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                    )
                                                }
                                            } else {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(typeIcon, typeLabel, tint = typeColor, modifier = Modifier.size(20.dp))
                                                    Text(typeLabel, color = typeColor, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.name,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (item.isPlaybackVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Playback Verified",
                                                        tint = AccentEmerald,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            if (metadataSummary.isNotBlank()) {
                                                Text(
                                                    text = metadataSummary.trimEnd(' ', '•'),
                                                    color = AccentCyan,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = secondarySummary.trimEnd(' ', '•'),
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Action buttons: Add/Edit & Delete
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Button(
                                                onClick = { onAddImportedToTimeline(item) },
                                                colors = ButtonDefaults.buttonColors(containerColor = typeColor),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("add_imported_to_timeline_${item.id}")
                                            ) {
                                                Icon(Icons.Default.Add, "Add", tint = Color.Black, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (item.mediaType.equals("VIDEO", ignoreCase = true) && !item.isPlaybackVerified) "Verify" else "Add",
                                                    color = Color.Black,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            IconButton(
                                                onClick = { onDeleteImportedMedia(item.id) },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(60.dp)) }
                        }
                    }
                }
                1 -> {
                    // B-Roll Video Clips
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Curated Cinematic B-Roll Clips",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        items(SampleMediaRepository.sampleVideos) { sample ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioCardDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 80.dp, height = 56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(sample.colorGradientHex.first), Color(sample.colorGradientHex.second))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Movie, "Video", tint = Color.White.copy(alpha = 0.8f))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sample.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(sample.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${VideoEditorViewModel.formatTime(sample.durationMs)} • ${sample.category}",
                                            color = AccentCyan,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Button(
                                        onClick = { onAddSampleClip(sample) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("add_sample_${sample.name}")
                                    ) {
                                        Icon(Icons.Default.Add, "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }
                }
                2 -> {
                    // Soundtracks / Music
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Royalty-Free Background Music",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        items(SampleMediaRepository.sampleAudioTracks.filter { it.category == AudioCategory.MUSIC }) { audio ->
                            val isAuditioning = previewingAudioName == audio.name
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioCardDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D352B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            previewingAudioName = if (isAuditioning) null else audio.name
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isAuditioning) AccentEmerald else TrackAudioColor.copy(alpha = 0.2f))
                                    ) {
                                        Icon(
                                            imageVector = if (isAuditioning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Preview",
                                            tint = if (isAuditioning) Color.Black else TrackAudioColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(audio.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("${audio.artist} • ${audio.tempoBpm} BPM", color = Color.Gray, fontSize = 11.sp)
                                        Text(
                                            VideoEditorViewModel.formatTime(audio.durationMs),
                                            color = TrackAudioColor,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Button(
                                        onClick = { onAddAudio(audio) },
                                        colors = ButtonDefaults.buttonColors(containerColor = TrackAudioColor),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }
                }
                3 -> {
                    // Sound Effects (SFX)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Cinematic Sound Effects (SFX)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        items(SampleMediaRepository.sampleAudioTracks.filter { it.category == AudioCategory.SOUND_EFFECT }) { sfx ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StudioCardDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382C18)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFB300).copy(alpha = 0.2f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Audiotrack, "SFX", tint = Color(0xFFFFB300))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sfx.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Sound Effect • ${VideoEditorViewModel.formatTime(sfx.durationMs)}", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { onAddAudio(sfx) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }
                }
                4 -> {
                    // Stickers & Text Title Presets
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "Overlay Stickers",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                items(SampleMediaRepository.sampleStickers) { st ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StudioCardDark,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                                        modifier = Modifier.clickable { onAddSticker(st.emoji, st.name) }
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(st.emoji, fontSize = 24.sp)
                                            Text(st.name, color = Color.Gray, fontSize = 8.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Quick Text & Title Styles",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(TextStylePreset.entries.toTypedArray()) { preset ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StudioCardDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF381E72)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(preset.styleName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Pre-styled typography title", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { onAddTextTitle("OpenCut Title", preset) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Insert", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(70.dp)) }
                    }
                }
            }
        }
    }
}
