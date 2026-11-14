package com.example.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.StudioCardDark
import com.example.ui.theme.StudioSurfaceDark

data class TitleTemplateItem(
    val id: String,
    val name: String,
    val category: String,
    val is3D: Boolean,
    val previewBg: List<Color>,
    val defaultTitle: String,
    val defaultSubtitle: String
)

val openShotTitleTemplates = listOf(
    TitleTemplateItem(
        id = "STAR_CRAWL_3D",
        name = "Space Crawl 3D",
        category = "3D Animated",
        is3D = true,
        previewBg = listOf(Color(0xFF0F172A), Color(0xFF020617)),
        defaultTitle = "EPISODE IV: A NEW HOPE",
        defaultSubtitle = "In a galaxy far, far away..."
    ),
    TitleTemplateItem(
        id = "EXPLODING_TEXT_3D",
        name = "Exploding Kinetic 3D",
        category = "3D Animated",
        is3D = true,
        previewBg = listOf(Color(0xFF7C2D12), Color(0xFF451A03)),
        defaultTitle = "BLOCKBUSTER PREMIERE",
        defaultSubtitle = "Coming this summer to theaters"
    ),
    TitleTemplateItem(
        id = "NEON_GLITCH_3D",
        name = "Neon Cyber Glitch 3D",
        category = "3D Animated",
        is3D = true,
        previewBg = listOf(Color(0xFF581C87), Color(0xFF1E1B4B)),
        defaultTitle = "CYBERPUNK 2088",
        defaultSubtitle = "The future is now"
    ),
    TitleTemplateItem(
        id = "LENS_FLARE_3D",
        name = "Anamorphic Flare 3D",
        category = "3D Animated",
        is3D = true,
        previewBg = listOf(Color(0xFF0369A1), Color(0xFF0C4A6E)),
        defaultTitle = "DIRECTED BY OPENCUT",
        defaultSubtitle = "An OpenShot creation"
    ),
    TitleTemplateItem(
        id = "BROADCAST_LOWER_THIRD",
        name = "Broadcast Lower Third",
        category = "Standard",
        is3D = false,
        previewBg = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
        defaultTitle = "ALEX JOHNSON",
        defaultSubtitle = "Lead Cinematographer & Editor"
    ),
    TitleTemplateItem(
        id = "CINEMA_CENTERPIECE",
        name = "Cinema Headline",
        category = "Standard",
        is3D = false,
        previewBg = listOf(Color(0xFF334155), Color(0xFF1E293B)),
        defaultTitle = "THE LOST HORIZON",
        defaultSubtitle = "A cinematic journey"
    ),
    TitleTemplateItem(
        id = "BAR_BANNER",
        name = "Minimal Banner",
        category = "Standard",
        is3D = false,
        previewBg = listOf(Color(0xFF111827), Color(0xFF030712)),
        defaultTitle = "BREAKING NEWS",
        defaultSubtitle = "Live on location report"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenShotTitleSheet(
    onDismiss: () -> Unit,
    onAddTitle: (
        title: String,
        subtitle: String,
        template: String,
        is3D: Boolean,
        fontSizeSp: Int,
        textColorHex: Long,
        bgColorHex: Long,
        borderColorHex: Long,
        borderWidthDp: Float,
        durationMs: Long
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTemplate by remember { mutableStateOf(openShotTitleTemplates.first()) }
    var titleText by remember { mutableStateOf(selectedTemplate.defaultTitle) }
    var subtitleText by remember { mutableStateOf(selectedTemplate.defaultSubtitle) }
    var fontSize by remember { mutableIntStateOf(26) }
    var durationSec by remember { mutableIntStateOf(4) }
    var textColorHex by remember { mutableLongStateOf(0xFFFFFFFFL) }
    var bgColorHex by remember { mutableLongStateOf(0xCC000000L) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TextFields, "Titles", tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OpenShot Title Editor",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Standard & 3D Animated Title Generator",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_titles_sheet")) {
                    Icon(Icons.Default.Close, "Close", tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Template Selector Row
            Text(
                text = "Choose Template",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(openShotTitleTemplates) { template ->
                    val isSelected = selectedTemplate.id == template.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF2C244A) else StudioCardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentCyan else Color(0xFF38315E)
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedTemplate = template
                                titleText = template.defaultTitle
                                subtitleText = template.defaultSubtitle
                            }
                            .testTag("title_template_${template.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (template.is3D) {
                                Icon(Icons.Default.AutoAwesome, "3D", tint = AccentPink, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = template.name,
                                color = if (isSelected) AccentCyan else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Animated Preview Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38315E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Brush.linearGradient(selectedTemplate.previewBg)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(bgColorHex),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = titleText.ifBlank { "Sample Title" },
                                color = Color(textColorHex),
                                fontSize = (fontSize * 0.7f).sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (subtitleText.isNotBlank()) {
                            Text(
                                text = subtitleText,
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (selectedTemplate.is3D) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AccentPink.copy(alpha = 0.25f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "3D PERSPECTIVE ANIMATION",
                                    color = AccentPink,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inputs
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Main Headline Text") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = Color(0xFF38315E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input_headline")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = subtitleText,
                onValueChange = { subtitleText = it },
                label = { Text("Sub-Headline / Description (Optional)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = Color(0xFF38315E)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input_subtitle")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Controls: Font size & duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Font Size: ${fontSize}sp",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(110.dp)
                )
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { fontSize = it.toInt() },
                    valueRange = 16f..42f,
                    colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duration: ${durationSec}s",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(110.dp)
                )
                Slider(
                    value = durationSec.toFloat(),
                    onValueChange = { durationSec = it.toInt() },
                    valueRange = 1f..10f,
                    colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Title Button
            Button(
                onClick = {
                    onAddTitle(
                        titleText,
                        subtitleText,
                        selectedTemplate.id,
                        selectedTemplate.is3D,
                        fontSize,
                        textColorHex,
                        bgColorHex,
                        0xFF000000L,
                        if (selectedTemplate.is3D) 2f else 0f,
                        durationSec * 1000L
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_title_confirm_button")
            ) {
                Text(
                    text = if (selectedTemplate.is3D) "Generate 3D Animated Title" else "Insert Title Overlay",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
