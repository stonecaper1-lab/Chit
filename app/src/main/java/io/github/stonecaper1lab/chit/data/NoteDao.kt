package io.github.stonecaper1lab.chit.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query(
        """SELECT * FROM note
           WHERE trashedAt IS NULL AND archived = :archived
             AND (title LIKE '%' || :q || '%' ESCAPE '\' OR body LIKE '%' || :q || '%' ESCAPE '\')
           ORDER BY pinned DESC, updatedAt DESC"""
    )
    fun observe(archived: Boolean, q: String): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE trashedAt IS NOT NULL ORDER BY trashedAt DESC")
    fun observeTrash(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun get(id: Long): Note?

    @Query("SELECT * FROM note ORDER BY id")
    suspend fun all(): List<Note>

    @Upsert
    suspend fun upsert(note: Note): Long

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE note SET trashedAt = :t WHERE id = :id")
    suspend fun setTrashed(id: Long, t: Long?)

    @Query("DELETE FROM note WHERE trashedAt IS NOT NULL")
    suspend fun emptyTrash()

    @Query("DELETE FROM note WHERE trashedAt IS NOT NULL AND trashedAt < :cutoff")
    suspend fun purgeTrashedBefore(cutoff: Long)

    @Query("SELECT COUNT(*) FROM note WHERE title = :title AND body = :body")
    suspend fun countSame(title: String, body: String): Int
}
