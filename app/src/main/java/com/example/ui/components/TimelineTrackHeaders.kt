package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrackType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.TrackAudioColor
import com.example.ui.theme.TrackOverlayColor
import com.example.ui.theme.TrackTextColor
import com.example.ui.theme.TrackVideoColor

/**
 * Stationary Pinned Left Track Headers Column.
 * Stays stationary while the track lanes scroll horizontally to the right.
 * Provides professional NLE track identification (V1, A1, OV1, SH1, T1),
 * track visibility/mute toggles, lock toggles, and cue marker pin trigger.
 */
@Composable
fun TrackHeadersColumn(
    isVideoMuted: Boolean,
    isVideoLocked: Boolean,
    isAudioMuted: Boolean,
    isAudioLocked: Boolean,
    isStickerVisible: Boolean,
    isStickerLocked: Boolean,
    isShapeVisible: Boolean,
    isShapeLocked: Boolean,
    isTextVisible: Boolean,
    isTextLocked: Boolean,
    hasShapes: Boolean,
    onToggleVisibility: (TrackType) -> Unit,
    onToggleLock: (TrackType) -> Unit,
    onAddCueMarker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .background(Color(0xFF0F111A))
            .border(width = 0.5.dp, color = Color(0xFF1E2433))
            .testTag("track_headers_column")
    ) {
        // 1. Top Ruler Corner (Height 28.dp matching TimelineRuler)
        Surface(
            color = Color(0xFF161426),
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .testTag("track_headers_ruler_corner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TRACKS",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )

                // Quick Add Cue Marker Button
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = AccentCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddCueMarker()
                        }
                        .testTag("add_cue_marker_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Add Cue Marker",
                            tint = AccentCyan,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "+CUE",
                            color = AccentCyan,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. V1 Video Track Header (Height 64.dp matching VideoTrackLane)
        TrackHeaderCell(
            trackLabel = "V1",
            trackName = "Video",
            accentColor = TrackVideoColor,
            icon = Icons.Default.Movie,
            heightDp = 64,
            isMutedOrHidden = isVideoMuted,
            isLocked = isVideoLocked,
            onToggleVisibility = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleVisibility(TrackType.VIDEO)
            },
            onToggleLock = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleLock(TrackType.VIDEO)
            },
            visibilityTag = "track_v1_visibility_toggle",
            lockTag = "track_v1_lock_toggle",
            modifier = Modifier.testTag("track_v1_header")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. A1 Audio Track Header (Height 48.dp matching AudioTrackLane)
        TrackHeaderCell(
            trackLabel = "A1",
            trackName = "Audio",
            accentColor = TrackAudioColor,
            icon = Icons.Default.GraphicEq,
            heightDp = 48,
            isMutedOrHidden = isAudioMuted,
            isLocked = isAudioLocked,
            isAudioTrack = true,
            onToggleVisibility = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleVisibility(TrackType.AUDIO)
            },
            onToggleLock = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleLock(TrackType.AUDIO)
            },
            visibilityTag = "track_a1_visibility_toggle",
            lockTag = "track_a1_lock_toggle",
            modifier = Modifier.testTag("track_a1_header")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 4. OV1 Sticker Track Header (Height 48.dp matching StickerTrackLane)
        TrackHeaderCell(
            trackLabel = "OV1",
            trackName = "Lottie",
            accentColor = TrackOverlayColor,
            icon = Icons.Default.AutoAwesome,
            heightDp = 48,
            isMutedOrHidden = !isStickerVisible,
            isLocked = isStickerLocked,
            onToggleVisibility = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleVisibility(TrackType.OVERLAY)
            },
            onToggleLock = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleLock(TrackType.OVERLAY)
            },
            visibilityTag = "track_ov1_visibility_toggle",
            lockTag = "track_ov1_lock_toggle",
            modifier = Modifier.testTag("track_ov1_header")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 5. SH1 Shape Track Header (Height 48.dp if shapes present)
        if (hasShapes) {
            TrackHeaderCell(
                trackLabel = "SH1",
                trackName = "Shape",
                accentColor = Color(0xFFA855F7),
                icon = Icons.Default.Category,
                heightDp = 48,
                isMutedOrHidden = !isShapeVisible,
                isLocked = isShapeLocked,
                onToggleVisibility = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleVisibility(TrackType.SHAPE)
                },
                onToggleLock = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleLock(TrackType.SHAPE)
                },
                visibilityTag = "track_sh1_visibility_toggle",
                lockTag = "track_sh1_lock_toggle",
                modifier = Modifier.testTag("track_sh1_header")
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 6. T1 Text Track Header (Height 48.dp matching TextTrackLane)
        TrackHeaderCell(
            trackLabel = "T1",
            trackName = "Text",
            accentColor = TrackTextColor,
            icon = Icons.Default.TextFields,
            heightDp = 48,
            isMutedOrHidden = !isTextVisible,
            isLocked = isTextLocked,
            onToggleVisibility = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggleVisibility(TrackType.TEXT)
            },
            onToggleLock = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleLock(TrackType.TEXT)
            },
            visibilityTag = "track_t1_visibility_toggle",
            lockTag = "track_t1_lock_toggle",
            modifier = Modifier.testTag("track_t1_header")
        )
    }
}

@Composable
fun TrackHeaderCell(
    trackLabel: String,
    trackName: String,
    accentColor: Color,
    icon: ImageVector,
    heightDp: Int,
    isMutedOrHidden: Boolean,
    isLocked: Boolean,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    visibilityTag: String,
    lockTag: String,
    isAudioTrack: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isLocked) Color(0xFF13151F) else Color(0xFF141824),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23293D)),
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(if (isMutedOrHidden) accentColor.copy(alpha = 0.3f) else accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Track Label & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = trackName,
                        tint = if (isMutedOrHidden) Color.Gray else accentColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = trackLabel,
                        color = if (isMutedOrHidden) Color.Gray else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = trackName,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontSize = 7.sp
                    )
                }

                // Controls: Visibility/Mute + Padlock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Visibility/Mute button
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = onToggleVisibility)
                            .testTag(visibilityTag),
                        contentAlignment = Alignment.Center
                    ) {
                        val visIcon = when {
                            isAudioTrack && isMutedOrHidden -> Icons.Default.VolumeMute
                            isAudioTrack -> Icons.Default.VolumeUp
                            isMutedOrHidden -> Icons.Default.VisibilityOff
                            else -> Icons.Default.Visibility
                        }
                        Icon(
                            imageVector = visIcon,
                            contentDescription = if (isMutedOrHidden) "Unmute/Show Track" else "Mute/Hide Track",
                            tint = if (isMutedOrHidden) Color(0xFFEF4444) else Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Padlock Lock button
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = onToggleLock)
                            .testTag(lockTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = if (isLocked) "Unlock Track" else "Lock Track",
                            tint = if (isLocked) Color(0xFFFBBF24) else Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
