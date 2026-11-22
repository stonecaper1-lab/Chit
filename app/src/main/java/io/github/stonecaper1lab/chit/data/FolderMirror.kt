package io.github.stonecaper1lab.chit.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One-way mirror: writes each live note as <slug>-<id>.md into a user-chosen folder (SAF). */
object FolderMirror {
    suspend fun mirror(resolver: ContentResolver, tree: Uri, notes: List<Note>): Int =
        withContext(Dispatchers.IO) {
            val rootId = DocumentsContract.getTreeDocumentId(tree)
            val rootDoc = DocumentsContract.buildDocumentUriUsingTree(tree, rootId)
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, rootId)

            val existing = HashMap<String, Uri>()
            resolver.query(
                children,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                ),
                null, null, null,
            )?.use { c ->
                while (c.moveToNext()) {
                    existing[c.getString(1)] =
                        DocumentsContract.buildDocumentUriUsingTree(tree, c.getString(0))
                }
            }

            var written = 0
            for (n in notes) {
                if (n.trashedAt != null) continue
                val name = noteFileName(n.id, n.title)
                val target = existing[name]
                    ?: DocumentsContract.createDocument(resolver, rootDoc, "application/octet-stream", name)
                    ?: continue
                resolver.openOutputStream(target, "wt")?.use { it.write(n.body.toByteArray(Charsets.UTF_8)) }
                written++
            }
            written
        }
}
