package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GroundingSource
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CodeBlockBackground
import com.example.ui.theme.CodeBlockBorder
import com.example.ui.theme.CodeBlockText
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.ElectricPurpleLight
import com.example.ui.theme.MathBlockBackground
import com.example.ui.theme.MathBlockBorder
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import org.json.JSONArray

@Composable
fun FormattedAssistantMessage(
    text: String,
    sourcesJson: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cleanedText = text
        .replace(Regex("(?is)<(thought|thinking|reasoning|reflection|inner_monologue)[^>]*>.*?(</\\1>|$)"), "")
        .replace(Regex("(?is)```(?:thought|thinking|reasoning|scratchpad|inner_monologue)[\\s\\S]*?```"), "")
        .replace(Regex("(?is)^(#+\\s*)?(\\*{1,2})?(thinking process|thought process|internal reasoning|my thinking|thoughts):?(\\*{1,2})?.*?((\\n\\s*(\\*{1,2})?(final answer|answer|response):?(\\*{1,2})?\\s*\\n+)|(?=\\n\\n[A-Z#\\*]))"), "")
        .replace(Regex("(?im)^(#+\\s*)?(\\*{1,2})?(thinking process|thought process|internal reasoning|my thinking):?(\\*{1,2})?\\s*$"), "")
        .trim()

    val parsedBlocks = parseMarkdownBlocks(cleanedText)

    val sources = rememberSources(sourcesJson)

    Column(modifier = modifier.fillMaxWidth()) {
        parsedBlocks.forEach { block ->
            when (block) {
                is Block.Heading -> {
                    HeadingView(block)
                }
                is Block.Code -> {
                    CodeBlockView(block, context)
                }
                is Block.MathExpression -> {
                    MathBlockView(block)
                }
                is Block.BulletItem -> {
                    BulletItemView(block)
                }
                is Block.NumberedItem -> {
                    NumberedItemView(block)
                }
                is Block.TableBlock -> {
                    TableView(block)
                }
                is Block.Paragraph -> {
                    ParagraphView(block)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Web Grounding Sources
        if (sources.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Web Sources",
                    modifier = Modifier.size(16.dp),
                    tint = BrightCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Verified Web Sources & Citations",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricPurple
                )
            }

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sources.forEach { source ->
                    SourceBadge(source = source, context = context)
                }
            }
        }
    }
}

@Composable
fun SourceBadge(source: GroundingSource, context: Context) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape)
            .background(Color(0xFFF1F5F9))
            .border(0.8.dp, Color(0xFFCBD5E1), shape)
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.uri))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open: ${source.uri}", Toast.LENGTH_SHORT).show()
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("source_badge")
    ) {
        Text(
            text = source.title.take(30),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2563EB)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open link",
            modifier = Modifier.size(12.dp),
            tint = Color(0xFF64748B)
        )
    }
}

private fun rememberSources(sourcesJson: String?): List<GroundingSource> {
    if (sourcesJson.isNullOrBlank()) return emptyList()
    return try {
        val array = JSONArray(sourcesJson)
        val list = mutableListOf<GroundingSource>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                GroundingSource(
                    title = obj.optString("title", "Web Source"),
                    uri = obj.optString("uri", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}

@Composable
fun HeadingView(block: Block.Heading) {
    val size = when (block.level) {
        1 -> 20.sp
        2 -> 18.sp
        else -> 16.sp
    }
    Text(
        text = block.text,
        fontSize = size,
        fontWeight = FontWeight.Bold,
        color = ElectricPurple,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
fun CodeBlockView(block: Block.Code, context: Context) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CodeBlockBackground)
            .border(1.dp, CodeBlockBorder, shape)
    ) {
        // Code Block Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = block.language.ifBlank { "code" }.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = BrightCyan
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("code", block.code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("copy_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Copy",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = block.code,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = CodeBlockText
            )
        }
    }
}

@Composable
fun MathBlockView(block: Block.MathExpression) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MathBlockBackground)
            .border(1.2.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = "Math formula",
            tint = ElectricPurple,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = block.expression,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E1B4B)
        )
    }
}

@Composable
fun BulletItemView(block: Block.BulletItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(ElectricPurpleLight)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseInlineMarkdown(block.text),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun NumberedItemView(block: Block.NumberedItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${block.number}.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricPurple,
            modifier = Modifier.width(22.dp)
        )
        Text(
            text = parseInlineMarkdown(block.text),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun TableView(block: Block.TableBlock) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .horizontalScroll(rememberScrollState())
    ) {
        block.rows.forEachIndexed { index, row ->
            val isHeader = index == 0
            Row(
                modifier = Modifier
                    .background(if (isHeader) Color(0xFFF1F5F9) else if (index % 2 == 0) Color.White else Color(0xFFF8FAFC))
                    .padding(8.dp)
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell.trim(),
                        fontSize = 12.sp,
                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                        color = if (isHeader) ElectricPurple else TextPrimary,
                        modifier = Modifier
                            .width(120.dp)
                            .padding(horizontal = 6.dp)
                    )
                }
            }
            if (index < block.rows.size - 1) {
                Divider(color = Color(0xFFE2E8F0), thickness = 0.8.dp)
            }
        }
    }
}

@Composable
fun ParagraphView(block: Block.Paragraph) {
    Text(
        text = parseInlineMarkdown(block.text),
        fontSize = 14.5.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    )
}

// Inline Markdown Parser: **bold**, *italic*, `inline code`
fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0xFFF1F5F9),
                                color = Color(0xFF7C3AED),
                                fontSize = 13.sp
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = TextSecondary)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

// Blocks definition
sealed class Block {
    data class Heading(val level: Int, val text: String) : Block()
    data class Code(val language: String, val code: String) : Block()
    data class MathExpression(val expression: String) : Block()
    data class BulletItem(val text: String) : Block()
    data class NumberedItem(val number: String, val text: String) : Block()
    data class TableBlock(val rows: List<List<String>>) : Block()
    data class Paragraph(val text: String) : Block()
}

fun parseMarkdownBlocks(input: String): List<Block> {
    val blocks = mutableListOf<Block>()
    val lines = input.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // 1. Code blocks ```
        if (line.trim().startsWith("```")) {
            val rawLang = line.trim().removePrefix("```").trim()
            val language = rawLang.lowercase(java.util.Locale.ROOT)
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            // CRITICAL: Suppress all internal thinking, reasoning, reflection or scratchpad blocks
            val isThinking = language in listOf("thought", "thinking", "reasoning", "internal", "scratchpad", "reflection", "monologue", "analysis", "trace")
            if (isThinking) {
                i++
                continue
            }

            val codeContent = codeLines.joinToString("\n").trim()
            if (codeContent.isBlank()) {
                i++
                continue
            }

            // Check if this code block starts with thinking phrases
            if (codeContent.startsWith("Thinking Process:", ignoreCase = true) ||
                codeContent.startsWith("Thought Process:", ignoreCase = true) ||
                codeContent.startsWith("Thought:", ignoreCase = true) ||
                codeContent.startsWith("Internal reasoning:", ignoreCase = true)
            ) {
                i++
                continue
            }

            // If language is generic (or empty, "text", "txt", "markdown") and doesn't look like actual programming code:
            val isExplicitCodeLang = language in listOf(
                "kotlin", "java", "python", "py", "javascript", "js", "typescript", "ts",
                "c", "cpp", "c++", "csharp", "c#", "go", "rust", "swift", "php", "ruby",
                "html", "css", "sql", "shell", "bash", "sh", "json", "xml", "yaml", "yml"
            )

            if (!isExplicitCodeLang) {
                val hasCodeKeywords = codeContent.contains(Regex("\\b(fun |val |var |class |def |import |return |function |public |private |const |let |console\\.log|print\\(|SELECT |FROM |WHERE |<div|<html)"))
                val hasSentencesOrFormatting = codeContent.contains(". ") || codeContent.contains("? ") || codeContent.contains("\n- ") || codeContent.contains("\n* ") || codeContent.startsWith("#")
                
                // If it looks like normal prose/explanation rather than programming code, unwrap it so it does NOT appear like code!
                if (!hasCodeKeywords || hasSentencesOrFormatting) {
                    val innerBlocks = parseMarkdownBlocks(codeContent)
                    blocks.addAll(innerBlocks)
                    i++
                    continue
                }
            }

            blocks.add(Block.Code(language = rawLang.ifBlank { "code" }, code = codeLines.joinToString("\n")))
            i++
            continue
        }

        // 2. Math blocks $$ ... $$ or [Math: ...]
        if (line.trim().startsWith("$$") && line.trim().endsWith("$$") && line.trim().length > 4) {
            val expr = line.trim().removeSurrounding("$$").trim()
            blocks.add(Block.MathExpression(expr))
            i++
            continue
        }

        // 3. Headings #, ##, ###
        if (line.startsWith("### ")) {
            blocks.add(Block.Heading(level = 3, text = line.removePrefix("### ").trim()))
            i++
            continue
        }
        if (line.startsWith("## ")) {
            blocks.add(Block.Heading(level = 2, text = line.removePrefix("## ").trim()))
            i++
            continue
        }
        if (line.startsWith("# ")) {
            blocks.add(Block.Heading(level = 1, text = line.removePrefix("# ").trim()))
            i++
            continue
        }

        // 4. Bullet list (* or - )
        if (line.trim().startsWith("* ") || line.trim().startsWith("- ")) {
            val text = line.trim().substring(2).trim()
            blocks.add(Block.BulletItem(text))
            i++
            continue
        }

        // 5. Numbered list (e.g. 1. )
        val numberedRegex = Regex("^(\\d+)\\.\\s+(.+)$")
        val matchNumbered = numberedRegex.find(line.trim())
        if (matchNumbered != null) {
            val num = matchNumbered.groupValues[1]
            val text = matchNumbered.groupValues[2]
            blocks.add(Block.NumberedItem(num, text))
            i++
            continue
        }

        // 6. Tables (lines with |)
        if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
            val tableRows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                val currentTableLine = lines[i].trim()
                // Ignore markdown separator row |---|---|
                if (!currentTableLine.contains("---")) {
                    val cells = currentTableLine.split("|")
                        .filterIndexed { idx, _ -> idx != 0 && idx != currentTableLine.split("|").lastIndex }
                    tableRows.add(cells)
                }
                i++
            }
            if (tableRows.isNotEmpty()) {
                blocks.add(Block.TableBlock(tableRows))
            }
            continue
        }

        // 7. Regular paragraph
        if (line.isNotBlank()) {
            blocks.add(Block.Paragraph(line.trim()))
        }
        i++
    }

    return blocks
}
