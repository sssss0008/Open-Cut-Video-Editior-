package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.db.ImportedMediaEntity
import com.example.data.model.AudioTrackItem
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceDark
import com.example.util.MediaDiagnostics
import com.example.viewmodel.VideoEditorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.math.abs

/**
 * Real Media3 ExoPlayer surface bound to the active timeline VideoClip + synchronized
 * background audio tracks (with automatic voiceover ducking volume control).
 */
@OptIn(UnstableApi::class)
@Composable
fun RealExoVideoSurface(
    clip: VideoClip,
    currentPositionMs: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    isVideoTrackMuted: Boolean,
    audioTracks: List<AudioTrackItem> = emptyList(),
    isAudioTrackMuted: Boolean = false,
    isVoiceoverActive: Boolean = false,
    autoDuckingLevel: Float = 0.25f,
    masterVolume: Float = 1.0f,
    isAutoDuckingEnabled: Boolean = true,
    onBufferingChanged: (Boolean) -> Unit = {},
    onPlaybackError: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSyntheticSample = clip.uri.isBlank() || clip.uri.startsWith("sample://")
    val hasValidLocalSource = remember(clip.uri) {
        if (isSyntheticSample) false
        else if (clip.uri.startsWith("content://")) true
        else File(clip.uri.removePrefix("file://")).exists()
    }

    if (!hasValidLocalSource) {
        // Fallback notice is handled cleanly if missing non-sample file
        if (!isSyntheticSample) {
            LaunchedEffect(clip.uri) {
                onPlaybackError("Video file missing: ${clip.name}")
                MediaDiagnostics.error("EXOPLAYER", "Missing local video file", clip.uri)
            }
        }
        return
    }

    val videoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // Secondary audio player for first active local audio soundtrack / voiceover
    val audioPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(videoPlayer, audioPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                onBufferingChanged(playbackState == Player.STATE_BUFFERING)
            }

            override fun onPlayerError(error: PlaybackException) {
                val msg = "Playback error (${error.errorCodeName}): ${error.localizedMessage ?: "Unsupported video stream"}"
                MediaDiagnostics.error("EXOPLAYER", msg, clip.uri, error)
                onPlaybackError(msg)
            }
        }
        videoPlayer.addListener(listener)
        onDispose {
            videoPlayer.removeListener(listener)
            videoPlayer.release()
            audioPlayer.release()
        }
    }

    // Load MediaItem whenever active clip source or trim boundaries change
    LaunchedEffect(clip.id, clip.uri, clip.trimInMs, clip.trimOutMs) {
        onPlaybackError(null)
        val uri = if (clip.uri.startsWith("content://")) {
            Uri.parse(clip.uri)
        } else {
            Uri.fromFile(File(clip.uri.removePrefix("file://")))
        }
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.trimInMs.coerceAtLeast(0L))
                    .setEndPositionMs(clip.trimOutMs.coerceAtLeast(clip.trimInMs + 100L))
                    .build()
            )
            .build()
        videoPlayer.setMediaItem(mediaItem)
        videoPlayer.prepare()
    }

    // Update speed & audio volume dynamically (including fade-in/out)
    val elapsedInClip = (currentPositionMs - clip.timelineStartMs).coerceAtLeast(0L)
    LaunchedEffect(
        isPlaying,
        playbackSpeed,
        clip.speed,
        clip.volume,
        clip.isMuted,
        isVideoTrackMuted,
        masterVolume,
        elapsedInClip
    ) {
        val effectiveSpeed = (playbackSpeed * clip.speed).coerceIn(0.25f, 4.0f)
        videoPlayer.playbackParameters = PlaybackParameters(effectiveSpeed)
        val fadeGain = computeFadeGain(elapsedInClip, clip.trimmedDurationMs, clip.fadeInMs, clip.fadeOutMs)
        val vol = if (clip.isMuted || isVideoTrackMuted) 0f else (clip.volume * masterVolume * fadeGain).coerceIn(0f, 1f)
        videoPlayer.volume = vol
    }

    // Synchronize video ExoPlayer play/pause & seek position with timeline playhead
    LaunchedEffect(isPlaying, currentPositionMs, clip.id) {
        val targetClipMs = ((currentPositionMs - clip.timelineStartMs).coerceAtLeast(0L) * clip.speed).toLong()
        val drift = abs(videoPlayer.currentPosition - targetClipMs)
        if (!isPlaying || drift > 260L) {
            videoPlayer.seekTo(targetClipMs.coerceAtLeast(0L))
        }
        if (isPlaying && !videoPlayer.isPlaying) {
            videoPlayer.playWhenReady = true
            videoPlayer.play()
        } else if (!isPlaying && videoPlayer.isPlaying) {
            videoPlayer.pause()
        }
    }

    // Sync active local audio track player + real-time audio ducking volume
    val activeLocalAudio = remember(audioTracks, currentPositionMs) {
        audioTracks.firstOrNull {
            it.uri.isNotBlank() &&
                !it.uri.startsWith("sample://") &&
                currentPositionMs in it.timelineStartMs..it.timelineEndMs
        }
    }
    LaunchedEffect(activeLocalAudio?.id, activeLocalAudio?.uri) {
        val track = activeLocalAudio
        if (track != null) {
            val audioUri = if (track.uri.startsWith("content://")) {
                Uri.parse(track.uri)
            } else {
                val f = File(track.uri.removePrefix("file://"))
                if (f.exists()) Uri.fromFile(f) else null
            }
            if (audioUri != null) {
                audioPlayer.setMediaItem(MediaItem.fromUri(audioUri))
                audioPlayer.prepare()
            }
        } else {
            audioPlayer.stop()
            audioPlayer.clearMediaItems()
        }
    }
    LaunchedEffect(
        activeLocalAudio?.id,
        isPlaying,
        currentPositionMs,
        isAudioTrackMuted,
        isVoiceoverActive,
        isAutoDuckingEnabled,
        autoDuckingLevel,
        masterVolume,
        playbackSpeed
    ) {
        val track = activeLocalAudio
        if (track != null) {
            val localAudioPos = (currentPositionMs - track.timelineStartMs).coerceAtLeast(0L)
            if (!isPlaying || abs(audioPlayer.currentPosition - localAudioPos) > 280L) {
                audioPlayer.seekTo(localAudioPos)
            }
            audioPlayer.playbackParameters = PlaybackParameters(playbackSpeed.coerceIn(0.25f, 4f))
            val shouldDuck = isAutoDuckingEnabled &&
                (track.isDuckingEnabled || track.category == com.example.data.model.AudioCategory.MUSIC) &&
                isVoiceoverActive
            val fadeGain = computeFadeGain(localAudioPos, track.durationMs, track.fadeInMs, track.fadeOutMs)
            val baseVol = if (track.isMuted || isAudioTrackMuted) 0f else track.volume * fadeGain * masterVolume
            audioPlayer.volume = (if (shouldDuck) baseVol * autoDuckingLevel else baseVol).coerceIn(0f, 1f)
            if (isPlaying && !audioPlayer.isPlaying) {
                audioPlayer.play()
            } else if (!isPlaying && audioPlayer.isPlaying) {
                audioPlayer.pause()
            }
        } else if (audioPlayer.isPlaying) {
            audioPlayer.pause()
        }
    }

    val cropScaleX = if (clip.hasCrop) (1f / (clip.cropRight - clip.cropLeft).coerceAtLeast(0.1f)) else 1f
    val cropScaleY = if (clip.hasCrop) (1f / (clip.cropBottom - clip.cropTop).coerceAtLeast(0.1f)) else 1f
    val finalScaleX = (if (clip.flipHorizontal) -clip.scale else clip.scale) * cropScaleX
    val finalScaleY = clip.scale * cropScaleY

    AndroidView(
        factory = { ctx ->
            val inflated = runCatching {
                android.view.LayoutInflater.from(ctx)
                    .inflate(com.example.R.layout.texture_player_view, null, false) as? PlayerView
            }.getOrNull()
            (inflated ?: PlayerView(ctx)).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                player = videoPlayer
            }
        },
        update = { view ->
            if (view.player !== videoPlayer) {
                view.player = videoPlayer
            }
        },
        modifier = modifier
            .fillMaxSize()
            .rotate(clip.rotation)
            .scale(finalScaleX, finalScaleY)
            .alpha(clip.alpha)
            .testTag("exo_player_surface")
    )
}

private fun computeFadeGain(localMs: Long, totalMs: Long, fadeInMs: Long, fadeOutMs: Long): Float {
    var gain = 1f
    if (fadeInMs > 0L && localMs < fadeInMs) {
        gain *= (localMs.toFloat() / fadeInMs.toFloat()).coerceIn(0f, 1f)
    }
    if (fadeOutMs > 0L && totalMs > fadeOutMs) {
        val rem = (totalMs - localMs).coerceAtLeast(0L)
        if (rem < fadeOutMs) {
            gain *= (rem.toFloat() / fadeOutMs.toFloat()).coerceIn(0f, 1f)
        }
    }
    return gain
}

/**
 * Full-screen & Landscape-compatible Media3 ExoPlayer Preview Dialog.
 */
@OptIn(UnstableApi::class)
@Composable
fun FullscreenPreviewDialog(
    clip: VideoClip?,
    currentPositionMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    playbackSpeed: Float,
    masterVolume: Float,
    onVolumeChange: (Float) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var forceLandscapePreview by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier
                .fillMaxSize()
                .testTag("fullscreen_preview_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar: Orientation toggle & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = clip?.name ?: "Fullscreen Preview",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (forceLandscapePreview) AccentCyan.copy(alpha = 0.25f) else StudioCardDark,
                            border = BorderStroke(1.dp, if (forceLandscapePreview) AccentCyan else Color(0xFF334155)),
                            modifier = Modifier
                                .clickable { forceLandscapePreview = !forceLandscapePreview }
                                .testTag("fullscreen_landscape_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ScreenRotation, "Landscape", tint = AccentCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (forceLandscapePreview) "16:9 Landscape" else "9:16 Portrait",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_fullscreen_preview")) {
                            Icon(Icons.Default.Close, "Exit Fullscreen", tint = Color.White)
                        }
                    }
                }

                // Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val ratio = if (forceLandscapePreview) 16f / 9f else 9f / 16f
                    Box(
                        modifier = Modifier
                            .aspectRatio(ratio)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A0C12)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (clip != null) {
                            RealExoVideoSurface(
                                clip = clip,
                                currentPositionMs = currentPositionMs,
                                isPlaying = isPlaying,
                                playbackSpeed = playbackSpeed,
                                isVideoTrackMuted = false,
                                masterVolume = masterVolume,
                                onBufferingChanged = { isBuffering = it },
                                onPlaybackError = { playbackError = it }
                            )
                        }
                        if (isBuffering) {
                            CircularProgressIndicator(color = AccentCyan)
                        }
                        playbackError?.let { err ->
                            Surface(
                                color = Color(0xCC7F1D1D),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFFCA5A5))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(err, color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Scrubbing Seekbar & Transport
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = VideoEditorViewModel.formatTime(currentPositionMs),
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Slider(
                            value = currentPositionMs.toFloat().coerceIn(0f, totalDurationMs.toFloat().coerceAtLeast(1f)),
                            onValueChange = { onSeek(it.toLong()) },
                            valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = VideoEditorViewModel.formatTime(totalDurationMs),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Volume Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(onClick = { onVolumeChange(if (masterVolume > 0f) 0f else 1f) }) {
                                Icon(
                                    imageVector = if (masterVolume <= 0.01f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Volume",
                                    tint = Color.White
                                )
                            }
                            Slider(
                                value = masterVolume,
                                onValueChange = onVolumeChange,
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                                modifier = Modifier.width(110.dp)
                            )
                        }

                        // Play / Pause
                        Surface(
                            shape = CircleShape,
                            color = AccentCyan,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onTogglePlay() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Playback speed pill
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StudioCardDark,
                                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.6f)),
                                modifier = Modifier.clickable {
                                    val next = when (playbackSpeed) {
                                        0.5f -> 1.0f
                                        1.0f -> 1.5f
                                        1.5f -> 2.0f
                                        else -> 0.5f
                                    }
                                    onSetSpeed(next)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Speed, null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${playbackSpeed}x", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pre-Edit Playback Verification Gate:
 * Loads the selected/imported video inside Media3 ExoPlayer and verifies it actually decodes
 * and plays cleanly (`STATE_READY` + valid video dimensions) before allowing the user into Edit Mode.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlaybackVerificationDialog(
    media: ImportedMediaEntity,
    onVerifiedAndEnterEditor: (ImportedMediaEntity) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var isPlayableVerified by remember { mutableStateOf(media.isPlaybackVerified) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(media.durationMs.coerceAtLeast(1000L)) }
    var volume by remember { mutableFloatStateOf(1f) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    val exoPlayer = remember(context, media.uri) {
        ExoPlayer.Builder(context).build().apply {
            val uri = if (media.uri.startsWith("content://")) {
                Uri.parse(media.uri)
            } else {
                Uri.fromFile(File(media.uri.removePrefix("file://")))
            }
            setMediaItem(MediaItem.fromUri(uri))
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    val dur = exoPlayer.duration
                    if (dur > 0L) totalDurationMs = dur
                    isPlayableVerified = true
                    MediaDiagnostics.info("VERIFY_PLAY", "Pre-edit playback verified", "${media.name} (${totalDurationMs}ms)")
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    isPlayableVerified = true
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                isPlayableVerified = false
                val msg = "Cannot play this video (${error.errorCodeName}): ${error.localizedMessage ?: "Unsupported codec or corrupted file"}"
                playbackError = msg
                MediaDiagnostics.error("VERIFY_PLAY", msg, media.uri, error)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(exoPlayer) {
        while (isActive) {
            currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            delay(150L)
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurfaceDark,
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("playback_verification_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Verify Video Playback", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = media.name,
                            color = AccentCyan,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ExoPlayer Surface
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                player = exoPlayer
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isBuffering) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                    playbackError?.let { err ->
                        Surface(
                            color = Color(0xDD7F1D1D),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(err, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                        }
                    }
                }

                // Seek & Volume controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = AccentCyan
                        )
                    }
                    Slider(
                        value = currentPosMs.toFloat().coerceIn(0f, totalDurationMs.toFloat().coerceAtLeast(1f)),
                        onValueChange = { exoPlayer.seekTo(it.toLong()) },
                        valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        volume = if (volume > 0f) 0f else 1f
                        exoPlayer.volume = volume
                    }) {
                        Icon(
                            imageVector = if (volume <= 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute/Unmute",
                            tint = Color.LightGray
                        )
                    }
                }

                // Metadata Summary Badges
                Text(
                    text = buildString {
                        if (media.width > 0 && media.height > 0) append("${media.width}x${media.height} • ")
                        if (media.frameRate > 0f) append("${media.frameRate.toInt()} fps • ")
                        if (media.videoCodec.isNotBlank()) append("${media.videoCodec} • ")
                        append(media.audioInfo.ifBlank { "Checking audio" })
                    },
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Verification Status Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = if (playbackError != null) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (playbackError != null) Color(0xFFEF4444) else if (isPlayableVerified) AccentEmerald else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            playbackError != null -> "Verification Failed — cannot enter editor with unplayable file"
                            isPlayableVerified -> "Playback Verified! Video & audio ready for editing"
                            else -> "Decoding video stream..."
                        },
                        color = if (playbackError != null) Color(0xFFFCA5A5) else if (isPlayableVerified) AccentEmerald else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = Color.White)
                    }
                    Button(
                        onClick = { onVerifiedAndEnterEditor(media.copy(isPlaybackVerified = true)) },
                        enabled = isPlayableVerified && playbackError == null,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("confirm_verified_enter_editor")
                    ) {
                        Text("Edit on Timeline", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
