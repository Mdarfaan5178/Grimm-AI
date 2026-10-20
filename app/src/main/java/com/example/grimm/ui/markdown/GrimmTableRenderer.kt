package com.example.grimm.ui.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GrimmBorder
import com.example.ui.theme.GrimmDarkBlue
import com.example.ui.theme.GrimmLightBlueSoft
import com.example.ui.theme.GrimmSurface
import com.example.ui.theme.GrimmTextPrimary

data class MarkdownTable(
    val headers: List<String>,
    val alignments: List<TextAlign>,
    val rows: List<List<String>>
)

object GrimmTableParser {
    fun parse(lines: List<String>): MarkdownTable? {
        if (lines.size < 2) return null

        val headerLine = lines[0].trim()
        val separatorLine = lines[1].trim()

        if (!headerLine.contains("|") || !separatorLine.contains("|")) return null

        val headers = splitTableRow(headerLine)
        val separators = splitTableRow(separatorLine)

        if (headers.isEmpty() || separators.isEmpty()) return null

        // Validate separator line contains dashes
        val isSeparatorValid = separators.all { cell ->
            val stripped = cell.replace(":", "").replace("-", "").trim()
            stripped.isEmpty() && cell.contains("-")
        }
        if (!isSeparatorValid) return null

        val alignments = separators.map { cell ->
            val trimmed = cell.trim()
            val startsWithColon = trimmed.startsWith(":")
            val endsWithColon = trimmed.endsWith(":")
            when {
                startsWithColon && endsWithColon -> TextAlign.Center
                endsWithColon -> TextAlign.End
                else -> TextAlign.Start
            }
        }

        val rows = mutableListOf<List<String>>()
        for (i in 2 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank() || !line.contains("|")) continue
            val rowCells = splitTableRow(line)
            if (rowCells.isNotEmpty()) {
                // Pad with empty strings if row has fewer cells than headers
                val padded = if (rowCells.size < headers.size) {
                    rowCells + List(headers.size - rowCells.size) { "" }
                } else {
                    rowCells.take(headers.size)
                }
                rows.add(padded)
            }
        }

        return MarkdownTable(
            headers = headers,
            alignments = alignments,
            rows = rows
        )
    }

    private fun splitTableRow(line: String): List<String> {
        var trimmed = line.trim()
        if (trimmed.startsWith("|")) trimmed = trimmed.substring(1)
        if (trimmed.endsWith("|")) trimmed = trimmed.substring(0, trimmed.length - 1)
        return trimmed.split("|").map { it.trim() }
    }
}

@Composable
fun GrimmTable(
    table: MarkdownTable,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = GrimmSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GrimmBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("markdown_table")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                // Header Row
                Row(
                    modifier = Modifier
                        .background(GrimmLightBlueSoft)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    table.headers.forEachIndexed { colIdx, header ->
                        val align = table.alignments.getOrElse(colIdx) { TextAlign.Start }
                        Box(
                            modifier = Modifier
                                .widthIn(min = 90.dp, max = 220.dp)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = header,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrimmDarkBlue,
                                textAlign = align,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                HorizontalDivider(color = GrimmBorder, thickness = 1.dp)

                // Data Rows
                table.rows.forEachIndexed { rowIdx, rowCells ->
                    val rowBg = if (rowIdx % 2 == 0) Color.White else Color(0xFFF8FAFC)
                    Row(
                        modifier = Modifier
                            .background(rowBg)
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowCells.forEachIndexed { colIdx, cellText ->
                            val align = table.alignments.getOrElse(colIdx) { TextAlign.Start }
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 90.dp, max = 220.dp)
                                    .padding(horizontal = 12.dp)
                            ) {
                                GrimmInlineMarkdownText(
                                    rawText = cellText,
                                    fontSize = 11.5.sp,
                                    textAlign = align,
                                    isUser = false
                                )
                            }
                        }
                    }

                    if (rowIdx < table.rows.size - 1) {
                        HorizontalDivider(color = GrimmBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
