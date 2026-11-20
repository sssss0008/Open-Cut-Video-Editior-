package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.ProjectEntity
import com.example.data.model.AspectRatio
import com.example.data.model.ImportProgressState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.util.DeviceMediaImporter
import com.example.viewmodel.VideoEditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsDashboardScreen(
    projects: List<ProjectEntity>,
    onOpenProject: (String) -> Unit,
    onCreateProject: (title: String, aspect: AspectRatio, withSampleContent: Boolean) -> Unit,
    onDuplicateProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onRenameProject: (id: String, newTitle: String) -> Unit = { _, _ -> },
    onOpenDrawer: () -> Unit = {},
    onNavigateAccount: () -> Unit = {},
    onImportDeviceVideo: (uriString: String, name: String) -> Unit = { _, _ -> },
    onImportMultipleVideos: ((List<Uri>) -> Unit)? = null,
    importProgress: ImportProgressState = ImportProgressState(),
    onCancelImport: () -> Unit = {},
    onClearImportError: () -> Unit = {},
    onGenerateTestVideo: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("opencut_prefs", android.content.Context.MODE_PRIVATE)
    }
    var showNewProjectSheet by remember { mutableStateOf(false) }
    var showEducationGuideDialog by remember {
        mutableStateOf(!prefs.getBoolean("has_seen_education_guide", false))
    }
    var showImportSourceDialog by remember { mutableStateOf(false) }
    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectForVersionSnapshot by remember { mutableStateOf<ProjectEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("Recent") }
    var searchQuery by remember { mutableStateOf("") }
    var showOnboardingBanner by remember { mutableStateOf(true) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            if (onImportMultipleVideos != null) {
                onImportMultipleVideos(uris)
            } else {
                val first = uris.first()
                onImportDeviceVideo(first.toString(), DeviceMediaImporter.resolveFileName(context, first))
            }
        }
    }

    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            if (onImportMultipleVideos != null) {
                onImportMultipleVideos(listOf(uri))
            } else {
                val fileName = DeviceMediaImporter.resolveFileName(context, uri)
                onImportDeviceVideo(uri.toString(), fileName)
            }
        }
    }

    val filteredProjects = remember(projects, selectedFilter, selectedSort, searchQuery) {
        val byFilter = when (selectedFilter) {
            "9:16 Shorts" -> projects.filter { it.aspectRatioName.contains("9_16") }
            "16:9 YouTube" -> projects.filter { it.aspectRatioName.contains("16_9") }
            "In Progress" -> projects.filter { it.lastPlayheadPositionMs > 0L }
            else -> projects
        }
        val bySearch = if (searchQuery.isBlank()) {
            byFilter
        } else {
            byFilter.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
        }
        when (selectedSort) {
            "Title" -> bySearch.sortedBy { it.title.lowercase() }
            "Duration" -> bySearch.sortedByDescending { it.durationMs }
            else -> bySearch.sortedByDescending { it.updatedAt }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_icon),
                            contentDescription = "OpenCut Studio Icon",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .testTag("dashboard_app_logo")
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("OpenCut Studio", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Professional AI & Multi-Track NLE", color = AccentCyan, fontSize = 10.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("dashboard_drawer_button")) {
                        Icon(Icons.Default.Menu, "Open Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEducationGuideDialog = true },
                        modifier = Modifier.testTag("dashboard_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "App Education Guide",
                            tint = AccentCyan
                        )
                    }
                    IconButton(
                        onClick = { showImportSourceDialog = true },
                        modifier = Modifier.testTag("dashboard_import_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Import Video from Gallery",
                            tint = Color.White
                        )
                    }
                    Button(
                        onClick = { showNewProjectSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("topbar_new_project_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create New Project", tint = Color.Black, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = onNavigateAccount,
                        modifier = Modifier.testTag("dashboard_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account & Analytics",
                            tint = AccentCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioSurfaceDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectSheet = true },
                containerColor = AccentCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("new_project_fab")
            ) {
                Icon(Icons.Default.Add, "New Project")
            }
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (importProgress.isImporting) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_import_progress_banner")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
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
                                        text = importProgress.stageLabel.ifBlank { "Copying to project workspace..." },
                                        color = AccentCyan,
                                        fontSize = 10.sp
                                    )
                                }
                                TextButton(
                                    onClick = onCancelImport,
                                    modifier = Modifier.testTag("dashboard_cancel_import_button")
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
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            } else if (!importProgress.errorMessage.isNullOrBlank()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF450A0A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_import_error_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
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
            }

            if (showOnboardingBanner) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StudioSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_hero_banner")
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(145.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_onboarding_hero),
                                    contentDescription = "OpenCut Studio Onboarding Illustration",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.15f),
                                                    Color(0xE60B0F19)
                                                )
                                            )
                                        )
                                )
                                IconButton(
                                    onClick = { showOnboardingBanner = false },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                                        .testTag("dismiss_onboarding_banner_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss Onboarding",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AccentCyan
                                    ) {
                                        Text(
                                            text = "WELCOME TO OPENCUT STUDIO PRO",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cinema Color LUTs • Multi-Track Scrubbing • Cloud Analytics",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "No sign-in required to edit. Visit Account anytime for Cloud Sync & Analytics.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AccentCyan.copy(alpha = 0.18f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                                    modifier = Modifier
                                        .clickable { onNavigateAccount() }
                                        .testTag("onboarding_account_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Analytics,
                                            contentDescription = null,
                                            tint = AccentCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Analytics",
                                            color = AccentCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Video Projects Visual Thumbnail Strip (with "New Project" creation card + live filmstrip previews)
            item {
                RecentProjectsThumbnailGallery(
                    projects = projects,
                    onOpenProject = onOpenProject,
                    onCreateNewProject = { showNewProjectSheet = true }
                )
            }

            item {
                // Quick Start Presets Banner
                QuickStartBanner(
                    onCreateTemplate = { title, aspect ->
                        onCreateProject(title, aspect, false)
                    }
                )
            }

            item {
                // 1-Tap Device Video Import Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF151E32),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showImportSourceDialog = true }
                        .testTag("one_tap_ai_wizard_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🎬 Import Device Video & Start Editing",
                                color = AccentCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select any video from your phone gallery to trim, cut, duck audio, and export in 4K",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentCyan
                        ) {
                            Text(
                                text = "Import Video",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Projects (${projects.size})",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Sort order selector pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Recent", "Title", "Duration").forEach { sortKey ->
                                val isSel = selectedSort == sortKey
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) AccentPink.copy(alpha = 0.2f) else Color(0xFF171E2E),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSel) AccentPink else Color(0xFF283249)
                                    ),
                                    modifier = Modifier
                                        .clickable { selectedSort = sortKey }
                                        .testTag("project_sort_${sortKey.lowercase()}")
                                ) {
                                    Text(
                                        text = sortKey,
                                        color = if (isSel) AccentPink else Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search Projects", tint = AccentCyan, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = Color.LightGray, modifier = Modifier.size(15.dp))
                                }
                            }
                        },
                        placeholder = { Text("Search video projects or version tags...", color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = Color(0xFF2C2840),
                            focusedContainerColor = StudioCardDark,
                            unfocusedContainerColor = StudioCardDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("All", "9:16 Shorts", "16:9 YouTube", "In Progress")) { filter ->
                            val isSelected = selectedFilter == filter
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AccentCyan else StudioCardDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AccentCyan else Color(0xFF2C2840)
                                ),
                                modifier = Modifier
                                    .clickable { selectedFilter = filter }
                                    .testTag("project_filter_${filter.replace(" ", "_").replace(":", "_").lowercase()}")
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSelected) Color.Black else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredProjects.isEmpty()) {
                item {
                    EmptyProjectsState(
                        onImportClick = { showImportSourceDialog = true },
                        onCreateClick = { showNewProjectSheet = true },
                        onOpenGuide = { showEducationGuideDialog = true }
                    )
                }
            } else {
                items(filteredProjects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onOpenProject(project.id) },
                        onDuplicate = { onDuplicateProject(project.id) },
                        onDelete = { onDeleteProject(project.id) },
                        onRename = { projectToRename = project },
                        onSaveVersion = { projectForVersionSnapshot = project }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
            }
        }
    }

    if (showImportSourceDialog) {
        ImportSourceDialog(
            onDismiss = { showImportSourceDialog = false },
            onPickGallery = {
                videoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onBrowseFiles = {
                getContentLauncher.launch("video/*")
            },
            onGenerateTestVideo = onGenerateTestVideo
        )
    }

    if (showEducationGuideDialog) {
        AppEducationGuideDialog(
            onDismiss = {
                prefs.edit().putBoolean("has_seen_education_guide", true).apply()
                showEducationGuideDialog = false
            }
        )
    }

    if (showNewProjectSheet) {
        NewProjectBottomSheet(
            onDismiss = { showNewProjectSheet = false },
            onCreate = { title, aspect, withSamples ->
                onCreateProject(title, aspect, withSamples)
                showNewProjectSheet = false
            }
        )
    }

    if (projectToRename != null) {
        val proj = projectToRename!!
        RenameProjectDialog(
            currentTitle = proj.title,
            onDismiss = { projectToRename = null },
            onConfirm = { newTitle ->
                onRenameProject(proj.id, newTitle)
                projectToRename = null
            }
        )
    }

    if (projectForVersionSnapshot != null) {
        val proj = projectForVersionSnapshot!!
        SaveNamedVersionDialog(
            project = proj,
            onDismiss = { projectForVersionSnapshot = null },
            onSaveVersion = { versionTag ->
                val aspect = try {
                    AspectRatio.valueOf(proj.aspectRatioName)
                } catch (_: Exception) {
                    AspectRatio.VERTICAL_9_16
                }
                val baseTitle = proj.title.substringBefore(" [").trim()
                onCreateProject("$baseTitle [$versionTag]", aspect, true)
                projectForVersionSnapshot = null
            }
        )
    }
}

/**
 * Horizontal Visual Thumbnail Carousel of Recent Video Projects + "New Project" Tile.
 */
@Composable
fun RecentProjectsThumbnailGallery(
    projects: List<ProjectEntity>,
    onOpenProject: (String) -> Unit,
    onCreateNewProject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_projects_thumbnail_strip")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎬 Project Management & Visual Thumbnails",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap to resume or create new",
                color = AccentCyan,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. Create New Project Card
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131D31),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan),
                    modifier = Modifier
                        .width(128.dp)
                        .height(114.dp)
                        .clickable { onCreateNewProject() }
                        .testTag("create_new_project_thumbnail_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan.copy(alpha = 0.22f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Project",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "New Project",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "9:16 • 16:9 • 1:1",
                            color = AccentCyan,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // 2. Visual Thumbnail Cards for Recent Projects
            items(projects.take(8), key = { "thumb_${it.id}" }) { proj ->
                val aspectBadge = proj.aspectRatioName.split("_").getOrNull(1)?.replace("_", ":") ?: "16:9"
                val progress = if (proj.durationMs > 0L) {
                    (proj.lastPlayheadPositionMs.toFloat() / proj.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E364F)),
                    modifier = Modifier
                        .width(152.dp)
                        .height(114.dp)
                        .clickable { onOpenProject(proj.id) }
                        .testTag("recent_project_thumb_card_${proj.id}")
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Visual Filmstrip Preview Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                        ) {
                            ProjectFilmstripThumbnailCanvas(
                                gradientStartHex = proj.thumbnailGradientStart,
                                gradientEndHex = proj.thumbnailGradientEnd,
                                clipCount = proj.clipCount,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Aspect Ratio Badge (Top Left)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(5.dp)
                            ) {
                                Text(
                                    text = aspectBadge,
                                    color = AccentCyan,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            // Duration Badge (Bottom Right)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(5.dp)
                            ) {
                                Text(
                                    text = VideoEditorViewModel.formatTime(proj.durationMs),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            // Center Play Icon Overlay
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Open Project",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        if (progress > 0f) {
                            LinearProgressIndicator(
                                progress = { progress },
                                color = AccentEmerald,
                                trackColor = Color(0xFF1F2937),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = proj.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${proj.clipCount} clips • ${proj.resolutionName.removePrefix("RES_")}",
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas renderer that draws a realistic multi-frame video filmstrip + audio waveform preview
 * for project thumbnails.
 */
@Composable
fun ProjectFilmstripThumbnailCanvas(
    gradientStartHex: Long,
    gradientEndHex: Long,
    clipCount: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(gradientStartHex), Color(gradientEndHex))
            )
        )

        // Draw filmstrip frame dividers
        val frames = clipCount.coerceIn(2, 5)
        val frameWidth = size.width / frames
        for (i in 1 until frames) {
            val x = i * frameWidth
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 2f
            )
        }

        // Draw top & bottom film sprocket perforations
        val holeCount = 8
        val holeStep = size.width / holeCount
        for (i in 0 until holeCount) {
            val hx = i * holeStep + 4f
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(hx, 2f),
                size = Size(6f, 3.5f),
                cornerRadius = CornerRadius(1f, 1f)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.45f),
                topLeft = Offset(hx, size.height - 5.5f),
                size = Size(6f, 3.5f),
                cornerRadius = CornerRadius(1f, 1f)
            )
        }

        // Draw miniature audio waveform along lower third
        val barCount = 16
        val barStep = size.width / barCount
        for (i in 0 until barCount) {
            val bx = i * barStep + 3f
            val h = ((i * 7 + clipCount * 3) % 10 + 3).toFloat()
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(bx, size.height - 8f),
                end = Offset(bx, size.height - 8f - h),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun QuickStartBanner(
    onCreateTemplate: (title: String, aspect: AspectRatio) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1A33),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38315E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✨ Quick Start Templates",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose a canvas aspect ratio to begin cutting",
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    TemplateChip(
                        title = "TikTok / Reels",
                        aspect = "9:16",
                        gradient = listOf(Color(0xFFE040FB), Color(0xFF00E5FF)),
                        onClick = { onCreateTemplate("Shorts / Reel Cut", AspectRatio.VERTICAL_9_16) }
                    )
                }
                item {
                    TemplateChip(
                        title = "YouTube Video",
                        aspect = "16:9",
                        gradient = listOf(Color(0xFFFF5252), Color(0xFFFF7A00)),
                        onClick = { onCreateTemplate("YouTube Vlog Cut", AspectRatio.LANDSCAPE_16_9) }
                    )
                }
                item {
                    TemplateChip(
                        title = "Instagram Post",
                        aspect = "1:1",
                        gradient = listOf(Color(0xFF7C4DFF), Color(0xFFFF4081)),
                        onClick = { onCreateTemplate("Instagram Square Cut", AspectRatio.SQUARE_1_1) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateChip(
    title: String,
    aspect: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = StudioCardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("template_${title.replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(aspect, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Preloaded clips", color = Color.Gray, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit = {},
    onSaveVersion: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(project.updatedAt) { dateFormat.format(Date(project.updatedAt)) }
    val versionBadge = remember(project.title) {
        if (project.title.contains("[") && project.title.contains("]")) {
            project.title.substringAfter("[").substringBefore("]")
        } else null
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rich Multi-Frame Filmstrip Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 78.dp, height = 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                ProjectFilmstripThumbnailCanvas(
                    gradientStartHex = project.thumbnailGradientStart,
                    gradientEndHex = project.thumbnailGradientEnd,
                    clipCount = project.clipCount,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (versionBadge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AccentPink.copy(alpha = 0.22f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentPink)
                        ) {
                            Text(
                                text = versionBadge,
                                color = AccentPink,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF1E1B2E),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = project.aspectRatioName.split("_").getOrNull(1)?.replace("_", ":") ?: "16:9",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${project.clipCount} clips • ${VideoEditorViewModel.formatTime(project.durationMs)}",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edited $formattedDate",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    if (project.lastPlayheadPositionMs > 0L) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Resume: ${VideoEditorViewModel.formatTime(project.lastPlayheadPositionMs)}",
                                color = Color(0xFF34D399),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Quick actions: Save Named Version, Rename, Duplicate & Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSaveVersion,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("save_version_button_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = "Save Named Project Version",
                        tint = AccentPink,
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(
                    onClick = onRename,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("rename_project_button_${project.id}")
                ) {
                    Icon(Icons.Default.Edit, "Rename Project", tint = AccentCyan, modifier = Modifier.size(17.dp))
                }
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("duplicate_project_button_${project.id}")
                ) {
                    Icon(Icons.Default.ContentCopy, "Duplicate", tint = Color.LightGray, modifier = Modifier.size(17.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_project_button_${project.id}")
                ) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

/**
 * Dialog allowing users to create named project snapshots (First Cut, Rough Cut, Color Version, Final Version, Social Version).
 */
@Composable
fun SaveNamedVersionDialog(
    project: ProjectEntity,
    onDismiss: () -> Unit,
    onSaveVersion: (String) -> Unit
) {
    val presets = listOf("First Cut", "Rough Cut", "Color Version", "Final Version", "Social Version")
    var selectedTag by remember { mutableStateOf(presets.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save Named Project Version",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Create a branch snapshot of \"${project.title}\" so you can compare edits or experiment non-destructively:",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                presets.forEach { preset ->
                    val isSelected = selectedTag == preset
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AccentPink.copy(alpha = 0.22f) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentPink else Color(0xFF2E364F)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTag = preset }
                            .testTag("version_preset_${preset.replace(" ", "_").lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset,
                                color = if (isSelected) AccentPink else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSelected) "Selected" else "Tap to choose",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveVersion(selectedTag) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                modifier = Modifier.testTag("confirm_save_version_button")
            ) {
                Text("Save Snapshot", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = StudioSurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EmptyProjectsState(
    onImportClick: () -> Unit,
    onCreateClick: () -> Unit,
    onOpenGuide: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = StudioCardDark,
            modifier = Modifier.size(76.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentCyan.copy(alpha = 0.5f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.VideoLibrary, "Projects", tint = AccentCyan, modifier = Modifier.size(38.dp))
            }
        }

        Text(
            text = "No Video Projects Yet",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Import a video directly from your phone gallery to trim, cut, duck audio, and export in 4K.",
            color = Color.LightGray,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onImportClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
                .testTag("dashboard_empty_import_button")
        ) {
            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import Video from Device", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF374151)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("dashboard_empty_create_blank_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Blank Project", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = onOpenGuide,
                colors = ButtonDefaults.buttonColors(containerColor = StudioCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("dashboard_empty_guide_button")
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("App Guide", color = AccentCyan, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (title: String, aspect: AspectRatio, withSampleContent: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var selectedAspect by remember { mutableStateOf(AspectRatio.VERTICAL_9_16) }
    var preloadSamples by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "New Video Project",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Project Title") },
                placeholder = { Text("e.g., Summer Roadtrip 2026") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_project_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Canvas Aspect Ratio:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(AspectRatio.entries.toTypedArray()) { aspect ->
                    val isSelected = selectedAspect == aspect
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AccentCyan else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .clickable { selectedAspect = aspect }
                            .testTag("aspect_${aspect.name}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = aspect.label.split(" ").first(),
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = aspect.iconDescription,
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = preloadSamples,
                    onCheckedChange = { preloadSamples = it },
                    colors = CheckboxDefaults.colors(checkedColor = AccentCyan)
                )
                Text(
                    text = "Preload with sample cinematic clips (optional demo)",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Untitled Project" }
                    onCreate(finalTitle, selectedAspect, preloadSamples)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("create_project_confirm_button")
            ) {
                Text("Start Editing", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RenameProjectDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Project",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter a new title for your video project:",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    placeholder = { Text("Project title", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color(0xFF38315E),
                        cursorColor = AccentCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_project_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier.testTag("confirm_rename_button")
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_rename_button")
            ) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = StudioSurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AppEducationGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = AccentCyan.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.School, contentDescription = "Education Guide", tint = AccentCyan, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "How to Use OpenCut",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complete Editor Guidelines & Tips",
                        color = AccentCyan,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                EducationGuideStepItem(
                    stepNumber = "1",
                    title = "Import Video from Gallery",
                    description = "Tap 'Import Video from Device' to select any video from your phone. OpenCut loads real 1080p/4K resolution and frame rates.",
                    icon = Icons.Default.VideoLibrary,
                    color = AccentCyan
                )

                EducationGuideStepItem(
                    stepNumber = "2",
                    title = "Drag Playhead & Scrub",
                    description = "Drag the red playhead handle across the timeline to scrub. Use zoom (+/-) for frame-accurate precision editing.",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFFFFB300)
                )

                EducationGuideStepItem(
                    stepNumber = "3",
                    title = "Split, Cut & Ripple Edit",
                    description = "Position playhead and tap 'Split' to cut clips. Delete unwanted pieces and remaining clips automatically snap into place.",
                    icon = Icons.Default.ContentCut,
                    color = Color(0xFFEF4444)
                )

                EducationGuideStepItem(
                    stepNumber = "4",
                    title = "Audio Ducking & Music",
                    description = "Add background music and voiceover. Enable 'Audio Ducking' so music smoothly dips when speech is detected.",
                    icon = Icons.Default.Audiotrack,
                    color = Color(0xFF10B981)
                )

                EducationGuideStepItem(
                    stepNumber = "5",
                    title = "Export Without Watermark",
                    description = "Render your timeline in Full HD 1080p or 4K Ultra HD at 60fps directly saved to your device gallery.",
                    icon = Icons.Default.FileDownload,
                    color = AccentPink
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("dismiss_guide_dialog_button")
            ) {
                Text("Got It, Let's Start Editing!", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = StudioSurfaceDark,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun EducationGuideStepItem(
    stepNumber: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = stepNumber, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = description,
                color = Color.LightGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ImportSourceDialog(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onBrowseFiles: () -> Unit,
    onGenerateTestVideo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Import Video from Device", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Choose how you'd like to load video into OpenCut:",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )

                // Option 1: Gallery / Photos
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onPickGallery()
                        }
                        .testTag("import_source_gallery")
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Pick from Gallery / Photos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Select one or multiple videos via Photo Picker", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }

                // Option 2: Files / Downloads / SAF
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onBrowseFiles()
                        }
                        .testTag("import_source_files")
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Browse Device Files & Downloads", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Select MP4, MOV, MKV, WebM from any folder", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }

                // Option 3: Quick Test Video on Device
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onGenerateTestVideo()
                        }
                        .testTag("import_source_generate_test")
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Quick Test: Device Camera Video", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Generate local 1080p sample footage on device", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = StudioSurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}
