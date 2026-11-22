package io.github.stonecaper1lab.chit

import android.app.Application
import androidx.room.Room
import io.github.stonecaper1lab.chit.data.ChitDatabase
import io.github.stonecaper1lab.chit.data.NoteRepository
import io.github.stonecaper1lab.chit.data.Prefs
import io.github.stonecaper1lab.chit.update.Updater

/** Manual DI: no Hilt, to keep the APK tiny. */
class ChitApp : Application() {
    val db: ChitDatabase by lazy {
        Room.databaseBuilder(this, ChitDatabase::class.java, "chit.db").build()
    }
    val repo: NoteRepository by lazy { NoteRepository(db.noteDao()) }
    val prefs: Prefs by lazy { Prefs(this) }

    override fun onCreate() {
        super.onCreate()
        if (prefs.updateCheck) Updater.ensureScheduled(this)
    }
}
