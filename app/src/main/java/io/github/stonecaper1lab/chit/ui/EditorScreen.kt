package io.github.stonecaper1lab.chit.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private fun share(context: Context, title: String, body: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(send, null))
}

@Composable
fun EditorScreen(vm: NotesViewModel) {
    BackHandler { vm.closeEditor() }
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var colors by remember { mutableStateOf(false) }
    val transparent = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )

    Scaffold { pad ->
        Column(Modifier.padding(pad).fillMaxSize().imePadding()) {
            Header(title = "", onBack = { vm.closeEditor() }) {
                ActionButton(
                    label = if (vm.preview) "Edit" else "Preview",
                    description = if (vm.preview) "Switch to editing" else "Switch to preview",
                ) { vm.togglePreview() }
                Box {
                    ActionButton("⋮", "More options") { menu = true }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (vm.current.pinned) "Unpin" else "Pin") },
                            onClick = { menu = false; vm.togglePin() },
                        )
                        DropdownMenuItem(
                            text = { Text("Colour") },
                            onClick = { menu = false; colors = true },
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = { menu = false; share(context, vm.title, vm.body.text) },
                        )
                        DropdownMenuItem(
                            text = { Text(if (vm.current.archived) "Unarchive" else "Archive") },
                            onClick = { menu = false; vm.toggleArchive() },
                        )
                        DropdownMenuItem(
                            text = { Text("Move to trash") },
                            onClick = { menu = false; vm.trashCurrent() },
                        )
                    }
                }
            }

            if (!vm.editorReady) {
                Spacer(Modifier.weight(1f))
            } else if (vm.preview) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    if (vm.title.isNotBlank()) {
                        Text(
                            vm.title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                    MarkdownView(vm.body.text, onToggle = vm::toggleCheck)
                }
            } else {
                TextField(
                    value = vm.title,
                    onValueChange = vm::onTitle,
                    placeholder = { Text("Title") },
                    textStyle = MaterialTheme.typography.titleLarge,
                    colors = transparent,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = vm.body,
                    onValueChange = vm::onBody,
                    placeholder = { Text("Note (Markdown supported)") },
                    colors = transparent,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    ActionButton("☐ Checklist", "Add checklist item", vm::insertChecklist)
                }
            }
        }
    }

    if (colors) {
        AlertDialog(
            onDismissRequest = { colors = false },
            confirmButton = { TextButton(onClick = { colors = false }) { Text("Close") } },
            title = { Text("Colour") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    noteColors.indices.chunked(4).forEach { rowIdx ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowIdx.forEach { i ->
                                val selected = vm.current.color == i
                                Box(
                                    Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(noteColors[i] ?: MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .border(
                                            if (selected) 3.dp else 1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline,
                                            CircleShape,
                                        )
                                        .clickable(onClickLabel = noteColorNames[i]) {
                                            vm.setColor(i)
                                            colors = false
                                        }
                                        .semantics { contentDescription = noteColorNames[i] },
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}
