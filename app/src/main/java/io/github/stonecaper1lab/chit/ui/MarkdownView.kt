package io.github.stonecaper1lab.chit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

fun inlineStyle(s: String, codeBg: Color): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < s.length) {
        if (s.startsWith("**", i)) {
            val e = s.indexOf("**", i + 2)
            if (e != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(s.substring(i + 2, e)) }
                i = e + 2
                continue
            }
        }
        if (s[i] == '*') {
            val e = s.indexOf('*', i + 1)
            if (e != -1 && e > i + 1) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(s.substring(i + 1, e)) }
                i = e + 1
                continue
            }
        }
        if (s[i] == '`') {
            val e = s.indexOf('`', i + 1)
            if (e != -1 && e > i + 1) {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg)) {
                    append(s.substring(i + 1, e))
                }
                i = e + 1
                continue
            }
        }
        append(s[i])
        i++
    }
}

@Composable
fun MarkdownView(src: String, onToggle: (Int) -> Unit, modifier: Modifier = Modifier) {
    val blocks = remember(src) { parseMarkdown(src) }
    val codeBg = MaterialTheme.colorScheme.surfaceVariant
    Column(modifier) {
        for (b in blocks) {
            when (b) {
                is Block.Heading -> Text(
                    text = inlineStyle(b.text, codeBg),
                    style = when (b.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
                is Block.Bullet -> Row(Modifier.padding(start = 8.dp)) {
                    Text("•", modifier = Modifier.padding(end = 8.dp))
                    Text(inlineStyle(b.text, codeBg))
                }
                is Block.Check -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = b.checked, onCheckedChange = { onToggle(b.line) })
                    Text(
                        text = inlineStyle(b.text, codeBg),
                        textDecoration = if (b.checked) TextDecoration.LineThrough else null,
                    )
                }
                is Block.Quote -> Row(Modifier.height(IntrinsicSize.Min).padding(vertical = 2.dp)) {
                    Spacer(
                        Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = inlineStyle(b.text, codeBg),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                is Block.Para -> Text(inlineStyle(b.text, codeBg))
                Block.Blank -> Spacer(Modifier.height(8.dp))
            }
        }
    }
}
