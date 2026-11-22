package io.github.stonecaper1lab.chit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.stonecaper1lab.chit.data.Note

val noteColors = listOf<Color?>(
    null, Color(0xFFE57373), Color(0xFFFFB74D), Color(0xFFFFF176),
    Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFBA68C8),
)
val noteColorNames = listOf("Default", "Red", "Orange", "Yellow", "Green", "Blue", "Purple")

@Composable
fun noteContainer(color: Int): Color {
    val base = noteColors.getOrNull(color)
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh
    return if (base == null) surface else base.copy(alpha = 0.28f).compositeOver(surface)
}

@Composable
fun ActionButton(label: String, description: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = description },
    ) { Text(label) }
}

@Composable
fun Header(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) ActionButton("←", "Back", onBack)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        actions()
    }
}

@Composable
fun NoteCard(note: Note, onClick: (() -> Unit)?, footer: @Composable () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = noteContainer(note.color)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
    ) {
        Column(Modifier.padding(12.dp)) {
            if (note.pinned) {
                Text("Pinned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (note.title.isNotBlank()) {
                Text(
                    note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (note.body.isNotBlank()) {
                Text(
                    note.body,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            footer()
        }
    }
}
