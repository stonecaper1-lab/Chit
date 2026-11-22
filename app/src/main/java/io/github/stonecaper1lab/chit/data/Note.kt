package io.github.stonecaper1lab.chit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val body: String = "",
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val color: Int = 0,
    val trashedAt: Long? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)
