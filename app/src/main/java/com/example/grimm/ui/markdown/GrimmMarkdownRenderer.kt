package com.example.grimm.ui.markdown

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grimm.ui.components.launchReaperYouTubeChannel
import com.example.grimm.ui.components.openUrlSafely
import com.example.ui.theme.GrimmBorder
import com.example.ui.theme.GrimmCyan
import com.example.ui.theme.GrimmDarkBlue
import com.example.ui.theme.GrimmLightBlue
import com.example.ui.theme.GrimmLightBlueSoft
import com.example.ui.theme.GrimmTextMuted
import com.example.ui.theme.GrimmTextPrimary
import com.example.ui.theme.GrimmTextSecondary

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    data class Blockquote(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class Table(val table: MarkdownTable) : MarkdownBlock
    object HorizontalRule : MarkdownBlock
}

object GrimmMarkdownParser {

    /**
     * Parses raw message text into structured Markdown blocks.
     */
    fun parse(rawText: String): List<MarkdownBlock> {
        val blocks = mutableListOf<MarkdownBlock>()
        val lines = rawText.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            // 1. Fenced Code Blocks (```)
            if (trimmed.startsWith("```")) {
                val lang = trimmed.removePrefix("```").trim()
                val codeBuilder = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeBuilder.append(lines[i]).append("\n")
                    i++
                }
                // Skip the closing ``` if present
                if (i < lines.size && lines[i].trim().startsWith("```")) {
                    i++
                }
                blocks.add(MarkdownBlock.CodeBlock(language = lang, code = codeBuilder.toString().trimEnd()))
                continue
            }

            // 2. Markdown Table detection
            if (trimmed.startsWith("|") && trimmed.endsWith("|") && i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                if (nextLine.startsWith("|") && nextLine.contains("-")) {
                    val tableLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().contains("|")) {
                        tableLines.add(lines[i].trim())
                        i++
                    }
                    val parsedTable = GrimmTableParser.parse(tableLines)
                    if (parsedTable != null && parsedTable.headers.isNotEmpty()) {
                        blocks.add(MarkdownBlock.Table(parsedTable))
                        continue
                    } else {
                        // If parsing failed, fallback to paragraphs for these lines
                        tableLines.forEach { blocks.add(MarkdownBlock.Paragraph(it)) }
                        continue
                    }
                }
            }

            // 3. Horizontal Rule: --- or *** or ___
            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                blocks.add(MarkdownBlock.HorizontalRule)
                i++
                continue
            }

            // 4. Headings: #, ##, ###, ####
            val headingMatch = Regex("""^(#{1,4})\s+(.*)$""").find(trimmed)
            if (headingMatch != null) {
                val hashes = headingMatch.groupValues[1]
                val content = headingMatch.groupValues[2].trim()
                blocks.add(MarkdownBlock.Heading(level = hashes.length, text = content))
                i++
                continue
            }

            // 5. Blockquote: > text
            if (trimmed.startsWith(">")) {
                val quoteText = trimmed.removePrefix(">").trim()
                blocks.add(MarkdownBlock.Blockquote(quoteText))
                i++
                continue
            }

            // 6. Bullet list item: -, *, +, •
            val bulletMatch = Regex("""^([*+\-•])\s+(.*)$""").find(trimmed)
            if (bulletMatch != null) {
                val content = bulletMatch.groupValues[2].trim()
                blocks.add(MarkdownBlock.BulletItem(content))
                i++
                continue
            }

            // 7. Numbered list item: 1., 2., etc.
            val numberedMatch = Regex("""^(\d+)[.)]\s+(.*)$""").find(trimmed)
            if (numberedMatch != null) {
                val number = numberedMatch.groupValues[1]
                val content = numberedMatch.groupValues[2].trim()
                blocks.add(MarkdownBlock.NumberedItem(number = number, text = content))
                i++
                continue
            }

            // 8. Normal line or paragraph
            if (trimmed.isNotEmpty()) {
                val paraBuilder = StringBuilder(line)
                i++
                while (i < lines.size) {
                    val next = lines[i]
                    val nextTrim = next.trim()
                    if (nextTrim.isEmpty() ||
                        nextTrim.startsWith("```") ||
                        nextTrim.startsWith("|") ||
                        nextTrim.startsWith(">") ||
                        nextTrim == "---" ||
                        Regex("""^(#{1,4})\s+""").containsMatchIn(nextTrim) ||
                        Regex("""^([*+\-•])\s+""").containsMatchIn(nextTrim) ||
                        Regex("""^(\d+)[.)]\s+""").containsMatchIn(nextTrim)
                    ) {
                        break
                    }
                    paraBuilder.append("\n").append(next)
                    i++
                }
                blocks.add(MarkdownBlock.Paragraph(paraBuilder.toString()))
            } else {
                i++
            }
        }

        return blocks
    }
}

/**
 * Builds an AnnotatedString from text with inline Markdown:
 * - Bold: **text** or __text__
 * - Italic: *text* or _text_
 * - Bold & Italic: ***text***
 * - Inline code: `text`
 * - Strikethrough: ~~text~~
 * - Links: [label](url) and bare URLs
 */
fun buildInlineMarkdownAnnotatedString(
    rawText: String,
    isUser: Boolean,
    primaryColor: Color = if (isUser) Color.White else GrimmTextPrimary,
    linkColor: Color = if (isUser) Color(0xFF93C5FD) else Color(0xFF2563EB),
    codeBgColor: Color = if (isUser) Color.White.copy(alpha = 0.2f) else Color(0xFFF1F5F9),
    codeTextColor: Color = if (isUser) Color.White else Color(0xFF0F172A)
): AnnotatedString {
    return buildAnnotatedString {
        // Regex pattern to tokenize inline markdown tokens:
        // 1. Markdown link: [text](url)
        // 2. Bare URL: https?://...
        // 3. Inline code: `code`
        // 4. Bold + Italic: ***text*** or ___text___
        // 5. Bold: **text** or __text__
        // 6. Italic: *text* or _text_
        // 7. Strikethrough: ~~text~~
        val tokenRegex = Regex(
            """(\[(.*?)\]\((https?://[^\s)]+)\))|""" + // Group 1, 2 (label), 3 (url)
            """(https?://[^\s<>"'{}|\\^`]+)|""" +      // Group 4 (bare url)
            """(`([^`\n]+)`)|""" +                     // Group 5, 6 (inline code)
            """(\*\*\*([^*]+)\*\*\*|___([^_]+)___)|""" + // Group 7, 8/9 (bold italic)
            """(\*\*([^*]+)\*\*|__([^_]+)__)|""" +       // Group 10, 11/12 (bold)
            """(\*([^*]+)\*|_([^_]+)_)|""" +             // Group 13, 14/15 (italic)
            """(~~([^~]+)~~)"""                          // Group 16, 17 (strikethrough)
        )

        var lastIndex = 0
        for (match in tokenRegex.findAll(rawText)) {
            val matchStart = match.range.first
            val matchEnd = match.range.last + 1

            if (matchStart > lastIndex) {
                append(rawText.substring(lastIndex, matchStart))
            }

            val mdLinkUrl = match.groups[3]?.value
            val mdLinkLabel = match.groups[2]?.value
            val bareUrl = match.groups[4]?.value
            val inlineCode = match.groups[6]?.value
            val boldItalic = match.groups[8]?.value ?: match.groups[9]?.value
            val bold = match.groups[11]?.value ?: match.groups[12]?.value
            val italic = match.groups[14]?.value ?: match.groups[15]?.value
            val strikethrough = match.groups[17]?.value

            when {
                mdLinkUrl != null && mdLinkLabel != null -> {
                    val start = length
                    append(mdLinkLabel)
                    val end = length
                    addStyle(
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold),
                        start, end
                    )
                    addStringAnnotation(tag = "URL", annotation = mdLinkUrl, start = start, end = end)
                }
                bareUrl != null -> {
                    var cleanBareUrl: String = bareUrl
                    var trailingPunct = ""
                    while (cleanBareUrl.isNotEmpty() && (cleanBareUrl.endsWith(".") || cleanBareUrl.endsWith(",") || cleanBareUrl.endsWith(")") || cleanBareUrl.endsWith(";"))) {
                        trailingPunct = cleanBareUrl.takeLast(1) + trailingPunct
                        cleanBareUrl = cleanBareUrl.dropLast(1)
                    }

                    val start = length
                    append(cleanBareUrl)
                    val end = length
                    addStyle(
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.SemiBold),
                        start, end
                    )
                    addStringAnnotation(tag = "URL", annotation = cleanBareUrl, start = start, end = end)
                    if (trailingPunct.isNotEmpty()) {
                        append(trailingPunct)
                    }
                }
                inlineCode != null -> {
                    val start = length
                    append(" $inlineCode ")
                    val end = length
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = codeTextColor,
                            background = codeBgColor,
                            fontSize = 12.5.sp
                        ),
                        start, end
                    )
                }
                boldItalic != null -> {
                    val start = length
                    append(boldItalic)
                    val end = length
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                        start, end
                    )
                }
                bold != null -> {
                    val start = length
                    append(bold)
                    val end = length
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start, end
                    )
                }
                italic != null -> {
                    val start = length
                    append(italic)
                    val end = length
                    addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        start, end
                    )
                }
                strikethrough != null -> {
                    val start = length
                    append(strikethrough)
                    val end = length
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        start, end
                    )
                }
                else -> {
                    append(match.value)
                }
            }

            lastIndex = matchEnd
        }

        if (lastIndex < rawText.length) {
            append(rawText.substring(lastIndex))
        }
    }
}

@Composable
fun GrimmInlineMarkdownText(
    rawText: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    textAlign: TextAlign = TextAlign.Start,
    isUser: Boolean = false,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textColor: Color = if (isUser) Color.White else GrimmTextPrimary
) {
    val context = LocalContext.current
    val annotatedString = remember(rawText, isUser) {
        buildInlineMarkdownAnnotatedString(
            rawText = rawText,
            isUser = isUser,
            primaryColor = textColor
        )
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize,
            lineHeight = lineHeight,
            color = textColor,
            textAlign = textAlign,
            fontStyle = fontStyle,
            fontWeight = fontWeight
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

@Composable
fun GrimmMarkdownMessage(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { GrimmMarkdownParser.parse(text) }

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val (size, weight) = when (block.level) {
                        1 -> 18.sp to FontWeight.Bold
                        2 -> 16.sp to FontWeight.Bold
                        3 -> 15.sp to FontWeight.SemiBold
                        else -> 14.sp to FontWeight.SemiBold
                    }
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    GrimmInlineMarkdownText(
                        rawText = block.text,
                        fontSize = size,
                        lineHeight = (size.value + 6).sp,
                        fontWeight = weight,
                        isUser = isUser,
                        textColor = if (isUser) Color.White else GrimmDarkBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) Color.White else GrimmCyan,
                            modifier = Modifier.padding(end = 8.dp, start = 2.dp)
                        )
                        GrimmInlineMarkdownText(
                            rawText = block.text,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            isUser = isUser,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) Color.White else GrimmDarkBlue,
                            modifier = Modifier.width(22.dp)
                        )
                        GrimmInlineMarkdownText(
                            rawText = block.text,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            isUser = isUser,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Blockquote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isUser) Color.White.copy(alpha = 0.1f) else GrimmLightBlueSoft)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isUser) Color.White else GrimmLightBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        GrimmInlineMarkdownText(
                            rawText = block.text,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            fontStyle = FontStyle.Italic,
                            isUser = isUser,
                            textColor = if (isUser) Color.White else GrimmTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    GrimmCodeBlock(
                        code = block.code,
                        language = block.language
                    )
                }

                is MarkdownBlock.Table -> {
                    GrimmTable(table = block.table)
                }

                is MarkdownBlock.HorizontalRule -> {
                    HorizontalDivider(
                        color = if (isUser) Color.White.copy(alpha = 0.3f) else GrimmBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    if (index > 0 && blocks[index - 1] !is MarkdownBlock.Heading) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    GrimmInlineMarkdownText(
                        rawText = block.text,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        isUser = isUser
                    )
                }
            }
        }
    }
}
