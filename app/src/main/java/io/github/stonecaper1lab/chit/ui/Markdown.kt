package io.github.stonecaper1lab.chit.ui

sealed interface Block {
    data class Heading(val level: Int, val text: String) : Block
    data class Bullet(val text: String) : Block
    data class Check(val checked: Boolean, val text: String, val line: Int) : Block
    data class Quote(val text: String) : Block
    data class Para(val text: String) : Block
    data object Blank : Block
}

fun parseMarkdown(src: String): List<Block> =
    src.split("\n").mapIndexed { i, raw -> parseLine(i, raw) }

private fun parseLine(i: Int, raw: String): Block {
    val line = raw.trimEnd()
    return when {
        line.isBlank() -> Block.Blank
        line == "- [ ]" || line.startsWith("- [ ] ") -> Block.Check(false, line.drop(6), i)
        line == "- [x]" || line == "- [X]" || line.startsWith("- [x] ") || line.startsWith("- [X] ") ->
            Block.Check(true, line.drop(6), i)
        line.startsWith("- ") || line.startsWith("* ") -> Block.Bullet(line.drop(2))
        line.startsWith("> ") -> Block.Quote(line.drop(2))
        line.startsWith("#") -> {
            val level = line.takeWhile { it == '#' }.length
            if (level in 1..6 && line.getOrNull(level) == ' ') Block.Heading(level, line.drop(level + 1))
            else Block.Para(line)
        }
        else -> Block.Para(line)
    }
}

fun toggleCheckLine(src: String, line: Int): String {
    val lines = src.split("\n").toMutableList()
    val l = lines.getOrNull(line) ?: return src
    lines[line] = when {
        l.startsWith("- [ ]") -> "- [x]" + l.drop(5)
        l.startsWith("- [x]") || l.startsWith("- [X]") -> "- [ ]" + l.drop(5)
        else -> l
    }
    return lines.joinToString("\n")
}
