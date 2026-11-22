package io.github.stonecaper1lab.chit.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
                    Text("Chit", style = MaterialTheme.typography.displaySmall)
                    Text("Locked", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onUnlock,
                        modifier = Modifier.height(56.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                    ) { Text("Unlock", style = MaterialTheme.typography.labelLarge) }
                }
            } else {
                // Expressive motion: springy scale-and-fade between screens.
                AnimatedContent(
                    targetState = vm.screen,
                    transitionSpec = {
                        val enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleIn(
                                initialScale = 0.94f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                            )
                        enter togetherWith fadeOut(spring(stiffness = Spring.StiffnessMedium))
                    },
                    contentKey = { if (it is Screen.Editor) "editor" else it },
                    label = "screen",
                ) { screen ->
                    when (screen) {
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
}
