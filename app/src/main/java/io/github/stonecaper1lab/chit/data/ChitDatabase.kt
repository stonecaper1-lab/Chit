package io.github.stonecaper1lab.chit.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class ChitDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
