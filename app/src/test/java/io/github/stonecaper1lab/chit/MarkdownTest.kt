package io.github.stonecaper1lab.chit

import io.github.stonecaper1lab.chit.ui.Block
import io.github.stonecaper1lab.chit.ui.parseMarkdown
import io.github.stonecaper1lab.chit.ui.toggleCheckLine
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownTest {
    @Test
    fun parsesBlocks() {
        val b = parseMarkdown("# Title\n- [ ] milk\n- [x] eggs\n- item\n> quote\ntext\n")
        assertEquals(Block.Heading(1, "Title"), b[0])
        assertEquals(Block.Check(false, "milk", 1), b[1])
        assertEquals(Block.Check(true, "eggs", 2), b[2])
        assertEquals(Block.Bullet("item"), b[3])
        assertEquals(Block.Quote("quote"), b[4])
        assertEquals(Block.Para("text"), b[5])
        assertEquals(Block.Blank, b[6])
    }

    @Test
    fun hashWithoutSpaceIsParagraph() {
        assertEquals(Block.Para("#tag"), parseMarkdown("#tag")[0])
    }

    @Test
    fun emptyCheckItemIsStillAChecklist() {
        assertEquals(Block.Check(false, "", 0), parseMarkdown("- [ ]")[0])
    }

    @Test
    fun togglesChecklistLines() {
        assertEquals("- [x] a\n- [ ] b", toggleCheckLine("- [ ] a\n- [ ] b", 0))
        assertEquals("- [ ] a", toggleCheckLine("- [x] a", 0))
        assertEquals("plain", toggleCheckLine("plain", 0))
        assertEquals("plain", toggleCheckLine("plain", 5))
    }
}
