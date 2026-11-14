package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioSurfaceDark

@Composable
fun EditToolstrip(
    hasSelectedClip: Boolean,
    hasSelectedAudio: Boolean,
    hasSelectedText: Boolean,
    hasSelectedSticker: Boolean,
    hasSelectedShape: Boolean = false,
    onSplit: () -> Unit,
    onTrim: () -> Unit,
    onSpeed: () -> Unit,
    onFilter: () -> Unit,
    onTransition: () -> Unit,
    onRotate: () -> Unit,
    onFlip: () -> Unit,
    onAudio: () -> Unit,
    onDetachAudio: () -> Unit,
    onText: () -> Unit,
    onStickers: () -> Unit,
    onShapes: () -> Unit = {},
    onStabilize: () -> Unit = {},
    onAddMusic: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onFreeze: () -> Unit = {},
    onReverse: () -> Unit = {},
    onCanvasBg: () -> Unit = {},
    onKeyframe: () -> Unit = {},
    onChromaKey: () -> Unit = {},
    onBeatSync: () -> Unit = {},
    onAudioDucking: () -> Unit = {},
    onEffects: () -> Unit = {},
    onRazor: () -> Unit = {},
    onOpenShotTitles: () -> Unit = {},
    onToggleSnapping: () -> Unit = {},
    onPrecisionScrubber: () -> Unit = {},
    onColorGrade: () -> Unit = {},
    onAiMagic: () -> Unit = {},
    isReversed: Boolean = false,
    hasKeyframe: Boolean = false,
    isChromaKeyActive: Boolean = false,
    isBeatSyncActive: Boolean = false,
    isDuckingActive: Boolean = false,
    isStabilized: Boolean = false,
    isSnappingActive: Boolean = true,
    hasAppliedEffects: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = Color(0xFF131B2A),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-Performance Precision Frame Scrubber & Jog Dial
            ToolButton(
                icon = Icons.Default.Speed,
                label = "Scrubber",
                accentColor = AccentCyan,
                enabled = hasSelectedClip,
                testTag = "tool_precision_scrubber",
                onClick = onPrecisionScrubber
            )

            // OpenShot Razor / Slicing Tools
            ToolButton(
                icon = Icons.Default.ContentCut,
                label = "Razor",
                accentColor = AccentPink,
                enabled = true,
                testTag = "tool_razor",
                onClick = onRazor
            )

            // OpenShot Video Effects Rack
            ToolButton(
                icon = Icons.Default.AutoAwesome,
                label = if (hasAppliedEffects) "FX Active" else "Effects",
                accentColor = if (hasAppliedEffects) Color(0xFF00E5FF) else Color.White,
                enabled = hasSelectedClip,
                testTag = "tool_effects",
                onClick = onEffects
            )

            // OpenShot 3D & 2D Animated Titles
            ToolButton(
                icon = Icons.Default.TextFields,
                label = "3D Titles",
                accentColor = Color(0xFFFFB300),
                enabled = true,
                testTag = "tool_titles_3d",
                onClick = onOpenShotTitles
            )

            // OpenShot Magnet Snapping
            ToolButton(
                icon = Icons.Default.Hub,
                label = if (isSnappingActive) "Magnet ON" else "Magnet",
                accentColor = if (isSnappingActive) AccentCyan else Color.Gray,
                enabled = true,
                testTag = "tool_magnet_snapping",
                onClick = onToggleSnapping
            )

            // Core Split Action
            ToolButton(
                icon = Icons.Default.ContentCut,
                label = "Split",
                accentColor = AccentPink,
                enabled = hasSelectedClip,
                testTag = "tool_split",
                onClick = onSplit
            )

            // Speed
            ToolButton(
                icon = Icons.Default.Speed,
                label = "Speed",
                enabled = hasSelectedClip,
                testTag = "tool_speed",
                onClick = onSpeed
            )

            // Filters & Color Grading
            ToolButton(
                icon = Icons.Default.Palette,
                label = "Filter",
                enabled = hasSelectedClip,
                testTag = "tool_filters",
                onClick = onFilter
            )

            // Chroma Key / Green Screen
            ToolButton(
                icon = Icons.Default.Layers,
                label = if (isChromaKeyActive) "Keyed" else "Chroma Key",
                accentColor = if (isChromaKeyActive) Color(0xFF10B981) else Color.White,
                enabled = hasSelectedClip,
                testTag = "tool_chroma_key",
                onClick = onChromaKey
            )

            // Hardware-Accelerated Video Stabilization
            ToolButton(
                icon = Icons.Default.Videocam,
                label = if (isStabilized) "Stabilized" else "Stabilize",
                accentColor = if (isStabilized) AccentCyan else Color.White,
                enabled = hasSelectedClip,
                testTag = "tool_stabilize",
                onClick = onStabilize
            )

            // Professional Color Grading Studio (DaVinci Wheels, Scopes & LUTs)
            ToolButton(
                icon = Icons.Default.Palette,
                label = "Color",
                accentColor = Color(0xFFFFB300),
                enabled = true,
                testTag = "tool_color_grade",
                onClick = onColorGrade
            )

            // AI Magic Studio (Silence Removal, Auto Subtitles, Smart Reframe)
            ToolButton(
                icon = Icons.Default.AutoAwesome,
                label = "AI Magic",
                accentColor = AccentCyan,
                enabled = true,
                testTag = "tool_ai_magic",
                onClick = onAiMagic
            )

            // Vector Shapes & Styling
            ToolButton(
                icon = Icons.Default.Category,
                label = "Shapes",
                accentColor = Color(0xFFC084FC),
                enabled = true,
                testTag = "tool_shapes",
                onClick = onShapes
            )

            // Audio Ducking (lowers background music during speech)
            ToolButton(
                icon = Icons.Default.GraphicEq,
                label = if (isDuckingActive) "Ducking ON" else "Ducking",
                accentColor = if (isDuckingActive) Color(0xFFF59E0B) else Color.White,
                enabled = true,
                testTag = "tool_ducking",
                onClick = onAudioDucking
            )

            // Auto Beat Sync
            ToolButton(
                icon = Icons.Default.GraphicEq,
                label = if (isBeatSyncActive) "Beat Synced" else "Beat Sync",
                accentColor = if (isBeatSyncActive) Color(0xFFFBBF24) else Color.White,
                enabled = hasSelectedAudio,
                testTag = "tool_beat_sync",
                onClick = onBeatSync
            )

            // Lottie Animated Badges / Effects
            ToolButton(
                icon = Icons.Default.AutoAwesome,
                label = "Lottie FX",
                accentColor = Color(0xFF8B5CF6),
                enabled = true,
                testTag = "tool_lottie_fx",
                onClick = onStickers
            )

            // Trim
            ToolButton(
                icon = Icons.Default.Crop,
                label = "Trim",
                enabled = hasSelectedClip,
                testTag = "tool_trim",
                onClick = onTrim
            )

            // Transitions
            ToolButton(
                icon = Icons.Default.Transform,
                label = "Transition",
                enabled = true,
                testTag = "tool_transition",
                onClick = onTransition
            )

            // Audio & Detach (LibreCuts signature)
            ToolButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = "Audio",
                enabled = hasSelectedClip || hasSelectedAudio,
                testTag = "tool_audio",
                onClick = onAudio
            )

            if (hasSelectedClip) {
                ToolButton(
                    icon = Icons.Default.Audiotrack,
                    label = "Detach",
                    accentColor = AccentCyan,
                    enabled = true,
                    testTag = "tool_detach_audio",
                    onClick = onDetachAudio
                )
            }

            // Rotate
            ToolButton(
                icon = Icons.Default.RotateRight,
                label = "Rotate",
                enabled = hasSelectedClip,
                testTag = "tool_rotate",
                onClick = onRotate
            )

            // Flip
            ToolButton(
                icon = Icons.Default.Flip,
                label = "Flip",
                enabled = hasSelectedClip,
                testTag = "tool_flip",
                onClick = onFlip
            )

            // Freeze Frame (LibreCuts & OpenCut)
            ToolButton(
                icon = Icons.Default.PauseCircle,
                label = "Freeze",
                accentColor = Color(0xFFFFD700),
                enabled = hasSelectedClip,
                testTag = "tool_freeze",
                onClick = onFreeze
            )

            // Reverse Playback (OpenCut)
            ToolButton(
                icon = Icons.Default.FastRewind,
                label = if (isReversed) "Reversed" else "Reverse",
                accentColor = if (isReversed) AccentCyan else Color.White,
                enabled = hasSelectedClip,
                testTag = "tool_reverse",
                onClick = onReverse
            )

            // OpenShot Keyframe Property Editor
            ToolButton(
                icon = Icons.Default.Timeline,
                label = if (hasKeyframe) "Keyframes ●" else "Keyframes",
                accentColor = if (hasKeyframe) AccentCyan else Color.White,
                enabled = hasSelectedClip,
                testTag = "tool_keyframe",
                onClick = onKeyframe
            )

            // Canvas Background Mode (OpenCut canvas blur)
            ToolButton(
                icon = Icons.Default.Wallpaper,
                label = "Canvas Bg",
                enabled = hasSelectedClip,
                testTag = "tool_canvas_bg",
                onClick = onCanvasBg
            )

            // Add Text
            ToolButton(
                icon = Icons.Default.TextFields,
                label = "Text",
                accentColor = Color(0xFFFFB300),
                enabled = true,
                testTag = "tool_text",
                onClick = onText
            )

            // Add Stickers
            ToolButton(
                icon = Icons.Default.AutoAwesome,
                label = "Stickers",
                accentColor = AccentCyan,
                enabled = true,
                testTag = "tool_stickers",
                onClick = onStickers
            )

            // Add Music / SFX
            ToolButton(
                icon = Icons.Default.MusicNote,
                label = "Music",
                accentColor = Color(0xFF00E676),
                enabled = true,
                testTag = "tool_music",
                onClick = onAddMusic
            )

            // Reorder Left / Right
            if (hasSelectedClip) {
                ToolButton(
                    icon = Icons.Default.ArrowBack,
                    label = "Move L",
                    enabled = true,
                    testTag = "tool_move_left",
                    onClick = onMoveLeft
                )
                ToolButton(
                    icon = Icons.Default.ArrowForward,
                    label = "Move R",
                    enabled = true,
                    testTag = "tool_move_right",
                    onClick = onMoveRight
                )
                ToolButton(
                    icon = Icons.Default.ContentCopy,
                    label = "Duplicate",
                    enabled = true,
                    testTag = "tool_duplicate",
                    onClick = onDuplicate
                )
            }

            // Delete selected item
            val canDelete = hasSelectedClip || hasSelectedAudio || hasSelectedText || hasSelectedSticker || hasSelectedShape
            ToolButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                accentColor = Color(0xFFFF5252),
                enabled = canDelete,
                testTag = "tool_delete",
                onClick = onDelete
            )
        }
    }
}

@Composable
fun ToolButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    accentColor: Color = Color.White,
    testTag: String = "",
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val tint = if (enabled) accentColor else Color.Gray.copy(alpha = 0.5f)
    val textTint = if (enabled) Color.White.copy(alpha = 0.9f) else Color.Gray.copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(width = 62.dp, height = 56.dp)
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = textTint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
