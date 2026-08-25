package com.gogart.englishbuddy.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified
) {
    Text(
        text = parseMarkdown(text),
        modifier = modifier,
        style = style,
        color = color
    )
}

fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        
        // Regex for bold (**text**), italic (*text*), and code (`text`)
        val pattern = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`)""")
        val matches = pattern.findAll(text)
        
        matches.forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            val matchText = match.value
            
            // Append plain text before match
            append(text.substring(cursor, start))
            
            when {
                matchText.startsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchText.removeSurrounding("**"))
                    }
                }
                matchText.startsWith("*") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchText.removeSurrounding("*"))
                    }
                }
                matchText.startsWith("`") -> {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.2f))) {
                        append(matchText.removeSurrounding("`"))
                    }
                }
            }
            cursor = end
        }
        
        // Append remaining text
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
