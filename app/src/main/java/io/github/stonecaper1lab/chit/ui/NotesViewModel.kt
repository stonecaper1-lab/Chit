package io.github.stonecaper1lab.chit.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.stonecaper1lab.chit.ChitApp
import io.github.stonecaper1lab.chit.data.Backup
import io.github.stonecaper1lab.chit.data.FolderMirror
import io.github.stonecaper1lab.chit.data.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface Screen {
    data object Notes : Screen
    data object Archive : Screen
    data object Trash : Screen
    data object Settings : Screen
    data class Editor(val id: Long) : Screen
}

private class Session(var note: Note)

class NotesViewModel(private val app: ChitApp) : ViewModel() {
    private val repo = app.repo
    private val prefs = app.prefs

    // ---- navigation ----
    var screen: Screen by mutableStateOf(Screen.Notes)
        private set
    private var returnTo: Screen = Screen.Notes

    fun go(s: Screen) {
        query = ""
        searching = false
        screen = s
    }

    // ---- lists + search ----
    var query by mutableStateOf("")
    var searching by mutableStateOf(false)
        private set

    fun startSearch() { searching = true }
    fun closeSearch() { searching = false; query = "" }

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = snapshotFlow { query }
        .flatMapLatest { repo.notes(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val archived: StateFlow<List<Note>> = snapshotFlow { query }
        .flatMapLatest { repo.archived(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trash: StateFlow<List<Note>> = repo.trash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repo.purgeTrashOlderThan(30L * 24 * 60 * 60 * 1000) }
    }

    fun restore(id: Long) { viewModelScope.launch { repo.restore(id) } }
    fun deleteForever(id: Long) { viewModelScope.launch { repo.delete(id) } }
    fun emptyTrash() { viewModelScope.launch { repo.emptyTrash() } }

    // ---- editor ----
    var title by mutableStateOf("")
        private set
    var body by mutableStateOf(TextFieldValue(""))
        private set
    var preview by mutableStateOf(false)
        private set
    var editorReady by mutableStateOf(false)
        private set
    var current by mutableStateOf(Note())
        private set

    private var session = Session(Note())
    private val mutex = Mutex()
    private var saveJob: Job? = null

    fun openNote(id: Long, presetTitle: String = "", presetBody: String = "") {
        if (screen is Screen.Editor && editorReady) {
            saveJob?.cancel()
            commit(session, title, body.text)
        } else if (screen !is Screen.Editor) {
            returnTo = screen
        }
        editorReady = false
        preview = false
        screen = Screen.Editor(id)
        viewModelScope.launch {
            val n = if (id == 0L) Note() else repo.get(id) ?: Note()
            session = Session(n)
            current = n
            title = if (id == 0L) presetTitle else n.title
            body = TextFieldValue(if (id == 0L) presetBody else n.body)
            editorReady = true
            if (presetTitle.isNotBlank() || presetBody.isNotBlank()) scheduleSave()
        }
    }

    fun onTitle(s: String) { title = s; scheduleSave() }
    fun onBody(v: TextFieldValue) { body = v; scheduleSave() }
    fun togglePreview() { preview = !preview }

    fun toggleCheck(line: Int) {
        body = TextFieldValue(toggleCheckLine(body.text, line))
        scheduleSave()
    }

    fun insertChecklist() {
        val v = body
        val pos = v.selection.start.coerceIn(0, v.text.length)
        val atLineStart = pos == 0 || v.text[pos - 1] == '\n'
        val ins = (if (atLineStart) "" else "\n") + "- [ ] "
        body = TextFieldValue(v.text.substring(0, pos) + ins + v.text.substring(pos), TextRange(pos + ins.length))
        scheduleSave()
    }

    fun togglePin() {
        session.note = session.note.copy(pinned = !session.note.pinned)
        current = session.note
        scheduleSave()
    }

    fun setColor(i: Int) {
        session.note = session.note.copy(color = i)
        current = session.note
        scheduleSave()
    }

    fun toggleArchive() {
        session.note = session.note.copy(archived = !session.note.archived)
        current = session.note
        closeEditor()
    }

    fun trashCurrent() {
        saveJob?.cancel()
        val s = session
        val t = title
        val b = body.text
        screen = returnTo
        viewModelScope.launch {
            mutex.withLock {
                if (s.note.id != 0L || t.isNotBlank() || b.isNotBlank()) {
                    repo.save(s.note.copy(title = t, body = b, trashedAt = System.currentTimeMillis()))
                }
            }
        }
    }

    fun closeEditor() {
        saveJob?.cancel()
        if (editorReady) commit(session, title, body.text)
        screen = returnTo
    }

    /** Called from Activity.onStop so nothing typed is lost. */
    fun flush() {
        if (screen is Screen.Editor && editorReady) {
            saveJob?.cancel()
            val s = session
            val t = title
            val b = body.text
            viewModelScope.launch { persist(s, t, b, closing = false) }
        }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        val s = session
        saveJob = viewModelScope.launch {
            delay(600)
            if (s === session) persist(s, title, body.text, closing = false)
        }
    }

    private fun commit(s: Session, t: String, b: String) {
        viewModelScope.launch { persist(s, t, b, closing = true) }
    }

    private suspend fun persist(s: Session, t: String, b: String, closing: Boolean) {
        mutex.withLock {
            if (t.isBlank() && b.isBlank()) {
                if (closing && s.note.id != 0L) {
                    repo.delete(s.note.id)
                    s.note = s.note.copy(id = 0)
                }
            } else {
                val saved = withContext(NonCancellable) { repo.save(s.note.copy(title = t, body = b)) }
                s.note = saved
                if (s === session) current = saved
            }
        }
    }

    // ---- settings: lock, backup, mirror ----
    var message by mutableStateOf<String?>(null)

    var lockEnabled by mutableStateOf(prefs.lockEnabled)
        private set
    var locked by mutableStateOf(prefs.lockEnabled)

    fun updateLock(v: Boolean) {
        prefs.lockEnabled = v
        lockEnabled = v
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            message = runCatching {
                Backup.export(app.contentResolver, uri, repo.all())
                "Backup saved"
            }.getOrElse { "Export failed: ${it.message}" }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            message = runCatching {
                val n = Backup.importZip(app.contentResolver, uri, repo)
                "Imported $n new notes"
            }.getOrElse { "Import failed: ${it.message}" }
        }
    }

    var mirrorUri by mutableStateOf(prefs.mirrorUri)
        private set

    private val rw = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    fun setMirrorFolder(uri: Uri) {
        runCatching { app.contentResolver.takePersistableUriPermission(uri, rw) }
        prefs.mirrorUri = uri.toString()
        mirrorUri = uri.toString()
        syncNow()
    }

    fun stopMirror() {
        mirrorUri?.let { runCatching { app.contentResolver.releasePersistableUriPermission(Uri.parse(it), rw) } }
        prefs.mirrorUri = null
        mirrorUri = null
    }

    fun autoMirror() { if (mirrorUri != null) syncNow(silent = true) }

    fun syncNow(silent: Boolean = false) {
        val tree = mirrorUri ?: return
        viewModelScope.launch {
            val r = runCatching { FolderMirror.mirror(app.contentResolver, Uri.parse(tree), repo.all()) }
            if (!silent) {
                message = r.fold(
                    onSuccess = { "Synced $it notes to folder" },
                    onFailure = { "Sync failed: ${it.message}" },
                )
            }
        }
    }

    companion object {
        fun factory(app: ChitApp) = viewModelFactory { initializer { NotesViewModel(app) } }
    }
}
