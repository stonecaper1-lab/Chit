package io.github.stonecaper1lab.chit

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import io.github.stonecaper1lab.chit.ui.ChitRoot
import io.github.stonecaper1lab.chit.ui.NotesViewModel
import io.github.stonecaper1lab.chit.widget.QuickAddWidget

class MainActivity : ComponentActivity() {
    private val vm: NotesViewModel by viewModels { NotesViewModel.factory(application as ChitApp) }

    private var prompting = false
    private var autoPrompt = true

    private val unlockLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            prompting = false
            if (result.resultCode == RESULT_OK) vm.locked = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(vm.lockEnabled) {
                if (vm.lockEnabled) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
            ChitRoot(vm, onUnlock = ::requestUnlock)
        }
        if (savedInstanceState == null) {
            handle(intent)
            setIntent(Intent(Intent.ACTION_MAIN))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    override fun onResume() {
        super.onResume()
        if (vm.locked && autoPrompt && !prompting) requestUnlock()
    }

    override fun onStop() {
        super.onStop()
        vm.flush()
        vm.autoMirror()
        if (!prompting) {
            autoPrompt = true
            if (!isChangingConfigurations && vm.lockEnabled) vm.locked = true
        }
    }

    private fun handle(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> if (intent.type?.startsWith("text/") == true) {
                vm.openNote(
                    0,
                    intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty(),
                    intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString().orEmpty(),
                )
            }
            QuickAddWidget.ACTION_NEW -> vm.openNote(0)
            Intent.ACTION_PROCESS_TEXT -> {
                val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
                if (text.isNotBlank()) vm.openNote(0, "", text)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun requestUnlock() {
        if (prompting) return
        val km = getSystemService(KeyguardManager::class.java)
        val i = km.createConfirmDeviceCredentialIntent(getString(R.string.app_name), null)
        if (i == null) {
            vm.locked = false
            return
        }
        autoPrompt = false
        prompting = true
        unlockLauncher.launch(i)
    }
}
