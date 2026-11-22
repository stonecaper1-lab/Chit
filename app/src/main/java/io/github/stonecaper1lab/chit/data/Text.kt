package io.github.stonecaper1lab.chit.data

fun slugify(s: String): String {
    val t = buildString {
        for (c in s.lowercase()) append(if (c.isLetterOrDigit()) c else '-')
    }
    val collapsed = t.replace(Regex("-+"), "-").trim('-').take(40).trim('-')
    return collapsed.ifEmpty { "note" }
}

fun noteFileName(id: Long, title: String): String = "${slugify(title)}-$id.md"

fun titleFromFileName(path: String): String {
    val base = path.substringAfterLast('/').substringBeforeLast('.')
    return base.replace(Regex("-\\d+$"), "").replace('-', ' ').trim().ifEmpty { "Imported note" }
}

fun escapeLike(q: String): String =
    q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
