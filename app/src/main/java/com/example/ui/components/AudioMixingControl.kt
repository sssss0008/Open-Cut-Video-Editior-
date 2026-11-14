package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioCategory
import com.example.data.model.AudioTrackItem
import com.example.data.model.VideoClip
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.ui.theme.TrackAudioColor
import com.example.viewmodel.EditorUiState
import com.example.viewmodel.VideoEditorViewModel

/**
 * Audio Mixing Console & Auto-Ducking Control Component.
 * Enables automatic background audio attenuation (ducking) when voiceover or dialogue is active.
 */
@Composable
fun AudioMixingControl(
    uiState: EditorUiState,
    onToggleAutoDuckingMaster: (Boolean) -> Unit,
    onSetAutoDuckingLevel: (Float) -> Unit,
    onToggleTrackDucking: (audioId: String) -> Unit,
    onSetTrackVolume: (audioId: String, volume: Float, isMuted: Boolean) -> Unit,
    onSetTrackPan: (audioId: String, pan: Float) -> Unit,
    onSetTrackCategory: (audioId: String, category: AudioCategory) -> Unit,
    onSetClipVolume: (clipId: String, volume: Float, isMuted: Boolean) -> Unit,
    onToggleClipSpeech: (clipId: String, hasSpeech: Boolean) -> Unit = { _, _ -> },
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDuckingActive = uiState.isVoiceoverActive
    val duckLevel = uiState.autoDuckingDuckLevel

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfaceDark)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("audio_mixing_control")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = TrackAudioColor.copy(alpha = 0.25f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Mixer",
                            tint = TrackAudioColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Audio Mixer & Auto-Ducking",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Balance background tracks & automate voiceover ducking",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SECTION 1: PRO AUDIO AUTO-DUCKING CONTROL PANEL
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isDuckingActive) Color(0xFF261D13) else Color(0xFF131A26),
            border = BorderStroke(
                1.5.dp,
                if (isDuckingActive) Color(0xFFFFB300) else if (uiState.isAutoDuckingMasterEnabled) AccentCyan.copy(alpha = 0.6f) else Color(0xFF232A3B)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audio_ducking_panel")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header with Master Auto-Ducking Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Ducking",
                            tint = if (isDuckingActive) Color(0xFFFFB300) else AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Auto-Ducking Engine",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lowers background audio when speech is active",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isAutoDuckingMasterEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleAutoDuckingMaster(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFB300),
                            checkedTrackColor = Color(0xFF5D4002),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF202636)
                        ),
                        modifier = Modifier.testTag("auto_ducking_master_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time Ducking Status Pill Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDuckingActive) Color(0x33FFB300) else Color(0x2200E5FF),
                    border = BorderStroke(
                        1.dp,
                        if (isDuckingActive) Color(0xFFFFB300) else AccentCyan.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auto_ducking_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isDuckingActive) "⚡ DUCKING ACTIVE" else "● STANDBY",
                                color = if (isDuckingActive) Color(0xFFFFB300) else AccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDuckingActive) {
                                    val source = when {
                                        uiState.isPrimaryVideoSpeechActive && uiState.isVoiceoverTrackSpeechActive -> "Video & Voiceover"
                                        uiState.isPrimaryVideoSpeechActive -> "Primary Video Speech"
                                        else -> "Voiceover Speech"
                                    }
                                    "$source detected: BGM lowered to ${(duckLevel * 100).toInt()}%"
                                } else if (!uiState.isAutoDuckingMasterEnabled) {
                                    "Auto-Ducking disabled"
                                } else {
                                    "Monitoring Voiceover & Primary Video track speech"
                                },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Animated live attenuation meter
                        LiveDuckingPulsar(isActive = isDuckingActive)
                    }
                }

                // Attenuation Slider & Presets (when enabled)
                AnimatedVisibility(visible = uiState.isAutoDuckingMasterEnabled) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ducked Background Volume: ${(duckLevel * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = when {
                                    duckLevel <= 0.05f -> "-∞ dB (Full Mute)"
                                    duckLevel <= 0.15f -> "-18 dB (Deep)"
                                    duckLevel <= 0.30f -> "-12 dB (Standard)"
                                    else -> "-6 dB (Subtle)"
                                },
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = duckLevel,
                            onValueChange = { onSetAutoDuckingLevel(it) },
                            valueRange = 0f..0.60f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFB300),
                                activeTrackColor = Color(0xFFFFB300),
                                inactiveTrackColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .testTag("auto_ducking_level_slider")
                        )

                        // Quick Ducking Preset Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DuckingPresetChip(
                                label = "Subtle (-6dB)",
                                level = 0.50f,
                                current = duckLevel,
                                onClick = { onSetAutoDuckingLevel(0.50f) }
                            )
                            DuckingPresetChip(
                                label = "Standard (-12dB)",
                                level = 0.25f,
                                current = duckLevel,
                                onClick = { onSetAutoDuckingLevel(0.25f) }
                            )
                            DuckingPresetChip(
                                label = "Deep (-18dB)",
                                level = 0.10f,
                                current = duckLevel,
                                onClick = { onSetAutoDuckingLevel(0.10f) }
                            )
                            DuckingPresetChip(
                                label = "Mute (0%)",
                                level = 0.0f,
                                current = duckLevel,
                                onClick = { onSetAutoDuckingLevel(0.0f) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION 2: MULTI-TRACK AUDIO MIXING STRIPS
        Text(
            text = "Track Volume & Ducking Routing",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main Video Track Audio Strip (if video clips exist)
            // Main Video Track Audio Strip (if video clips exist)
            val activeClip = uiState.selectedClip ?: uiState.clips.firstOrNull()
            if (activeClip != null) {
                item {
                    VideoTrackAudioStrip(
                        clip = activeClip,
                        onUpdateVolume = { vol, muted -> onSetClipVolume(activeClip.id, vol, muted) },
                        onToggleSpeech = { hasSpeech -> onToggleClipSpeech(activeClip.id, hasSpeech) }
                    )
                }
            }

            // Soundtrack / Audio Tracks Strips
            items(uiState.audioClips) { track ->
                val effectiveVol = uiState.getEffectiveTrackVolume(track)
                val isBeingDucked = isDuckingActive && (track.isDuckingEnabled || track.category == AudioCategory.MUSIC) && !track.isMuted

                AudioTrackStrip(
                    track = track,
                    effectiveVolume = effectiveVol,
                    isBeingDucked = isBeingDucked,
                    duckLevel = duckLevel,
                    onToggleDucking = { onToggleTrackDucking(track.id) },
                    onSetVolume = { vol, muted -> onSetTrackVolume(track.id, vol, muted) },
                    onSetPan = { pan -> onSetTrackPan(track.id, pan) },
                    onSetCategory = { cat -> onSetTrackCategory(track.id, cat) }
                )
            }
        }
    }
}

@Composable
fun VideoTrackAudioStrip(
    clip: VideoClip,
    onUpdateVolume: (volume: Float, isMuted: Boolean) -> Unit,
    onToggleSpeech: (Boolean) -> Unit = {}
) {
    var volume by remember(clip.volume) { mutableFloatStateOf(clip.volume) }
    var isMuted by remember(clip.isMuted) { mutableStateOf(clip.isMuted) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StudioCardDark,
        border = BorderStroke(1.dp, if (clip.hasSpeech) Color(0xFFFFB300).copy(alpha = 0.6f) else Color(0xFF2C2840)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_strip_video")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF3B82F6).copy(alpha = 0.25f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "V1 VIDEO",
                            color = Color(0xFF60A5FA),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Embedded Video Audio",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Speech / Dialogue detected switch button
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (clip.hasSpeech) Color(0xFFFFB300).copy(alpha = 0.25f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (clip.hasSpeech) Color(0xFFFFB300) else Color(0xFF334155)),
                        modifier = Modifier
                            .clickable {
                                onToggleSpeech(!clip.hasSpeech)
                            }
                            .testTag("toggle_video_speech_${clip.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Speech Detected",
                                tint = if (clip.hasSpeech) Color(0xFFFFB300) else Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (clip.hasSpeech) "SPEECH ON" else "+ SPEECH",
                                color = if (clip.hasSpeech) Color(0xFFFFB300) else Color.LightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Mute button
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isMuted) Color(0xCCEF4444) else Color(0xFF1E293B),
                        modifier = Modifier
                            .clickable {
                                isMuted = !isMuted
                                onUpdateVolume(volume, isMuted)
                            }
                            .testTag("mute_video_audio")
                    ) {
                        Text(
                            text = if (isMuted) "MUTED" else "MUTE",
                            color = if (isMuted) Color.White else Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Slider & VU Meter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )

                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        onUpdateVolume(volume, isMuted)
                    },
                    valueRange = 0f..2.0f,
                    enabled = !isMuted,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF60A5FA),
                        activeTrackColor = Color(0xFF60A5FA)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                )

                Text(
                    text = "${(volume * 100).toInt()}%",
                    color = if (isMuted) Color.Gray else Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
fun AudioTrackStrip(
    track: AudioTrackItem,
    effectiveVolume: Float,
    isBeingDucked: Boolean,
    duckLevel: Float,
    onToggleDucking: () -> Unit,
    onSetVolume: (volume: Float, isMuted: Boolean) -> Unit,
    onSetPan: (pan: Float) -> Unit,
    onSetCategory: (AudioCategory) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var volume by remember(track.volume) { mutableFloatStateOf(track.volume) }
    var pan by remember(track.pan) { mutableFloatStateOf(track.pan) }
    var isMuted by remember(track.isMuted) { mutableStateOf(track.isMuted) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isBeingDucked) Color(0xFF261D13) else StudioCardDark,
        border = BorderStroke(
            1.dp,
            if (isBeingDucked) Color(0xFFFFB300) else Color(track.accentColorHex).copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audio_strip_${track.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Track Metadata & Category Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Selector Pill
                    CategoryTag(
                        category = track.category,
                        onCycleCategory = {
                            val next = when (track.category) {
                                AudioCategory.MUSIC -> AudioCategory.VOICEOVER
                                AudioCategory.VOICEOVER -> AudioCategory.DIALOGUE
                                AudioCategory.DIALOGUE -> AudioCategory.SOUND_EFFECT
                                AudioCategory.SOUND_EFFECT -> AudioCategory.MUSIC
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSetCategory(next)
                        }
                    )

                    Text(
                        text = track.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(130.dp)
                    )
                }

                // Controls: Auto-Duck Toggle & Mute
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Ducking Trigger / Target indicator
                    if (track.category == AudioCategory.VOICEOVER || track.category == AudioCategory.DIALOGUE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x3300E5FF),
                            border = BorderStroke(0.5.dp, AccentCyan)
                        ) {
                            Text(
                                text = "SPEECH TRIGGER",
                                color = AccentCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        // Auto-Duck Toggle Button for BGM / SFX
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (track.isDuckingEnabled) Color(0xFFFFB300).copy(alpha = 0.25f) else Color(0xFF1E293B),
                            border = BorderStroke(
                                1.dp,
                                if (track.isDuckingEnabled) Color(0xFFFFB300) else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleDucking()
                                }
                                .testTag("ducking_toggle_${track.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (track.isDuckingEnabled) "DUCK ON SPEECH ✓" else "DUCK OFF",
                                    color = if (track.isDuckingEnabled) Color(0xFFFFB300) else Color.LightGray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Mute Button
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isMuted) Color(0xCCEF4444) else Color(0xFF1E293B),
                        modifier = Modifier
                            .clickable {
                                isMuted = !isMuted
                                onSetVolume(volume, isMuted)
                            }
                            .testTag("mute_track_${track.id}")
                    ) {
                        Text(
                            text = if (isMuted) "MUTED" else "M",
                            color = if (isMuted) Color.White else Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Volume Slider & Live Attenuated Level
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeMute else if (isBeingDucked) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isBeingDucked) Color(0xFFFFB300) else TrackAudioColor,
                    modifier = Modifier.size(16.dp)
                )

                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        onSetVolume(volume, isMuted)
                    },
                    valueRange = 0f..2.0f,
                    enabled = !isMuted,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isBeingDucked) Color(0xFFFFB300) else TrackAudioColor,
                        activeTrackColor = if (isBeingDucked) Color(0xFFFFB300) else TrackAudioColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .testTag("volume_slider_${track.id}")
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isBeingDucked) {
                        Text(
                            text = "➔ ${(effectiveVolume * 100).toInt()}%",
                            color = Color(0xFFFFB300),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stereo Pan Slider (L <-> R)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "L", color = Color.Gray, fontSize = 9.sp)
                Slider(
                    value = pan,
                    onValueChange = {
                        pan = it
                        onSetPan(pan)
                    },
                    valueRange = -1.0f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.LightGray,
                        activeTrackColor = Color.Gray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                )
                Text(text = "R", color = Color.Gray, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun CategoryTag(
    category: AudioCategory,
    onCycleCategory: () -> Unit
) {
    val (label, color, icon) = when (category) {
        AudioCategory.MUSIC -> Triple("MUSIC", TrackAudioColor, Icons.Default.MusicNote)
        AudioCategory.VOICEOVER -> Triple("VOICEOVER", AccentCyan, Icons.Default.RecordVoiceOver)
        AudioCategory.DIALOGUE -> Triple("DIALOGUE", Color(0xFF00E676), Icons.Default.Mic)
        AudioCategory.SOUND_EFFECT -> Triple("SFX", AccentPink, Icons.Default.Headphones)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = Modifier.clickable { onCycleCategory() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DuckingPresetChip(
    label: String,
    level: Float,
    current: Float,
    onClick: () -> Unit
) {
    val isSelected = kotlin.math.abs(current - level) < 0.04f
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) Color(0xFFFFB300) else Color(0xFF1E293B),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else Color.LightGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun LiveDuckingPulsar(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "ducking_pulsar")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsar_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(4) { idx ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((8 + idx * 3).dp)
                    .background(
                        if (isActive) Color(0xFFFFB300).copy(alpha = alpha) else Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

/**
 * Bottom Sheet container for the Audio Mixing & Auto-Ducking controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioMixingSheet(
    uiState: EditorUiState,
    onToggleAutoDuckingMaster: (Boolean) -> Unit,
    onSetAutoDuckingLevel: (Float) -> Unit,
    onToggleTrackDucking: (audioId: String) -> Unit,
    onSetTrackVolume: (audioId: String, volume: Float, isMuted: Boolean) -> Unit,
    onSetTrackPan: (audioId: String, pan: Float) -> Unit,
    onSetTrackCategory: (audioId: String, category: AudioCategory) -> Unit,
    onSetClipVolume: (clipId: String, volume: Float, isMuted: Boolean) -> Unit,
    onToggleClipSpeech: (clipId: String, hasSpeech: Boolean) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        AudioMixingControl(
            uiState = uiState,
            onToggleAutoDuckingMaster = onToggleAutoDuckingMaster,
            onSetAutoDuckingLevel = onSetAutoDuckingLevel,
            onToggleTrackDucking = onToggleTrackDucking,
            onSetTrackVolume = onSetTrackVolume,
            onSetTrackPan = onSetTrackPan,
            onSetTrackCategory = onSetTrackCategory,
            onSetClipVolume = onSetClipVolume,
            onToggleClipSpeech = onToggleClipSpeech,
            onClose = onDismiss
        )
    }
}
