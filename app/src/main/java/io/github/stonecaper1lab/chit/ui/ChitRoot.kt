package io.github.stonecaper1lab.chit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.stonecaper1lab.chit.ui.theme.ChitTheme

@Composable
fun ChitRoot(vm: NotesViewModel, onUnlock: () -> Unit) {
    ChitTheme {
        Surface(Modifier.fillMaxSize()) {
            if (vm.locked) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Chit is locked", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onUnlock) { Text("Unlock") }
                }
            } else {
                when (vm.screen) {
                    Screen.Notes -> ListScreen(vm, Kind.NOTES)
                    Screen.Archive -> ListScreen(vm, Kind.ARCHIVE)
                    Screen.Trash -> ListScreen(vm, Kind.TRASH)
                    Screen.Settings -> SettingsScreen(vm)
                    is Screen.Editor -> EditorScreen(vm)
                }
            }
        }
    }
}
