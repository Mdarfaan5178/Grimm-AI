package com.example.grimm.ui.markdown

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import java.util.regex.Pattern

/**
 * Syntax highlighter for Grimm AI code blocks.
 * Supports Kotlin, Java, Python, JavaScript, TypeScript, SQL, JSON, HTML, XML, Bash, and generic code.
 */
object GrimmSyntaxHighlighter {

    // Palette tuned for dark editor slate backgrounds (e.g. #0F172A)
    val ColorKeyword = Color(0xFFC084FC)      // Soft Violet / Purple
    val ColorType = Color(0xFF38BDF8)         // Sky Blue
    val ColorString = Color(0xFF4ADE80)       // Mint Green
    val ColorNumber = Color(0xFFFB923C)       // Amber / Orange
    val ColorComment = Color(0xFF94A3B8)      // Muted Slate
    val ColorFunction = Color(0xFFFDE047)     // Warm Yellow
    val ColorAnnotation = Color(0xFFF472B6)   // Rose Pink
    val ColorOperator = Color(0xFF94A3B8)     // Light Slate
    val ColorDefault = Color(0xFFE2E8F0)      // Slate Off-White

    private val kotlinKeywords = setOf(
        "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "!in", "is", "!is", "null", "object", "package", "return", "super",
        "this", "throw", "true", "try", "typealias", "val", "var", "when", "while",
        "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
        "get", "import", "init", "param", "property", "receiver", "set", "setparam",
        "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
        "data", "enum", "expect", "external", "final", "infix", "inline", "inner",
        "internal", "lateinit", "noinline", "open", "operator", "out", "override",
        "private", "protected", "public", "reified", "sealed", "suspend", "tailrec",
        "vararg", "yield"
    )

    private val pythonKeywords = setOf(
        "False", "None", "True", "and", "as", "assert", "async", "await", "break",
        "class", "continue", "def", "del", "elif", "else", "except", "finally", "for",
        "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
        "or", "pass", "raise", "return", "try", "while", "with", "yield", "self", "cls"
    )

    private val jsKeywords = setOf(
        "async", "await", "break", "case", "catch", "class", "const", "continue",
        "debugger", "default", "delete", "do", "else", "export", "extends", "finally",
        "for", "function", "if", "import", "in", "instanceof", "let", "new", "null",
        "return", "super", "switch", "this", "throw", "true", "false", "try", "typeof",
        "var", "void", "while", "with", "yield", "undefined", "interface", "type",
        "implements", "declare", "public", "private", "protected", "readonly"
    )

    private val sqlKeywords = setOf(
        "select", "from", "where", "insert", "update", "delete", "into", "values",
        "create", "table", "drop", "alter", "join", "left", "right", "inner", "outer",
        "full", "on", "group", "by", "order", "having", "limit", "offset", "as", "distinct",
        "and", "or", "not", "in", "is", "null", "like", "between", "primary", "key",
        "foreign", "references", "index", "count", "sum", "avg", "min", "max", "union",
        "case", "when", "then", "end", "asc", "desc"
    )

    private val commonTypes = setOf(
        "String", "Int", "Double", "Float", "Long", "Boolean", "Byte", "Short", "Char",
        "List", "MutableList", "Map", "MutableMap", "Set", "MutableSet", "Array", "Unit",
        "Any", "Nothing", "Promise", "Observable", "Flow", "StateFlow", "SharedFlow",
        "Composable", "Modifier", "Column", "Row", "Box", "Text", "Button", "Card"
    )

    /**
     * Highlights code text into an AnnotatedString using syntax token rules.
     */
    fun highlight(code: String, rawLanguage: String?): AnnotatedString {
        val lang = (rawLanguage ?: "").trim().lowercase()

        val keywords = when {
            lang in listOf("kt", "kotlin", "java") -> kotlinKeywords
            lang in listOf("py", "python") -> pythonKeywords
            lang in listOf("js", "jsx", "ts", "tsx", "javascript", "typescript") -> jsKeywords
            lang in listOf("sql") -> sqlKeywords
            lang in listOf("json") -> setOf("true", "false", "null")
            else -> kotlinKeywords + pythonKeywords + jsKeywords
        }

        return buildAnnotatedString {
            append(code)

            // 1. Strings: "..." or '...' or """..."""
            highlightRegex(
                code = code,
                regex = Regex("""\"\"\"[\s\S]*?\"\"\"|\"[^\"\n\r]*\"|'[^'\n\r]*'"""),
                style = SpanStyle(color = ColorString)
            )

            // 2. Comments: //... or #... or /*...*/
            val commentRegex = if (lang in listOf("py", "python", "sh", "bash")) {
                Regex("""#.*$""", RegexOption.MULTILINE)
            } else {
                Regex("""//.*$|/\*[\s\S]*?\*/""", RegexOption.MULTILINE)
            }
            highlightRegex(
                code = code,
                regex = commentRegex,
                style = SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic)
            )

            // 3. Numbers: integers, floats, hex
            highlightRegex(
                code = code,
                regex = Regex("""\b(0x[0-9a-fA-F]+|\d+(\.\d+)?([eE][+-]?\d+)?[fFL]?)\b"""),
                style = SpanStyle(color = ColorNumber, fontWeight = FontWeight.Medium)
            )

            // 4. Annotations/decorators (@Composable, @Test, @decorator)
            highlightRegex(
                code = code,
                regex = Regex("""@[a-zA-Z_]\w*"""),
                style = SpanStyle(color = ColorAnnotation, fontWeight = FontWeight.SemiBold)
            )

            // 5. Functions: functionName(...)
            highlightRegex(
                code = code,
                regex = Regex("""\b([a-zA-Z_]\w*)(?=\s*\()"""),
                style = SpanStyle(color = ColorFunction)
            )

            // 6. Keywords & Types
            val wordRegex = Regex("""\b([a-zA-Z_]\w*)\b""")
            for (match in wordRegex.findAll(code)) {
                val word = match.value
                val wordLower = word.lowercase()
                val range = match.range

                if (keywords.contains(word) || (lang == "sql" && keywords.contains(wordLower))) {
                    addStyle(
                        SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold),
                        range.first,
                        range.last + 1
                    )
                } else if (commonTypes.contains(word)) {
                    addStyle(
                        SpanStyle(color = ColorType, fontWeight = FontWeight.SemiBold),
                        range.first,
                        range.last + 1
                    )
                }
            }
        }
    }

    private fun AnnotatedString.Builder.highlightRegex(
        code: String,
        regex: Regex,
        style: SpanStyle
    ) {
        for (match in regex.findAll(code)) {
            addStyle(style, match.range.first, match.range.last + 1)
        }
    }

    fun normalizeLanguageName(rawLanguage: String?): String {
        val trimmed = (rawLanguage ?: "").trim().lowercase()
        return when (trimmed) {
            "kt", "kotlin" -> "Kotlin"
            "py", "python" -> "Python"
            "js", "javascript" -> "JavaScript"
            "ts", "typescript" -> "TypeScript"
            "java" -> "Java"
            "sql" -> "SQL"
            "json" -> "JSON"
            "html" -> "HTML"
            "xml" -> "XML"
            "css" -> "CSS"
            "sh", "bash", "shell" -> "Bash"
            "c" -> "C"
            "cpp", "c++" -> "C++"
            "cs", "csharp", "c#" -> "C#"
            "rust", "rs" -> "Rust"
            "go", "golang" -> "Go"
            "md", "markdown" -> "Markdown"
            "yaml", "yml" -> "YAML"
            "" -> "Code"
            else -> trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
