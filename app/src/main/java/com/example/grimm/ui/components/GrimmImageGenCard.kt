package com.example.grimm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grimm.service.ImageAspectRatio
import com.example.grimm.service.ImageGenService
import com.example.grimm.service.ImageStyle
import com.example.ui.theme.GrimmBorder
import com.example.ui.theme.GrimmCyan
import com.example.ui.theme.GrimmDarkBlue
import com.example.ui.theme.GrimmLightBlue
import com.example.ui.theme.GrimmLightBlueSoft
import com.example.ui.theme.GrimmSurface
import com.example.ui.theme.GrimmTextMuted
import com.example.ui.theme.GrimmTextPrimary
import com.example.ui.theme.GrimmTextSecondary

@Composable
fun GrimmImageGenCard(
    prompt: String,
    onPromptChange: (String) -> Unit,
    selectedStyle: ImageStyle,
    onStyleSelect: (ImageStyle) -> Unit,
    selectedAspectRatio: ImageAspectRatio,
    onAspectRatioSelect: (ImageAspectRatio) -> Unit,
    onGenerateClick: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GrimmSurface,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrimmLightBlue),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("image_gen_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GrimmLightBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = GrimmLightBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI Picture Maker Studio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrimmDarkBlue
                        )
                        Text(
                            text = "Generate and render custom visuals from prompts",
                            fontSize = 11.sp,
                            color = GrimmTextSecondary
                        )
                    }
                }
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close image tool",
                        tint = GrimmTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Input
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = { Text("Describe the visual you want Grimm AI to create...", fontSize = 13.sp, color = GrimmTextMuted) },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = GrimmTextPrimary,
                    fontSize = 13.sp
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GrimmTextPrimary,
                    unfocusedTextColor = GrimmTextPrimary,
                    cursorColor = GrimmLightBlue,
                    focusedBorderColor = GrimmLightBlue,
                    unfocusedBorderColor = GrimmBorder,
                    focusedContainerColor = GrimmSurface,
                    unfocusedContainerColor = GrimmSurface
                ),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("image_gen_prompt_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sample Prompts Carousel
            Text(
                text = "Inspiration Presets:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrimmTextSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ImageGenService.samplePrompts.forEach { sample ->
                    Surface(
                        onClick = { onPromptChange(sample) },
                        shape = RoundedCornerShape(16.dp),
                        color = GrimmLightBlueSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrimmBorder)
                    ) {
                        Text(
                            text = sample.take(34) + "...",
                            fontSize = 11.sp,
                            color = GrimmDarkBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Style Selectors
            Text(
                text = "Aesthetic Preset:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrimmTextSecondary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ImageStyle.values().forEach { style ->
                    val isSelected = selectedStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStyleSelect(style) },
                        label = { Text(style.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrimmLightBlue,
                            selectedLabelColor = Color.White,
                            containerColor = GrimmSurface,
                            labelColor = GrimmTextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Aspect Ratio and Generate Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ImageAspectRatio.values().forEach { ratio ->
                        val isSelected = selectedAspectRatio == ratio
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GrimmDarkBlue else GrimmSurface)
                                .border(1.dp, if (isSelected) GrimmDarkBlue else GrimmBorder, RoundedCornerShape(8.dp))
                                .clickable { onAspectRatioSelect(ratio) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = ratio.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else GrimmTextSecondary
                            )
                        }
                    }
                }

                Button(
                    onClick = { onGenerateClick(prompt) },
                    enabled = prompt.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GrimmLightBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("generate_image_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Render Visual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
