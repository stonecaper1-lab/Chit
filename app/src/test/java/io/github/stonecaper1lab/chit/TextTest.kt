package io.github.stonecaper1lab.chit

import io.github.stonecaper1lab.chit.data.escapeLike
import io.github.stonecaper1lab.chit.data.noteFileName
import io.github.stonecaper1lab.chit.data.slugify
import io.github.stonecaper1lab.chit.data.titleFromFileName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextTest {
    @Test
    fun slugifies() {
        assertEquals("hello-world", slugify("Hello, World!"))
        assertEquals("note", slugify(""))
        assertEquals("note", slugify("***"))
        assertTrue(slugify("a".repeat(100)).length <= 40)
    }

    @Test
    fun buildsFileNames() {
        assertEquals("shopping-list-7.md", noteFileName(7, "Shopping List"))
    }

    @Test
    fun recoversTitlesFromFileNames() {
        assertEquals("shopping list", titleFromFileName("notes/shopping-list-12.md"))
        assertEquals("12", titleFromFileName("12.md"))
        assertEquals("Imported note", titleFromFileName("-5.md"))
    }

    @Test
    fun escapesLikeWildcards() {
        assertEquals("50\\%\\_x\\\\", escapeLike("50%_x\\"))
    }
}
