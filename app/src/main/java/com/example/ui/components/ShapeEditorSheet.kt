package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShapeAnimation
import com.example.data.model.ShapeOverlay
import com.example.data.model.ShapeType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeEditorSheet(
    existingShape: ShapeOverlay? = null,
    onDismiss: () -> Unit,
    onSaveShape: (ShapeOverlay) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var selectedShapeType by remember { mutableStateOf(existingShape?.shapeType ?: ShapeType.RECTANGLE) }
    var fillColorHex by remember { mutableLongStateOf(existingShape?.fillColorHex ?: 0xCC1E293B) }
    var strokeColorHex by remember { mutableLongStateOf(existingShape?.strokeColorHex ?: 0xFF00E5FF) }
    var strokeWidthDp by remember { mutableFloatStateOf(existingShape?.strokeWidthDp ?: 3f) }
    var isFilled by remember { mutableStateOf(existingShape?.isFilled ?: true) }
    var cornerRadiusDp by remember { mutableFloatStateOf(existingShape?.cornerRadiusDp ?: 12f) }
    var rotation by remember { mutableFloatStateOf(existingShape?.rotation ?: 0f) }
    var scale by remember { mutableFloatStateOf(existingShape?.scale ?: 1.0f) }
    var selectedAnimation by remember { mutableStateOf(existingShape?.animation ?: ShapeAnimation.POP_IN) }

    // Tab for Color Picker mode: 0 = Fill Color, 1 = Stroke Color
    var activeColorTarget by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF311B92)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Shapes",
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (existingShape != null) "Edit Vector Shape" else "Add Vector Shape Overlay",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Precision vector overlays with motion animations",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Interactive Preview Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF090A0F),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232B3E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(100.dp)
                            .rotate(rotation)
                            .scale(scale)
                    ) {
                        drawShapeGeometry(
                            shapeType = selectedShapeType,
                            isFilled = isFilled,
                            fillColor = Color(fillColorHex),
                            strokeColor = Color(strokeColorHex),
                            strokeWidthPx = strokeWidthDp * density,
                            cornerRadiusPx = cornerRadiusDp * density
                        )
                    }

                    // Watermark badge in preview
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${selectedShapeType.displayName} • ${selectedAnimation.displayName}",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Shape Geometry Type Selector
            Text(
                text = "Shape Geometry",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ShapeType.entries.toTypedArray()) { shape ->
                    val isSelected = selectedShapeType == shape
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF381E72) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentCyan else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .clickable { selectedShapeType = shape }
                            .testTag("shape_type_${shape.name.lowercase()}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = shape.displayName,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Fill & Stroke Styling Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fill Tab
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeColorTarget == 0) AccentCyan else StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeColorTarget = 0 }
                        .testTag("shape_fill_color_tab")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(fillColorHex))
                                    .border(1.dp, Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Fill Color",
                                color = if (activeColorTarget == 0) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isFilled,
                            onCheckedChange = { isFilled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }

                // Stroke Tab
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (activeColorTarget == 1) AccentCyan else StudioCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C2840)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeColorTarget = 1 }
                        .testTag("shape_stroke_color_tab")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(strokeColorHex))
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stroke Color",
                            color = if (activeColorTarget == 1) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Integrated Color Picker Component for Active Target (Fill or Stroke)
            ColorPickerComponent(
                selectedColorHex = if (activeColorTarget == 0) fillColorHex else strokeColorHex,
                onColorChanged = { newColor ->
                    if (activeColorTarget == 0) {
                        fillColorHex = newColor
                    } else {
                        strokeColorHex = newColor
                    }
                },
                title = if (activeColorTarget == 0) "Fill Color & Palettes" else "Stroke Color & Palettes",
                showAlphaSlider = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Sliders: Stroke Width, Corner Radius, Rotation & Scale
            // Stroke Width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Stroke Width", color = Color.LightGray, fontSize = 12.sp)
                Text(
                    text = "${strokeWidthDp.roundToInt()} dp",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = strokeWidthDp,
                onValueChange = { strokeWidthDp = it },
                valueRange = 0.5f..12f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                modifier = Modifier.testTag("shape_stroke_width_slider")
            )

            // Corner Radius (if Rectangle or Frame)
            if (selectedShapeType == ShapeType.RECTANGLE || selectedShapeType == ShapeType.SPOTLIGHT_FRAME) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Corner Radius", color = Color.LightGray, fontSize = 12.sp)
                    Text(
                        text = "${cornerRadiusDp.roundToInt()} dp",
                        color = AccentCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = cornerRadiusDp,
                    onValueChange = { cornerRadiusDp = it },
                    valueRange = 0f..40f,
                    colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                    modifier = Modifier.testTag("shape_corner_radius_slider")
                )
            }

            // Rotation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Rotation Angle", color = Color.LightGray, fontSize = 12.sp)
                Text(
                    text = "${rotation.roundToInt()}°",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = rotation,
                onValueChange = { rotation = it },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                modifier = Modifier.testTag("shape_rotation_slider")
            )

            // Scale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Scale Size", color = Color.LightGray, fontSize = 12.sp)
                Text(
                    text = "${String.format("%.2f", scale)}x",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = scale,
                onValueChange = { scale = it },
                valueRange = 0.5f..2.5f,
                colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                modifier = Modifier.testTag("shape_scale_slider")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. OpenReel / LibreCuts Keyframe Animation Presets
            Text(
                text = "Keyframe Motion Animation",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ShapeAnimation.entries.toTypedArray()) { anim ->
                    val isSelected = selectedAnimation == anim
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF6200EA) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentCyan else Color(0xFF2C2840)
                        ),
                        modifier = Modifier
                            .clickable { selectedAnimation = anim }
                            .testTag("shape_anim_${anim.name.lowercase()}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = anim.displayName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = anim.description,
                                color = if (isSelected) AccentCyan else Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save / Add Button
            Button(
                onClick = {
                    val shape = (existingShape ?: ShapeOverlay()).copy(
                        shapeType = selectedShapeType,
                        fillColorHex = fillColorHex,
                        strokeColorHex = strokeColorHex,
                        strokeWidthDp = strokeWidthDp,
                        isFilled = isFilled,
                        cornerRadiusDp = cornerRadiusDp,
                        rotation = rotation,
                        scale = scale,
                        animation = selectedAnimation
                    )
                    onSaveShape(shape)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_shape_button")
            ) {
                Text(
                    text = if (existingShape != null) "Update Shape" else "Add Shape to Timeline",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Canvas drawing helper for vector shapes
 */
fun DrawScope.drawShapeGeometry(
    shapeType: ShapeType,
    isFilled: Boolean,
    fillColor: Color,
    strokeColor: Color,
    strokeWidthPx: Float,
    cornerRadiusPx: Float
) {
    val w = size.width
    val h = size.height

    when (shapeType) {
        ShapeType.RECTANGLE -> {
            val inset = strokeWidthPx / 2
            val rectSize = Size(w - strokeWidthPx, h - strokeWidthPx)
            if (isFilled) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(inset, inset),
                    size = rectSize,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Fill
                )
            }
            if (strokeWidthPx > 0) {
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(inset, inset),
                    size = rectSize,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokeWidthPx)
                )
            }
        }
        ShapeType.CIRCLE -> {
            val radius = (minOf(w, h) / 2f) - (strokeWidthPx / 2f)
            val center = Offset(w / 2f, h / 2f)
            if (isFilled) {
                drawCircle(color = fillColor, radius = radius, center = center, style = Fill)
            }
            if (strokeWidthPx > 0) {
                drawCircle(color = strokeColor, radius = radius, center = center, style = Stroke(width = strokeWidthPx))
            }
        }
        ShapeType.POINTER_ARROW -> {
            val path = Path().apply {
                // Pointing arrow
                moveTo(w * 0.5f, h * 0.1f)
                lineTo(w * 0.9f, h * 0.6f)
                lineTo(w * 0.65f, h * 0.6f)
                lineTo(w * 0.65f, h * 0.95f)
                lineTo(w * 0.35f, h * 0.95f)
                lineTo(w * 0.35f, h * 0.6f)
                lineTo(w * 0.1f, h * 0.6f)
                close()
            }
            if (isFilled) {
                drawPath(path = path, color = fillColor, style = Fill)
            }
            if (strokeWidthPx > 0) {
                drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidthPx))
            }
        }
        ShapeType.CALLOUT_BANNER -> {
            val bubbleH = h * 0.75f
            val path = Path().apply {
                // Speech bubble with pointer tail
                moveTo(cornerRadiusPx, 0f)
                lineTo(w - cornerRadiusPx, 0f)
                arcTo(androidx.compose.ui.geometry.Rect(w - 2 * cornerRadiusPx, 0f, w, 2 * cornerRadiusPx), -90f, 90f, false)
                lineTo(w, bubbleH - cornerRadiusPx)
                arcTo(androidx.compose.ui.geometry.Rect(w - 2 * cornerRadiusPx, bubbleH - 2 * cornerRadiusPx, w, bubbleH), 0f, 90f, false)
                lineTo(w * 0.55f, bubbleH)
                lineTo(w * 0.45f, h) // Pointer tail tip
                lineTo(w * 0.35f, bubbleH)
                lineTo(cornerRadiusPx, bubbleH)
                arcTo(androidx.compose.ui.geometry.Rect(0f, bubbleH - 2 * cornerRadiusPx, 2 * cornerRadiusPx, bubbleH), 90f, 90f, false)
                lineTo(0f, cornerRadiusPx)
                arcTo(androidx.compose.ui.geometry.Rect(0f, 0f, 2 * cornerRadiusPx, 2 * cornerRadiusPx), 180f, 90f, false)
                close()
            }
            if (isFilled) {
                drawPath(path = path, color = fillColor, style = Fill)
            }
            if (strokeWidthPx > 0) {
                drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidthPx))
            }
        }
        ShapeType.SPOTLIGHT_FRAME -> {
            // Cutout focus frame (corner brackets)
            val bracketLen = minOf(w, h) * 0.3f
            val inset = strokeWidthPx / 2
            val sw = strokeWidthPx

            // Top-left
            drawLine(strokeColor, Offset(inset, inset), Offset(inset + bracketLen, inset), strokeWidth = sw)
            drawLine(strokeColor, Offset(inset, inset), Offset(inset, inset + bracketLen), strokeWidth = sw)
            // Top-right
            drawLine(strokeColor, Offset(w - inset, inset), Offset(w - inset - bracketLen, inset), strokeWidth = sw)
            drawLine(strokeColor, Offset(w - inset, inset), Offset(w - inset, inset + bracketLen), strokeWidth = sw)
            // Bottom-left
            drawLine(strokeColor, Offset(inset, h - inset), Offset(inset + bracketLen, h - inset), strokeWidth = sw)
            drawLine(strokeColor, Offset(inset, h - inset), Offset(inset, h - inset - bracketLen), strokeWidth = sw)
            // Bottom-right
            drawLine(strokeColor, Offset(w - inset, h - inset), Offset(w - inset - bracketLen, h - inset), strokeWidth = sw)
            drawLine(strokeColor, Offset(w - inset, h - inset), Offset(w - inset, h - inset - bracketLen), strokeWidth = sw)

            if (isFilled) {
                drawRect(color = fillColor.copy(alpha = 0.25f))
            }
        }
        ShapeType.STAR_BADGE -> {
            val points = 5
            val outerRadius = (minOf(w, h) / 2f) - (strokeWidthPx / 2f)
            val innerRadius = outerRadius * 0.45f
            val cx = w / 2f
            val cy = h / 2f

            val path = Path()
            val step = PI / points
            var angle = -PI / 2

            for (i in 0 until 2 * points) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val x = (cx + r * cos(angle)).toFloat()
                val y = (cy + r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                angle += step
            }
            path.close()

            if (isFilled) {
                drawPath(path = path, color = fillColor, style = Fill)
            }
            if (strokeWidthPx > 0) {
                drawPath(path = path, color = strokeColor, style = Stroke(width = strokeWidthPx))
            }
        }
    }
}
