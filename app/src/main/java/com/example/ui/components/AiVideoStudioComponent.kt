package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatio
import com.example.data.model.TextStylePreset
import com.example.data.model.VideoClip
import com.example.data.model.VideoFilter
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import com.example.viewmodel.EditorUiState

enum class AiStudioTab(val title: String, val icon: ImageVector) {
    SILENCE("Silence Removal", Icons.Default.VolumeOff),
    TRANSCRIPT("Transcript Edit", Icons.Default.Subtitles),
    MULTICAM("Multicam 4-Cam", Icons.Default.GridView),
    COPILOT("AI Copilot", Icons.Default.AutoAwesome),
    CAPTIONS("Auto Subtitles", Icons.Default.TextFields),
    REFRAME("Smart Reframe", Icons.Default.CenterFocusStrong),
    SCENE("Scene Detection", Icons.Default.MovieFilter),
    DENOISE("Voice Enhance", Icons.Default.RecordVoiceOver)
}

data class TranscriptLineItem(
    val id: String,
    val speaker: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val hasFillerWords: Boolean = false,
    val isCut: Boolean = false
)

/**
 * AI Magic Video Studio & Editing Assistant Component.
 * Implements AI Silence Removal (Auto Jump-Cut), Transcript-Based Video Editing,
 * 4-Angle Multicamera Switcher, AI Editing Copilot, Auto Subtitles,
 * Smart Reframing 16:9 to 9:16, AI Scene Cut Detection, and Voice Enhancement.
 */
@Composable
fun AiVideoStudioComponent(
    uiState: EditorUiState,
    onExecuteSilenceRemoval: (clipId: String, thresholdDb: Float) -> Unit,
    onGenerateAutoCaptions: (preset: TextStylePreset) -> Unit,
    onApplySmartReframe: (targetAspect: AspectRatio) -> Unit,
    onDetectSceneCuts: (clipId: String) -> Unit,
    onEnhanceVoice: (clipId: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onExecuteCopilotPrompt: (prompt: String) -> Unit = {},
    onSeekToTime: (Long) -> Unit = {},
    onRippleCutTranscript: (startMs: Long, endMs: Long, text: String) -> Unit = { _, _, _ -> },
    onRemoveFillerWords: () -> Unit = {},
    onSwitchMulticamAngle: (angleLabel: String, filter: VideoFilter) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(AiStudioTab.SILENCE) }
    val activeClip = uiState.selectedClip ?: uiState.clips.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfaceDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("ai_video_studio_component")
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
                    color = AccentCyan.copy(alpha = 0.25f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Magic Studio",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI Magic Studio & Transcript NLE",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Text-based ripple cuts • 4-Cam Multicam • Neural automation",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI Feature Navigation Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(AiStudioTab.entries.toList()) { tab ->
                val isSelected = selectedTab == tab
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) AccentCyan else StudioCardDark,
                    border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C3345)),
                    modifier = Modifier
                        .clickable { selectedTab = tab }
                        .testTag("ai_tab_${tab.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) Color.Black else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.title,
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Feature Content Pane
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F1523),
            border = BorderStroke(1.dp, Color(0xFF232B3E)),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            when (selectedTab) {
                AiStudioTab.SILENCE -> {
                    AiSilenceRemovalPane(
                        clip = activeClip,
                        onExecuteSilenceCut = { clipId, thresh ->
                            onExecuteSilenceRemoval(clipId, thresh)
                        }
                    )
                }
                AiStudioTab.TRANSCRIPT -> {
                    AiTranscriptEditorPane(
                        currentPositionMs = uiState.currentPositionMs,
                        onSeekToTime = onSeekToTime,
                        onRippleCutSegment = onRippleCutTranscript,
                        onRemoveFillerWords = onRemoveFillerWords
                    )
                }
                AiStudioTab.MULTICAM -> {
                    AiMulticamSwitcherPane(
                        currentPositionMs = uiState.currentPositionMs,
                        onSwitchAngle = onSwitchMulticamAngle
                    )
                }
                AiStudioTab.COPILOT -> {
                    AiCopilotAssistantPane(
                        onExecutePrompt = onExecuteCopilotPrompt
                    )
                }
                AiStudioTab.CAPTIONS -> {
                    AiAutoCaptionsPane(
                        currentCaptionCount = uiState.textOverlays.size,
                        onGenerate = onGenerateAutoCaptions
                    )
                }
                AiStudioTab.REFRAME -> {
                    AiSmartReframePane(
                        currentAspect = uiState.aspectRatio,
                        onApplyReframe = onApplySmartReframe
                    )
                }
                AiStudioTab.SCENE -> {
                    AiSceneDetectionPane(
                        clip = activeClip,
                        onDetect = { clipId -> onDetectSceneCuts(clipId) }
                    )
                }
                AiStudioTab.DENOISE -> {
                    AiVoiceEnhancePane(
                        clip = activeClip,
                        onEnhance = { clipId -> onEnhanceVoice(clipId) }
                    )
                }
            }
        }
    }
}

/**
 * Interactive Transcript-Based Video Editing Pane:
 * Tap spoken phrases to seek playhead, delete sentences to ripple-cut the timeline, or strip filler words ("um", "uh").
 */
@Composable
fun AiTranscriptEditorPane(
    currentPositionMs: Long,
    onSeekToTime: (Long) -> Unit,
    onRippleCutSegment: (startMs: Long, endMs: Long, text: String) -> Unit,
    onRemoveFillerWords: () -> Unit
) {
    val transcriptLines = remember {
        mutableStateListOf(
            TranscriptLineItem("t1", "Host", 0L, 2800L, "Welcome back to OpenCut Studio Pro — today we're cutting a cyberpunk reel."),
            TranscriptLineItem("t2", "Host", 2800L, 4600L, "Um... uh... [awkward pause] let me adjust the key light real quick.", hasFillerWords = true),
            TranscriptLineItem("t3", "Host", 4600L, 8200L, "Watch how color LUTs and audio ducking lock automatically to the voiceover."),
            TranscriptLineItem("t4", "Guest", 8200L, 11500L, "And you can delete any sentence right here to ripple-cut the video timeline!")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_transcript_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Text-Based Video Editing (Transcript Sync)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any line to jump playhead • Tap ✂ to ripple-delete spoken segment",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AccentPink.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, AccentPink),
                modifier = Modifier
                    .clickable {
                        transcriptLines.replaceAll {
                            if (it.hasFillerWords) it.copy(isCut = true) else it
                        }
                        onRemoveFillerWords()
                    }
                    .testTag("strip_filler_words_button")
            ) {
                Text(
                    text = "🪄 Strip 'Um / Uh'",
                    color = AccentPink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(transcriptLines, key = { _, item -> item.id }) { idx, line ->
                val isActive = currentPositionMs in line.startMs..line.endMs
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        line.isCut -> Color(0xFF1F1624)
                        isActive -> Color(0xFF162C3D)
                        else -> StudioCardDark
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            line.isCut -> Color(0xFFEF4444).copy(alpha = 0.4f)
                            isActive -> AccentCyan
                            line.hasFillerWords -> Color(0xFFFFB300).copy(alpha = 0.6f)
                            else -> Color(0xFF283249)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSeekToTime(line.startMs) }
                        .testTag("transcript_line_$idx")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${String.format("%.1f", line.startMs / 1000f)}s • ${line.speaker}",
                                    color = if (isActive) AccentCyan else Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                if (line.hasFillerWords && !line.isCut) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = Color(0xFFFFB300).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "FILLER DETECTED",
                                            color = Color(0xFFFFB300),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = line.text,
                                color = if (line.isCut) Color.Gray else Color.White,
                                fontSize = 11.sp,
                                textDecoration = if (line.isCut) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                if (!line.isCut) {
                                    transcriptLines[idx] = line.copy(isCut = true)
                                    onRippleCutSegment(line.startMs, line.endMs, line.text.take(24))
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("transcript_delete_segment_$idx")
                        ) {
                            Icon(
                                imageVector = if (line.isCut) Icons.Default.Delete else Icons.Default.ContentCut,
                                contentDescription = "Ripple Cut Phrase",
                                tint = if (line.isCut) Color.Gray else Color(0xFFEF4444),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4-Angle Multicamera Live Switcher & Audio Clapper Sync Pane.
 */
@Composable
fun AiMulticamSwitcherPane(
    currentPositionMs: Long,
    onSwitchAngle: (angleLabel: String, filter: VideoFilter) -> Unit
) {
    var activeAngleIdx by remember { mutableStateOf(0) }
    val angles = listOf(
        Triple("Cam 1: Wide Master", VideoFilter.NONE, Color(0xFF00E5FF)),
        Triple("Cam 2: Host Close-Up", VideoFilter.WARM_DRAMA, Color(0xFFFF4081)),
        Triple("Cam 3: Top-Down Desk", VideoFilter.CYBERPUNK, Color(0xFF10B981)),
        Triple("Cam 4: Guest Reaction", VideoFilter.CINEMATIC, Color(0xFFFFB300))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_multicam_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "4-Camera Multicam Live Switcher",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Audio waveform clapper synced • Tap any camera monitor to cut at playhead",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AccentEmerald.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "⚡ AUDIO SYNCED",
                    color = AccentEmerald,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                angles.take(2).forEachIndexed { idx, (label, filter, accent) ->
                    val isSelected = activeAngleIdx == idx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accent.copy(alpha = 0.22f) else StudioCardDark,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else Color(0xFF2D3748)),
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp)
                            .clickable {
                                activeAngleIdx = idx
                                onSwitchAngle(label, filter)
                            }
                            .testTag("multicam_angle_card_$idx")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CAM ${idx + 1}",
                                    color = accent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isSelected) {
                                    Surface(shape = RoundedCornerShape(3.dp), color = Color(0xFFEF4444)) {
                                        Text(
                                            text = "PGM LIVE",
                                            color = Color.White,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                angles.drop(2).forEachIndexed { offsetIdx, (label, filter, accent) ->
                    val idx = offsetIdx + 2
                    val isSelected = activeAngleIdx == idx
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) accent.copy(alpha = 0.22f) else StudioCardDark,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else Color(0xFF2D3748)),
                        modifier = Modifier
                            .weight(1f)
                            .height(74.dp)
                            .clickable {
                                activeAngleIdx = idx
                                onSwitchAngle(label, filter)
                            }
                            .testTag("multicam_angle_card_$idx")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CAM ${idx + 1}",
                                    color = accent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isSelected) {
                                    Surface(shape = RoundedCornerShape(3.dp), color = Color(0xFFEF4444)) {
                                        Text(
                                            text = "PGM LIVE",
                                            color = Color.White,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1. AI Silence Removal & Auto Jump-Cut Pane.
 */
@Composable
fun AiSilenceRemovalPane(
    clip: VideoClip?,
    onExecuteSilenceCut: (clipId: String, thresholdDb: Float) -> Unit
) {
    var thresholdDb by remember { mutableFloatStateOf(-32f) }
    var minSilenceMs by remember { mutableFloatStateOf(400f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_silence_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Silence Removal (Auto Jump-Cut)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "1.8s silence detected (3 pauses)",
                        color = Color(0xFF34D399),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Scans speech audio waveforms to detect dead air and automatically ripple-deletes awkward pauses for fast-paced YouTube & TikTok edits.",
                color = Color.LightGray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Threshold Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Silence Threshold", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("${thresholdDb.toInt()} dB", color = AccentCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = thresholdDb,
                onValueChange = { thresholdDb = it },
                valueRange = -45f..-20f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                modifier = Modifier.height(24.dp)
            )

            // Minimum Silence Duration Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Minimum Pause Duration", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("${minSilenceMs.toInt()} ms", color = Color(0xFFFFB300), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = minSilenceMs,
                onValueChange = { minSilenceMs = it },
                valueRange = 200f..1000f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFFB300), activeTrackColor = Color(0xFFFFB300)),
                modifier = Modifier.height(24.dp)
            )
        }

        Button(
            onClick = {
                clip?.let { onExecuteSilenceCut(it.id, thresholdDb) }
            },
            enabled = clip != null,
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("execute_silence_removal_button")
        ) {
            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Ripple-Cut Silences & Save 1.8s",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 2. AI Auto Subtitles & Speech-to-Text Generator Pane.
 */
@Composable
fun AiAutoCaptionsPane(
    currentCaptionCount: Int,
    onGenerate: (TextStylePreset) -> Unit
) {
    var selectedPreset by remember { mutableStateOf(TextStylePreset.NEON_GLOW) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_captions_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "AI Auto-Subtitles & Speech-to-Text",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Automatically transcribes spoken dialogue into synchronized, styled animated subtitle overlays across the video timeline.",
                color = Color.LightGray,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Select Subtitle Style Template:",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Presets Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TextStylePreset.entries.toList()) { preset ->
                    val isSel = selectedPreset == preset
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) Color(0xFF281C38) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSel) AccentPink else Color(0xFF333E54)),
                        modifier = Modifier
                            .clickable { selectedPreset = preset }
                            .testTag("caption_preset_${preset.name}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Text(
                                text = preset.styleName,
                                color = if (isSel) AccentPink else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Preview Text",
                                color = Color.Gray,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onGenerate(selectedPreset) },
            colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_auto_subtitles_button")
        ) {
            Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Generate Synchronized Subtitles (3 Tracks)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 3. AI Smart Reframing Pane (16:9 ➔ 9:16 Shorts/Reels).
 */
@Composable
fun AiSmartReframePane(
    currentAspect: AspectRatio,
    onApplyReframe: (AspectRatio) -> Unit
) {
    var targetAspect by remember { mutableStateOf(AspectRatio.VERTICAL_9_16) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_reframe_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "AI Smart Reframing & Auto-Tracking",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Intelligently identifies focal subjects (faces & motion action) to dynamically pan & convert landscape videos into viral vertical reels.",
                color = Color.LightGray,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Target Social Format:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AspectRatio.VERTICAL_9_16 to "9:16 Shorts / Reels",
                    AspectRatio.SQUARE_1_1 to "1:1 Instagram Post",
                    AspectRatio.LANDSCAPE_16_9 to "16:9 YouTube"
                ).forEach { (aspect, label) ->
                    val isSel = targetAspect == aspect
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) Color(0xFFFFB300) else StudioCardDark,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { targetAspect = aspect }
                            .testTag("reframe_aspect_${aspect.name}")
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color.Black else Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onApplyReframe(targetAspect) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("apply_smart_reframe_button")
        ) {
            Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Apply Smart Reframing (${targetAspect.label})",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 4. AI Scene Detection & Splitter Pane.
 */
@Composable
fun AiSceneDetectionPane(
    clip: VideoClip?,
    onDetect: (clipId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_scene_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "AI Scene Cut Detection",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scans frame-by-frame visual changes across the video clip to automatically detect camera cuts, angle switches, and split into individual clips with timeline cue markers.",
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }

        Button(
            onClick = { clip?.let { onDetect(it.id) } },
            enabled = clip != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("detect_scene_cuts_button")
        ) {
            Icon(Icons.Default.MovieFilter, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Detect & Split Scenes at Camera Cuts",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 5. AI Voice Enhance & Audio Denoise Pane.
 */
@Composable
fun AiVoiceEnhancePane(
    clip: VideoClip?,
    onEnhance: (clipId: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_voice_enhance_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "AI Voice Enhancement & Denoise",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Neural spectral subtraction removes AC hum, wind hiss, and room reverberation, while boosting speech formant clarity for studio-podcast quality.",
                color = Color.LightGray,
                fontSize = 10.sp
            )
        }

        Button(
            onClick = { clip?.let { onEnhance(it.id) } },
            enabled = clip != null,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("enhance_voice_button")
        ) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Enhance Voice & Remove Background Noise",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 6. AI Editing Copilot (Natural Language Prompt & 1-Tap Magic Recipes).
 */
@Composable
fun AiCopilotAssistantPane(
    onExecutePrompt: (prompt: String) -> Unit
) {
    var customPrompt by remember { mutableStateOf("Fast TikTok viral cut with neon captions, beat sync & teal-orange LUT") }
    val recipes = listOf(
        "⚡ TikTok Viral Edit" to "Fast TikTok viral cut with neon captions, beat sync & teal-orange LUT",
        "🎬 Cinema Documentary" to "Hollywood documentary grade with voice ducking, crossfades & letterbox",
        "🎙 Podcast Pro Clean" to "Remove silence pauses, enhance voice clarity & generate clean subtitles",
        "🔥 Music Beat Montage" to "Sync cuts to music beats, add speed ramp & cyberpunk glow effects"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("ai_copilot_pane"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "AI Editing Copilot — Describe Your Edit",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap a 1-click recipe or describe your desired timeline style in natural language:",
                color = Color.LightGray,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(recipes) { (badge, fullPrompt) ->
                    val isSelected = customPrompt == fullPrompt
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color(0xFF2C3345)),
                        modifier = Modifier
                            .clickable { customPrompt = fullPrompt }
                            .testTag("copilot_recipe_${badge.takeLast(10).trim().replace(" ", "_").lowercase()}")
                    ) {
                        Text(
                            text = badge,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF161F33),
                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "“$customPrompt”",
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Button(
            onClick = { onExecutePrompt(customPrompt) },
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("execute_ai_copilot_button")
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Run AI Copilot Auto-Edit on Timeline",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Bottom Sheet container for AiVideoStudioComponent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVideoStudioSheet(
    uiState: EditorUiState,
    onExecuteSilenceRemoval: (clipId: String, thresholdDb: Float) -> Unit,
    onGenerateAutoCaptions: (preset: TextStylePreset) -> Unit,
    onApplySmartReframe: (targetAspect: AspectRatio) -> Unit,
    onDetectSceneCuts: (clipId: String) -> Unit,
    onEnhanceVoice: (clipId: String) -> Unit,
    onDismiss: () -> Unit,
    onExecuteCopilotPrompt: (prompt: String) -> Unit = {},
    onSeekToTime: (Long) -> Unit = {},
    onRippleCutTranscript: (startMs: Long, endMs: Long, text: String) -> Unit = { _, _, _ -> },
    onRemoveFillerWords: () -> Unit = {},
    onSwitchMulticamAngle: (angleLabel: String, filter: VideoFilter) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        AiVideoStudioComponent(
            uiState = uiState,
            onExecuteSilenceRemoval = onExecuteSilenceRemoval,
            onGenerateAutoCaptions = onGenerateAutoCaptions,
            onApplySmartReframe = onApplySmartReframe,
            onDetectSceneCuts = onDetectSceneCuts,
            onEnhanceVoice = onEnhanceVoice,
            onClose = onDismiss,
            onExecuteCopilotPrompt = onExecuteCopilotPrompt,
            onSeekToTime = onSeekToTime,
            onRippleCutTranscript = onRippleCutTranscript,
            onRemoveFillerWords = onRemoveFillerWords,
            onSwitchMulticamAngle = onSwitchMulticamAngle
        )
    }
}
