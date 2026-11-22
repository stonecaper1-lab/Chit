package io.github.stonecaper1lab.chit.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class Kind { NOTES, ARCHIVE, TRASH }

@Composable
private fun SearchRow(vm: NotesViewModel) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Row(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            placeholder = { Text("Search notes") },
            singleLine = true,
            modifier = Modifier.weight(1f).focusRequester(focus),
        )
        TextButton(onClick = { vm.closeSearch() }) { Text("Close") }
    }
}

@Composable
fun ListScreen(vm: NotesViewModel, kind: Kind) {
    val flow = when (kind) {
        Kind.NOTES -> vm.notes
        Kind.ARCHIVE -> vm.archived
        Kind.TRASH -> vm.trash
    }
    val notes by flow.collectAsStateWithLifecycle()
    var menu by remember { mutableStateOf(false) }
    var confirmEmpty by remember { mutableStateOf(false) }

    BackHandler(enabled = kind != Kind.NOTES || vm.searching) {
        if (vm.searching) vm.closeSearch() else vm.go(Screen.Notes)
    }

    val title = when (kind) {
        Kind.NOTES -> "Chit"
        Kind.ARCHIVE -> "Archive"
        Kind.TRASH -> "Trash"
    }
    val back: (() -> Unit)? = if (kind == Kind.NOTES) null else ({ vm.go(Screen.Notes) })
    val emptyText = when (kind) {
        Kind.NOTES -> if (vm.query.isNotBlank()) "No matches" else "No notes yet. Tap + to write one."
        Kind.ARCHIVE -> if (vm.query.isNotBlank()) "No matches" else "Nothing archived"
        Kind.TRASH -> "Trash is empty"
    }

    Scaffold(
        floatingActionButton = {
            if (kind == Kind.NOTES) {
                LargeFloatingActionButton(
                    onClick = { vm.openNote(0) },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.semantics { contentDescription = "New note" },
                ) { Text("+", style = MaterialTheme.typography.displaySmall) }
            }
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (vm.searching) {
                SearchRow(vm)
            } else {
                Header(title, onBack = back) {
                    if (kind != Kind.TRASH) ActionButton("Search", "Search notes") { vm.startSearch() }
                    if (kind == Kind.TRASH && notes.isNotEmpty()) {
                        ActionButton("Empty", "Empty trash") { confirmEmpty = true }
                    }
                    if (kind == Kind.NOTES) {
                        Box {
                            ActionButton("⋮", "More options") { menu = true }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Archive") },
                                    onClick = { menu = false; vm.go(Screen.Archive) },
                                )
                                DropdownMenuItem(
                                    text = { Text("Trash") },
                                    onClick = { menu = false; vm.go(Screen.Trash) },
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { menu = false; vm.go(Screen.Settings) },
                                )
                            }
                        }
                    }
                }
            }

            if (kind == Kind.TRASH) {
                Text(
                    "Notes in trash are deleted after 30 days.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(emptyText) }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 128.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(notes, key = { it.id }) { n ->
                        NoteCard(
                            note = n,
                            onClick = if (kind != Kind.TRASH) ({ vm.openNote(n.id) }) else null,
                            modifier = Modifier.animateItem(),
                        ) {
                            if (kind == Kind.TRASH) {
                                Row {
                                    TextButton(onClick = { vm.restore(n.id) }) { Text("Restore") }
                                    TextButton(onClick = { vm.deleteForever(n.id) }) { Text("Delete forever") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmEmpty) {
        AlertDialog(
            onDismissRequest = { confirmEmpty = false },
            title = { Text("Empty trash?") },
            text = { Text("All notes in the trash will be deleted permanently.") },
            confirmButton = {
                TextButton(onClick = { confirmEmpty = false; vm.emptyTrash() }) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmEmpty = false }) { Text("Cancel") }
            },
        )
    }
}
