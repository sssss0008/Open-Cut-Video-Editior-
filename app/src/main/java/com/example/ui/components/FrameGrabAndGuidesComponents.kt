package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CompositionGuideMode
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioSurfaceDark
import java.io.File

/**
 * Non-destructive framing & social safe-zone overlay drawn over the preview monitor.
 * Never alters exported pixels — helps creators align subjects & captions cleanly.
 */
@Composable
fun CompositionGuideOverlay(
    mode: CompositionGuideMode,
    modifier: Modifier = Modifier
) {
    if (mode == CompositionGuideMode.OFF) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("composition_guide_overlay_${mode.name.lowercase()}")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (mode) {
                CompositionGuideMode.RULE_OF_THIRDS -> {
                    val lineColor = Color.White.copy(alpha = 0.45f)
                    val strokePx = 1.dp.toPx()
                    // Vertical thirds
                    drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = strokePx)
                    drawLine(lineColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = strokePx)
                    // Horizontal thirds
                    drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = strokePx)
                    drawLine(lineColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = strokePx)
                    // Center crosshair
                    val cross = 8.dp.toPx()
                    drawLine(AccentCyan.copy(alpha = 0.85f), Offset(w / 2f - cross, h / 2f), Offset(w / 2f + cross, h / 2f), strokeWidth = 1.5.dp.toPx())
                    drawLine(AccentCyan.copy(alpha = 0.85f), Offset(w / 2f, h / 2f - cross), Offset(w / 2f, h / 2f + cross), strokeWidth = 1.5.dp.toPx())
                }

                CompositionGuideMode.SOCIAL_SAFE_ZONE -> {
                    // Danger areas matching TikTok / Instagram Reels / YouTube Shorts overlays
                    val dangerFill = Color(0x40EF4444)
                    val topDangerH = h * 0.11f
                    val bottomDangerH = h * 0.22f
                    val rightDangerW = w * 0.16f

                    // Top bar zone
                    drawRect(dangerFill, topLeft = Offset.Zero, size = Size(w, topDangerH))
                    // Bottom caption & music bar zone
                    drawRect(dangerFill, topLeft = Offset(0f, h - bottomDangerH), size = Size(w, bottomDangerH))
                    // Right engagement buttons zone (like / comment / share)
                    drawRect(
                        dangerFill,
                        topLeft = Offset(w - rightDangerW, topDangerH),
                        size = Size(rightDangerW, h - topDangerH - bottomDangerH)
                    )
                    // Safe center boundary box
                    drawRect(
                        color = AccentEmerald.copy(alpha = 0.85f),
                        topLeft = Offset(w * 0.04f, topDangerH),
                        size = Size(w - rightDangerW - w * 0.04f, h - topDangerH - bottomDangerH),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    )
                }

                CompositionGuideMode.TITLE_SAFE -> {
                    // Broadcast Action Safe (90%) and Title Safe (80%)
                    val actionInsetX = w * 0.05f
                    val actionInsetY = h * 0.05f
                    val titleInsetX = w * 0.10f
                    val titleInsetY = h * 0.10f
                    drawRect(
                        color = AccentCyan.copy(alpha = 0.7f),
                        topLeft = Offset(actionInsetX, actionInsetY),
                        size = Size(w - 2 * actionInsetX, h - 2 * actionInsetY),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                    drawRect(
                        color = Color(0xFFF59E0B).copy(alpha = 0.85f),
                        topLeft = Offset(titleInsetX, titleInsetY),
                        size = Size(w - 2 * titleInsetX, h - 2 * titleInsetY),
                        style = Stroke(
                            width = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )
                }

                CompositionGuideMode.OFF -> Unit
            }
        }

        // Subtle label pill at bottom-left of viewport
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xBB090A0F),
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.45f)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = mode.label,
                color = AccentCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Displays a newly captured HD PNG still frame with instant Save (SAF), Share,
 * and Add-to-Timeline Overlay actions.
 */
@Composable
fun CapturedFramePreviewDialog(
    frameFile: File,
    onSaveToDevice: () -> Unit,
    onShare: () -> Unit,
    onAddAsImageOverlay: () -> Unit,
    onDismiss: () -> Unit
) {
    val decodedBitmap = remember(frameFile.absolutePath) {
        runCatching { BitmapFactory.decodeFile(frameFile.absolutePath) }.getOrNull()
    }
    val dimsLabel = if (decodedBitmap != null) "${decodedBitmap.width}×${decodedBitmap.height}" else "PNG"
    val sizeKb = (frameFile.length() / 1024L).coerceAtLeast(1L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioSurfaceDark,
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("captured_frame_preview_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HD Still Frame Captured",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${frameFile.name} • $dimsLabel • ${sizeKb} KB",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (decodedBitmap != null) {
                        Image(
                            bitmap = decodedBitmap.asImageBitmap(),
                            contentDescription = "Captured Still Frame",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Preview unavailable", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSaveToDevice,
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("captured_frame_save_button")
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = "Save PNG", tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save PNG", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onShare,
                        border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("captured_frame_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share PNG", tint = AccentEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = AccentEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onAddAsImageOverlay,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("captured_frame_add_overlay_button")
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Overlay", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert as Timeline Overlay", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
