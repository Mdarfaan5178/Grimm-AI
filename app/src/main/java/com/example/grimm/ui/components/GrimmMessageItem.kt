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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.grimm.model.ChatMessage
import com.example.grimm.model.MessageSender
import com.example.grimm.ui.markdown.GrimmMarkdownMessage
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
            modifier = Modifier.widthIn(max = if (isUser) 300.dp else 560.dp),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (message.isWebSearch) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE0F2FE))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Live Web",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0369A1)
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

                        // Live Web Search Sources List
                        if (!isUser && message.webResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Live Web Sources",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Live Web Sources (${message.webResults.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    message.webResults.forEach { result ->
                                        Surface(
                                            onClick = {
                                                try {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
                                                    context.startActivity(browserIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Cannot open web link", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCFCE7)),
                                            shadowElevation = 1.dp,
                                            modifier = Modifier.widthIn(max = 240.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = result.sourceName,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF16A34A),
                                                        maxLines = 1
                                                    )
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = "Open link",
                                                        tint = GrimmTextMuted,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = result.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = GrimmTextPrimary,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                if (result.snippet.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = result.snippet,
                                                        fontSize = 10.sp,
                                                        color = GrimmTextSecondary,
                                                        maxLines = 2,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
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

fun launchReaperYouTubeChannel(context: android.content.Context) {
    val canonicalUrl = "https://www.youtube.com/@ReaperXMPG"
    try {
        // Try opening directly in YouTube App if installed
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(canonicalUrl)).apply {
            setPackage("com.google.android.youtube")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(appIntent)
    } catch (e: Exception) {
        try {
            // Fallback: Default Browser / System Chooser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(canonicalUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            // Ultimate fallback: Copy link to clipboard and notify user
            try {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("ReaperXMPG YouTube Channel", "https://youtube.com/@ReaperXMPG")
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(context, "Link copied to clipboard: https://youtube.com/@ReaperXMPG", Toast.LENGTH_LONG).show()
            } catch (e3: Exception) {
                Toast.makeText(context, "Cannot open YouTube link", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun openUrlSafely(context: android.content.Context, rawUrl: String) {
    var cleanUrl = rawUrl.trim()
    while (cleanUrl.isNotEmpty() && (cleanUrl.startsWith("*") || cleanUrl.startsWith("_") || cleanUrl.startsWith("`") || cleanUrl.startsWith("(") || cleanUrl.startsWith("["))) {
        cleanUrl = cleanUrl.drop(1)
    }
    while (cleanUrl.isNotEmpty() && (cleanUrl.endsWith("*") || cleanUrl.endsWith("_") || cleanUrl.endsWith("`") || cleanUrl.endsWith(".") || cleanUrl.endsWith(",") || cleanUrl.endsWith(")") || cleanUrl.endsWith(";") || cleanUrl.endsWith("!") || cleanUrl.endsWith("]"))) {
        cleanUrl = cleanUrl.dropLast(1)
    }
    if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
        cleanUrl = "https://$cleanUrl"
    }

    if (cleanUrl.contains("youtube.com/@ReaperXMPG", ignoreCase = true) || cleanUrl.contains("youtube.com/@reaperxmpg", ignoreCase = true)) {
        launchReaperYouTubeChannel(context)
        return
    }

    try {
        val uri = Uri.parse(cleanUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("URL", cleanUrl)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied: $cleanUrl", Toast.LENGTH_SHORT).show()
        } catch (e2: Exception) {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ClickableMessageText(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urlRegex = remember {
        Regex("""(https?://[^\s<>"'{}|\\^`]+)""")
    }

    val hasUrls = remember(text) { urlRegex.containsMatchIn(text) }

    if (!hasUrls) {
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = if (isUser) Color.White else GrimmTextPrimary,
            modifier = modifier
        )
    } else {
        val annotatedString = remember(text, isUser) {
            buildAnnotatedString {
                var currentIndex = 0
                val fullLength = text.length

                for (match in urlRegex.findAll(text)) {
                    val matchStart = match.range.first
                    var rawUrl = match.value
                    while (rawUrl.isNotEmpty() && (rawUrl.startsWith("*") || rawUrl.startsWith("_") || rawUrl.startsWith("`") || rawUrl.startsWith("(") || rawUrl.startsWith("["))) {
                        rawUrl = rawUrl.drop(1)
                    }
                    var trailingPunctuation = ""
                    while (rawUrl.isNotEmpty() && (rawUrl.endsWith("*") || rawUrl.endsWith("_") || rawUrl.endsWith("`") || rawUrl.endsWith(".") || rawUrl.endsWith(",") || rawUrl.endsWith(")") || rawUrl.endsWith(";") || rawUrl.endsWith("!") || rawUrl.endsWith("]"))) {
                        trailingPunctuation = rawUrl.takeLast(1) + trailingPunctuation
                        rawUrl = rawUrl.dropLast(1)
                    }

                    if (matchStart > currentIndex && matchStart <= fullLength) {
                        append(text.substring(currentIndex, matchStart))
                    }

                    val linkStart = length
                    append(rawUrl)
                    val linkEnd = length

                    addStyle(
                        style = SpanStyle(
                            color = if (isUser) Color(0xFF93C5FD) else Color(0xFF2563EB),
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.SemiBold
                        ),
                        start = linkStart,
                        end = linkEnd
                    )
                    addStringAnnotation(
                        tag = "URL",
                        annotation = rawUrl,
                        start = linkStart,
                        end = linkEnd
                    )

                    if (trailingPunctuation.isNotEmpty()) {
                        append(trailingPunctuation)
                    }

                    currentIndex = match.range.last + 1
                }

                if (currentIndex < fullLength) {
                    append(text.substring(currentIndex, fullLength))
                }
            }
        }

        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = if (isUser) Color.White else GrimmTextPrimary
            ),
            modifier = modifier,
            onClick = { offset ->
                val annotation = annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()
                if (annotation != null) {
                    val clickedUrl = annotation.item
                    if (clickedUrl.contains("ReaperXMPG", ignoreCase = true)) {
                        launchReaperYouTubeChannel(context)
                    } else {
                        openUrlSafely(context, clickedUrl)
                    }
                }
            }
        )
    }
}

@Composable
fun RenderFormattedText(
    text: String,
    isUser: Boolean
) {
    val context = LocalContext.current

    Column {
        GrimmMarkdownMessage(
            text = text,
            isUser = isUser
        )

        // Dedicated clickable YouTube badge if ReaperXMPG channel is mentioned
        val mentionsReaperChannel = !isUser && (
            text.contains("youtube.com/@ReaperXMPG", ignoreCase = true) ||
            text.contains("youtube.com/@reaperxmpg", ignoreCase = true) ||
            (text.contains("ReaperXMPG", ignoreCase = true) && (text.contains("channel", ignoreCase = true) || text.contains("youtube", ignoreCase = true)))
        )

        if (mentionsReaperChannel) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                onClick = { launchReaperYouTubeChannel(context) },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF2F2),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reaper_youtube_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open YouTube Channel",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ReaperXMPG on YouTube",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "https://youtube.com/@ReaperXMPG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDC2626)
                        )
                    }
                    // Copy button
                    IconButton(
                        onClick = {
                            try {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("ReaperXMPG YouTube", "https://youtube.com/@ReaperXMPG")
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Unable to copy link", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Open channel button
                    IconButton(
                        onClick = { launchReaperYouTubeChannel(context) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Channel",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
