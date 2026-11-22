package io.github.stonecaper1lab.chit.ui

import android.app.KeyguardManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp, 20.dp, 16.dp, 4.dp),
    )
}

@Composable
fun SettingsScreen(vm: NotesViewModel) {
    BackHandler { vm.go(Screen.Notes) }
    val context = LocalContext.current
    val keyguard = remember { context.getSystemService(KeyguardManager::class.java) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri -> if (uri != null) vm.exportBackup(uri) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) vm.importBackup(uri) }
    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri -> if (uri != null) vm.setMirrorFolder(uri) }

    Scaffold { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Header("Settings", onBack = { vm.go(Screen.Notes) })

            SectionTitle("Privacy")
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("App lock")
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

            SectionTitle("Backup")
            Text(
                "A zip with every note as a .md file plus a chit.json with full details.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Row(Modifier.padding(16.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { exportLauncher.launch("chit-backup-${LocalDate.now()}.zip") }) {
                    Text("Export")
                }
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) { Text("Import") }
            }

            SectionTitle("Folder mirror")
            Text(
                "One-way copy of your notes as .md files into a folder you choose, e.g. one synced by Syncthing or Nextcloud. Files are never deleted by Chit.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                vm.mirrorUri ?: "No folder chosen",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp, 8.dp),
            )
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { folderLauncher.launch(null) }) { Text("Choose folder") }
                if (vm.mirrorUri != null) {
                    OutlinedButton(onClick = { vm.syncNow() }) { Text("Sync now") }
                    OutlinedButton(onClick = { vm.stopMirror() }) { Text("Stop") }
                }
            }

            vm.message?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp),
                )
            }

            SectionTitle("About")
            Text(
                "Chit is free software (GPL-3.0-or-later). No ads, no trackers, and no internet permission.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
