package com.example.grimm.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.grimm.model.ChatMessage
import com.example.grimm.model.MessageSender
import com.example.ui.theme.GrimmBorder
import com.example.ui.theme.GrimmCyan
import com.example.ui.theme.GrimmDarkBlue
import com.example.ui.theme.GrimmLightBlue
import com.example.ui.theme.GrimmLightBlueSoft
import com.example.ui.theme.GrimmSurface
import com.example.ui.theme.GrimmTemporaryBg
import com.example.ui.theme.GrimmTemporaryPurple
import com.example.ui.theme.GrimmTextMuted
import com.example.ui.theme.GrimmTextPrimary
import com.example.ui.theme.GrimmTextSecondary

@Composable
fun GrimmMessageItem(
    message: ChatMessage,
    onSpeakClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Grimm AI Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(GrimmDarkBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Grimm AI Avatar",
                    tint = GrimmCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.width(if (isUser) 300.dp else 320.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Message Card
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) GrimmDarkBlue else GrimmSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUser) GrimmLightBlue else GrimmBorder
                ),
                shadowElevation = if (isUser) 2.dp else 1.dp,
                modifier = Modifier.testTag("message_card_${message.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header tag row for assistant
                    if (!isUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Grimm AI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrimmDarkBlue
                                )
                                if (message.isTemporary) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GrimmTemporaryBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Temporary",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GrimmTemporaryPurple
                                        )
                                    }
                                }
                            }
                            // Style badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GrimmLightBlueSoft)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = message.outputStyle.label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GrimmDarkBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // User attached picture
                    if (message.attachedImageUri != null) {
                        SubcomposeAsyncImage(
                            model = message.attachedImageUri,
                            contentDescription = "User attached image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onImageClick(message.attachedImageUri) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Content: text or image
                    if (message.isImage && message.imageUrl != null) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                                    .clickable { onImageClick(message.imageUrl) },
                                contentAlignment = Alignment.Center
                            ) {
                                SubcomposeAsyncImage(
                                    model = message.imageUrl,
                                    contentDescription = message.prompt ?: "Generated visual",
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = GrimmLightBlue,
                                                modifier = Modifier.size(28.dp),
                                                strokeWidth = 2.5.dp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Rendering visual...",
                                                fontSize = 11.sp,
                                                color = GrimmTextSecondary
                                            )
                                        }
                                    },
                                    error = {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = GrimmTextMuted,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Image generated: Tap to view online",
                                                fontSize = 11.sp,
                                                color = GrimmTextSecondary
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                // Zoom icon overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "Zoom image",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (!message.prompt.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${message.prompt}\"",
                                    fontSize = 11.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = GrimmTextSecondary
                                )
                            }
                        }
                    } else {
                        // Text message with code-block handling
                        RenderFormattedText(
                            text = message.text,
                            isUser = isUser
                        )
                    }

                    // Action buttons for assistant
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // TTS Speak button
                            IconButton(
                                onClick = { onSpeakClick(message.text) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("tts_button_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read message aloud",
                                    tint = GrimmLightBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // Copy button
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.text))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("copy_button_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message text",
                                    tint = GrimmTextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(GrimmLightBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun RenderFormattedText(
    text: String,
    isUser: Boolean
) {
    // If text contains code blocks ```, format appropriately
    if (text.contains("```")) {
        val segments = text.split("```")
        Column {
            segments.forEachIndexed { index, segment ->
                if (index % 2 == 1) {
                    // Code block
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = segment.trim(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = GrimmCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else if (segment.isNotBlank()) {
                    Text(
                        text = segment.trim(),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (isUser) Color.White else GrimmTextPrimary
                    )
                }
            }
        }
    } else {
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = if (isUser) Color.White else GrimmTextPrimary
        )
    }
}
