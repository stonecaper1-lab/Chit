package io.github.stonecaper1lab.chit.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun notes(q: String): Flow<List<Note>> = dao.observe(false, escapeLike(q.trim()))
    fun archived(q: String): Flow<List<Note>> = dao.observe(true, escapeLike(q.trim()))
    fun trash(): Flow<List<Note>> = dao.observeTrash()

    suspend fun get(id: Long): Note? = dao.get(id)
    suspend fun all(): List<Note> = dao.all()

    suspend fun save(note: Note): Note {
        val now = System.currentTimeMillis()
        val n = if (note.id == 0L) note.copy(createdAt = now, updatedAt = now) else note.copy(updatedAt = now)
        val rowId = dao.upsert(n)
        return if (n.id == 0L) n.copy(id = rowId) else n
    }

    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun restore(id: Long) = dao.setTrashed(id, null)
    suspend fun emptyTrash() = dao.emptyTrash()

    suspend fun purgeTrashOlderThan(ms: Long) =
        dao.purgeTrashedBefore(System.currentTimeMillis() - ms)

    /** Imports a note unless one with identical title and body already exists. */
    suspend fun importNote(n: Note): Boolean {
        if (dao.countSame(n.title, n.body) > 0) return false
        dao.upsert(n.copy(id = 0))
        return true
    }
}
