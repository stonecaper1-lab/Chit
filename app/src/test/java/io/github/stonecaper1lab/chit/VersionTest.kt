package io.github.stonecaper1lab.chit

import io.github.stonecaper1lab.chit.update.isNewer
import io.github.stonecaper1lab.chit.update.isTrustedUrl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionTest {
    @Test
    fun comparesVersions() {
        assertTrue(isNewer("v0.9.1", "0.9.0"))
        assertTrue(isNewer("0.10.0", "0.9.9"))
        assertTrue(isNewer("v1.0", "0.9.9"))
        assertFalse(isNewer("v0.9.0", "0.9.0"))
        assertFalse(isNewer("v0.9", "0.9.0"))
        assertFalse(isNewer("v0.8.9", "0.9.0"))
    }

    @Test
    fun onlyTrustsOwnReleaseUrls() {
        assertTrue(isTrustedUrl("https://github.com/stonecaper1-lab/Chit/releases/download/v1/Chit-v1.apk"))
        assertFalse(isTrustedUrl("http://github.com/stonecaper1-lab/Chit/releases/download/v1/a.apk"))
        assertFalse(isTrustedUrl("https://evil.example/stonecaper1-lab/Chit/releases/download/v1/a.apk"))
    }
}
