package io.github.stonecaper1lab.chit.data

import android.content.ContentResolver
import android.net.Uri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Zip layout: notes/<slug>-<id>.md (body only) plus chit.json (full fidelity). */
object Backup {
    suspend fun export(resolver: ContentResolver, uri: Uri, notes: List<Note>) =
        withContext(Dispatchers.IO) {
            val out = resolver.openOutputStream(uri, "wt") ?: error("Cannot open destination")
            ZipOutputStream(BufferedOutputStream(out)).use { zip ->
                for (n in notes) {
                    zip.putNextEntry(ZipEntry("notes/" + noteFileName(n.id, n.title)))
                    zip.write(n.body.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                zip.putNextEntry(ZipEntry("chit.json"))
                zip.write(toJson(notes).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }

    suspend fun importZip(resolver: ContentResolver, uri: Uri, repo: NoteRepository): Int =
        withContext(Dispatchers.IO) {
            val files = LinkedHashMap<String, ByteArray>()
            val input = resolver.openInputStream(uri) ?: error("Cannot open file")
            ZipInputStream(BufferedInputStream(input)).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    if (!e.isDirectory) files[e.name] = readLimited(zip)
                }
            }
            val now = System.currentTimeMillis()
            val json = files["chit.json"]
            val notes: List<Note> = if (json != null) {
                fromJson(String(json, Charsets.UTF_8))
            } else {
                files.filterKeys { it.endsWith(".md") || it.endsWith(".txt") }
                    .map { (name, bytes) ->
                        Note(
                            title = titleFromFileName(name),
                            body = String(bytes, Charsets.UTF_8),
                            createdAt = now,
                            updatedAt = now,
                        )
                    }
            }
            var added = 0
            for (n in notes) if (repo.importNote(n)) added++
            added
        }

    private fun readLimited(zip: ZipInputStream, limit: Int = 8 * 1024 * 1024): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(8192)
        while (true) {
            val n = zip.read(buf)
            if (n < 0) break
            if (out.size() + n > limit) error("Backup entry too large")
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    private fun toJson(notes: List<Note>): String {
        val arr = JSONArray()
        for (n in notes) {
            arr.put(
                JSONObject()
                    .put("title", n.title)
                    .put("body", n.body)
                    .put("pinned", n.pinned)
                    .put("archived", n.archived)
                    .put("color", n.color)
                    .put("trashedAt", n.trashedAt ?: JSONObject.NULL)
                    .put("createdAt", n.createdAt)
                    .put("updatedAt", n.updatedAt)
            )
        }
        return JSONObject().put("version", 1).put("notes", arr).toString(2)
    }

    private fun fromJson(text: String): List<Note> {
        val arr = JSONObject(text).getJSONArray("notes")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Note(
                title = o.optString("title"),
                body = o.optString("body"),
                pinned = o.optBoolean("pinned"),
                archived = o.optBoolean("archived"),
                color = o.optInt("color"),
                trashedAt = if (o.isNull("trashedAt")) null else o.getLong("trashedAt"),
                createdAt = o.optLong("createdAt"),
                updatedAt = o.optLong("updatedAt"),
            )
        }
    }
}
