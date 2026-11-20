package com.example.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.cloud.CloudProject
import com.example.data.cloud.CloudSyncRepository
import com.example.data.cloud.CreatorAnalytics
import com.example.data.db.ProjectEntity
import com.example.data.db.TimelineSerializer
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.util.AppStorageBreakdown
import com.example.util.StudioExportUtilities
import com.example.viewmodel.EditorUiState
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface CloudUiState<out T> {
    data object Loading : CloudUiState<Nothing>
    data class Success<T>(val data: T) : CloudUiState<T>
    data class Error(val message: String) : CloudUiState<Nothing>
}

class CloudAccountViewModel(
    private val repository: CloudSyncRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val cloudProjectsState: StateFlow<CloudUiState<List<CloudProject>>> =
        repository.observeCloudProjects(currentUserId)
            .map<List<CloudProject>, CloudUiState<List<CloudProject>>> { CloudUiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing cloud projects", rootError)
                emit(CloudUiState.Error(rootError.localizedMessage ?: "Failed to load cloud projects"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = CloudUiState.Loading
            )

    val creatorAnalyticsState: StateFlow<CloudUiState<CreatorAnalytics?>> =
        repository.observeCreatorAnalytics(currentUserId)
            .map<CreatorAnalytics?, CloudUiState<CreatorAnalytics?>> { CloudUiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing creator analytics", rootError)
                emit(CloudUiState.Error(rootError.localizedMessage ?: "Failed to load cloud analytics"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = CloudUiState.Loading
            )

    fun syncAllToCloud(
        displayName: String,
        projects: List<ProjectEntity>,
        exportedVideosCount: Int,
        totalTimelineDurationMs: Long,
        favoriteFilterPreset: String
    ) {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = null
            var syncedCount = 0
            for (proj in projects) {
                val parsedClipCount = runCatching {
                    TimelineSerializer.deserialize(proj.timelineDataJson).clips.size.toLong()
                }.getOrDefault(proj.clipCount.toLong())
                val res = repository.syncProjectToCloud(
                    userId = currentUserId,
                    projectId = proj.id,
                    title = proj.title,
                    aspectRatio = proj.aspectRatioName,
                    durationMs = proj.durationMs.coerceAtLeast(proj.lastPlayheadPositionMs),
                    clipCount = parsedClipCount,
                    activeFilterPreset = favoriteFilterPreset
                )
                if (res.isSuccess) syncedCount++
            }
            val storageKb = (projects.sumOf { it.timelineDataJson.length.toLong() } / 1024L + exportedVideosCount * 128L).coerceAtLeast(16L)
            val analyticsRes = repository.syncAnalyticsSummary(
                userId = currentUserId,
                displayName = displayName,
                totalProjectsCount = projects.size.toLong(),
                exportedVideosCount = exportedVideosCount.toLong(),
                totalTimelineDurationMs = totalTimelineDurationMs,
                favoriteFilterPreset = favoriteFilterPreset,
                cloudStorageUsedKb = storageKb
            )
            _isSyncing.value = false
            _syncMessage.value = if (analyticsRes.isSuccess) {
                "Synced $syncedCount projects & creator analytics to Cloud Firestore"
            } else {
                "Cloud sync encountered an issue: ${analyticsRes.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun deleteCloudProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteCloudProject(currentUserId, projectId)
        }
    }

    private companion object {
        const val TAG = "CloudAccountVM"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAnalyticsScreen(
    projects: List<ProjectEntity>,
    uiState: EditorUiState,
    onOpenDrawer: () -> Unit = {},
    onOpenProject: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Observe Firebase Auth state reactively without forcing sign-in on app start
    val firebaseAuth = remember {
        try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
    }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(firebaseAuth?.currentUser) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    DisposableEffect(firebaseAuth) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        firebaseAuth?.addAuthStateListener(listener)
        onDispose {
            firebaseAuth?.removeAuthStateListener(listener)
        }
    }

    // Local Studio Analytics Metrics (computed from real Room projects, rendered archive, and disk usage)
    val totalProjectsCount = projects.size
    val exportedCount = uiState.renderedVideos.size
    val activeClipCount = uiState.clips.size
    val summedProjectDurationMs = projects.sumOf { it.durationMs }
    val totalDurationSec = (if (summedProjectDurationMs > 0L) summedProjectDurationMs else uiState.totalDurationMs) / 1000L
    val favoriteFilter = uiState.selectedClip?.filter?.displayName
        ?.takeIf { it != "Original" }
        ?: uiState.clips.firstOrNull { it.filter.displayName != "Original" }?.filter?.displayName
        ?: "Original"
    val actualStorageBytes = uiState.storageUsageBytes
    val actualStorageMb = actualStorageBytes / (1024f * 1024f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_icon),
                            contentDescription = "OpenCut Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Account & Creator Analytics",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentUser != null) "Cloud Sync Active • ${currentUser?.email ?: "Signed In"}"
                                else "Local Studio Mode • Sign in optional",
                                color = AccentCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("account_drawer_button")) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioSurfaceDark)
            )
        },
        containerColor = StudioDarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("account_analytics_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Section 1: Optional Account & Cloud Backup Card
                val user = currentUser
                if (user == null) {
                    UnauthenticatedAccountCard(
                        isSigningIn = isSigningIn,
                        errorMessage = authErrorMessage,
                        onSignInClick = {
                            coroutineScope.launch {
                                isSigningIn = true
                                authErrorMessage = null
                                signInWithGoogleCredentialManager(
                                    context = context,
                                    onSuccess = {
                                        isSigningIn = false
                                    },
                                    onError = { msg ->
                                        isSigningIn = false
                                        authErrorMessage = msg
                                    }
                                )
                            }
                        }
                    )
                } else {
                    // Authenticated: Compose CloudAccountSection gated by non-null user.uid
                    AuthenticatedCloudAccountSection(
                        user = user,
                        projects = projects,
                        exportedCount = exportedCount,
                        totalTimelineDurationMs = totalDurationSec * 1000L,
                        favoriteFilterPreset = favoriteFilter,
                        onSignOut = {
                            firebaseAuth?.signOut()
                            authErrorMessage = null
                        }
                    )
                }
            }

            // Section 2: Real-Time Creator Analytics Overview Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Studio Production Analytics",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AccentCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "LIVE TELEMETRY",
                                color = AccentCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnalyticsMetricCard(
                            title = "Total Projects",
                            value = "$totalProjectsCount",
                            subtitle = "${projects.count { it.lastPlayheadPositionMs > 0L }} active timelines",
                            icon = Icons.Default.Movie,
                            accentColor = AccentCyan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analytics_card_total_projects")
                        )
                        AnalyticsMetricCard(
                            title = "Rendered Exports",
                            value = "$exportedCount",
                            subtitle = "1080p / 4K master renders",
                            icon = Icons.Default.VideoFile,
                            accentColor = AccentPink,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analytics_card_exports")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnalyticsMetricCard(
                            title = "Edited Footage",
                            value = "${totalDurationSec}s",
                            subtitle = "$activeClipCount clips in current cut",
                            icon = Icons.Default.Timer,
                            accentColor = Color(0xFF10B981),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analytics_card_duration")
                        )
                        AnalyticsMetricCard(
                            title = "Top Color Look",
                            value = favoriteFilter,
                            subtitle = "Primary LUT & filter grade",
                            icon = Icons.Default.Palette,
                            accentColor = Color(0xFFF59E0B),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("analytics_card_top_filter")
                        )
                    }
                }
            }

            // Section 3: Visual Editing Velocity & Filter Usage Breakdown Chart
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_velocity_chart_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Weekly Editing & Render Throughput",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Timeline cuts, color grades & AI studio passes",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${projects.size + exportedCount} Actions",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Real 7-Day Activity Bar Chart computed from actual Room & Export timestamps
                        val (dayLabels, barValues) = remember(projects, uiState.renderedVideos) {
                            StudioExportUtilities.computeWeeklyActivityBuckets(
                                projectUpdatedTimestamps = projects.map { it.updatedAt },
                                renderedCreatedTimestamps = uiState.renderedVideos.map { it.createdAt }
                            )
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .padding(top = 6.dp)
                        ) {
                            val barCount = barValues.size
                            val slotWidth = size.width / barCount
                            val barWidth = slotWidth * 0.48f

                            barValues.forEachIndexed { index, ratio ->
                                val barHeight = size.height * ratio
                                val x = index * slotWidth + (slotWidth - barWidth) / 2f
                                val y = size.height - barHeight

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = if (index == barCount - 1) {
                                            listOf(AccentCyan, AccentPink)
                                        } else {
                                            listOf(AccentCyan.copy(alpha = 0.75f), Color(0xFF1E3A8A))
                                        }
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            dayLabels.forEach { day ->
                                Text(
                                    text = day,
                                    color = Color.LightGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Storage & Hardware Pipeline Breakdown
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioSurfaceDark,
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Storage & Pipeline Telemetry",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Actual App Media & Cache Usage",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = AppStorageBreakdown.formatBytes(actualStorageBytes),
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (actualStorageMb / 2048f).coerceIn(0.02f, 1f) },
                            color = AccentCyan,
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Active Proxy Mode: ${uiState.proxyQualityMode.label}",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "60 FPS Preview Engine",
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun UnauthenticatedAccountCard(
    isSigningIn: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131C31),
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("unauthenticated_account_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = AccentCyan.copy(alpha = 0.18f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Creator Account",
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OpenCut Cloud Account (Optional)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All editing features work 100% offline. Sign in with Google anytime to back up projects & sync analytics across devices.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF3F1D2B),
                    border = BorderStroke(1.dp, AccentPink.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFDA4AF),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = onSignInClick,
                enabled = !isSigningIn,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_google_sign_in_button")
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connecting to Google...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign in with Google to Enable Cloud Sync",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedCloudAccountSection(
    user: FirebaseUser,
    projects: List<ProjectEntity>,
    exportedCount: Int,
    totalTimelineDurationMs: Long,
    favoriteFilterPreset: String,
    onSignOut: () -> Unit
) {
    val currentUserId = user.uid
    val cloudViewModel: CloudAccountViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CloudAccountViewModel(CloudSyncRepository(db), currentUserId)
            }
        }
    )

    val cloudProjectsState by cloudViewModel.cloudProjectsState.collectAsStateWithLifecycle()
    val creatorAnalyticsState by cloudViewModel.creatorAnalyticsState.collectAsStateWithLifecycle()
    val isSyncing by cloudViewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by cloudViewModel.syncMessage.collectAsStateWithLifecycle()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF131C31),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("authenticated_account_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Signed In",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = user.displayName?.takeIf { it.isNotBlank() } ?: "OpenCut Pro Creator",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email ?: "UID: ${user.uid.take(12)}...",
                            color = AccentCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSignOut,
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("account_sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sign Out", color = Color.LightGray, fontSize = 11.sp)
                }
            }

            // Sync Now Button
            Button(
                onClick = {
                    cloudViewModel.syncAllToCloud(
                        displayName = user.displayName ?: "OpenCut Creator",
                        projects = projects,
                        exportedVideosCount = exportedCount,
                        totalTimelineDurationMs = totalTimelineDurationMs,
                        favoriteFilterPreset = favoriteFilterPreset
                    )
                },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_projects_to_cloud_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSyncing) "Syncing to Cloud Firestore..." else "Sync ${projects.size} Projects & Analytics to Cloud",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (syncMessage != null) {
                Text(
                    text = syncMessage!!,
                    color = Color(0xFF34D399),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Cloud Analytics Summary Snapshot
            when (val analyticsState = creatorAnalyticsState) {
                is CloudUiState.Success -> {
                    val summary = analyticsState.data
                    if (summary != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StudioCardDark,
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Cloud Analytics Snapshot",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${summary.totalProjectsCount} Cloud Projects • ${summary.exportedVideosCount} Exports • Look: ${summary.favoriteFilterPreset}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "${summary.cloudStorageUsedKb} KB",
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                is CloudUiState.Error -> {
                    Text(
                        text = analyticsState.message,
                        color = AccentPink,
                        fontSize = 10.sp
                    )
                }
                CloudUiState.Loading -> {}
            }

            // Cloud Synced Projects List
            when (val projState = cloudProjectsState) {
                is CloudUiState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = AccentCyan, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading cloud projects...", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
                is CloudUiState.Error -> {
                    Text(projState.message, color = AccentPink, fontSize = 11.sp)
                }
                is CloudUiState.Success -> {
                    val cloudProjects = projState.data
                    Text(
                        text = "Synced Cloud Projects (${cloudProjects.size})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (cloudProjects.isEmpty()) {
                        Text(
                            text = "No projects backed up yet. Tap 'Sync Projects & Analytics to Cloud' above.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("cloud_projects_list")
                        ) {
                            cloudProjects.take(5).forEach { cloudProj ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StudioCardDark,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cloudProj.title,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${cloudProj.aspectRatio} • Look: ${cloudProj.activeFilterPreset}",
                                                color = AccentCyan,
                                                fontSize = 10.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { cloudViewModel.deleteCloudProject(cloudProj.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Cloud Backup",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
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
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioSurfaceDark,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private suspend fun signInWithGoogleCredentialManager(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val credentialManager = CredentialManager.create(context)
        val webClientId = context.getString(R.string.default_web_client_id)
        val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            Firebase.auth.signInWithCredential(firebaseCredential).await()
            onSuccess()
        } else {
            onError("Unexpected credential type received.")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: GetCredentialCancellationException) {
        onError("Sign-in was canceled.")
    } catch (_: NoCredentialException) {
        onError("No Google account found on this device. Please add a Google account in device Settings.")
    } catch (e: Exception) {
        Log.e("AccountAnalytics", "Google Sign-In failed", e)
        onError(e.localizedMessage ?: "Google Sign-In failed.")
    }
}
