package io.github.stonecaper1lab.chit.ui

import android.Manifest
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 32.dp, top = 24.dp, bottom = 8.dp),
    )
}

/** Grouped, rounded container: the expressive take on a settings section. */
@Composable
private fun Group(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(vertical = 8.dp), content = content)
    }
}

@Composable
fun SettingsScreen(vm: NotesViewModel) {
    BackHandler { vm.go(Screen.Notes) }
    val context = LocalContext.current
    val keyguard = remember { context.getSystemService(KeyguardManager::class.java) }
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: ""
    }
    fun openUrl(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> if (uri != null) vm.exportBackup(uri) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) vm.importBackup(uri) }
    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri -> if (uri != null) vm.setMirrorFolder(uri) }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    Scaffold { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Header("Settings", onBack = { vm.go(Screen.Notes) })

            vm.message?.let {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            SectionTitle("Privacy")
            Group {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("App lock", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Unlock with your device PIN, pattern or biometrics. Also hides the app in recents.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = vm.lockEnabled,
                        onCheckedChange = { on ->
                            if (on && !keyguard.isDeviceSecure) {
                                vm.message = "Set a screen lock in system settings first"
                            } else {
                                vm.updateLock(on)
                            }
                        },
                    )
                }
            }

            SectionTitle("Backup")
            Group {
                Text(
                    "A zip with every note as a .md file plus a chit.json with full details.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { exportLauncher.launch("chit-backup-${LocalDate.now()}.zip") }) {
                        Text("Export")
                    }
                    FilledTonalButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) { Text("Import") }
                }
            }

            SectionTitle("Folder mirror")
            Group {
                Text(
                    "One-way copy of your notes as .md files into a folder you choose, e.g. one synced by Syncthing or Nextcloud. Files are never deleted by Chit.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Text(
                    vm.mirrorUri ?: "No folder chosen",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { folderLauncher.launch(null) }) { Text("Choose folder") }
                    if (vm.mirrorUri != null) {
                        FilledTonalButton(onClick = { vm.syncNow() }) { Text("Sync now") }
                        FilledTonalButton(onClick = { vm.stopMirror() }) { Text("Stop") }
                    }
                }
            }

            SectionTitle("Updates")
            Group {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Check for updates daily", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Off by default. Only contacts github.com to read the latest release. Tap the notification to download.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = vm.updateCheck,
                        onCheckedChange = { on ->
                            if (on && Build.VERSION.SDK_INT >= 33 &&
                                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            vm.changeUpdateCheck(on)
                        },
                    )
                }
                FilledTonalButton(
                    onClick = { vm.checkUpdatesNow() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) { Text("Check now") }
            }

            SectionTitle("About")
            Group {
                Text(
                    "Chit $version",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp),
                )
                Text(
                    "A tiny note-taking app that takes notes. Revolutionary, we know.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp, 4.dp, 16.dp, 8.dp),
                )
                Text(
                    "• No ads, no trackers, no accounts, no \"engagement metrics\".\n" +
                        "• Your notes live on this phone as plain Markdown you can export any time.\n" +
                        "• The only network use is the optional update check, off by default.\n" +
                        "• Free software under GPL-3.0-or-later. Read it, fork it, fix it.\n" +
                        "• Built with AI assistance (Claude), then tested and published by a human.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = { openUrl("https://github.com/stonecaper1-lab/Chit") }) {
                        Text("Source code")
                    }
                    FilledTonalButton(onClick = { openUrl("https://github.com/stonecaper1-lab/Chit/issues") }) {
                        Text("Report a bug")
                    }
                }
            }

            Text("", modifier = Modifier.padding(24.dp))
        }
    }
}
