package io.github.stonecaper1lab.chit.update

private const val TRUSTED_PREFIX = "https://github.com/stonecaper1-lab/Chit/releases/download/"

fun parseVersion(s: String): List<Int> =
    s.trim().removePrefix("v").removePrefix("V").split('.', '-').mapNotNull { it.toIntOrNull() }

fun isNewer(remote: String, local: String): Boolean {
    val r = parseVersion(remote)
    val l = parseVersion(local)
    for (i in 0 until maxOf(r.size, l.size)) {
        val a = r.getOrElse(i) { 0 }
        val b = l.getOrElse(i) { 0 }
        if (a != b) return a > b
    }
    return false
}

fun isTrustedUrl(url: String): Boolean = url.startsWith(TRUSTED_PREFIX)
