package com.middin.innovatie.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogRepositoryTest {
    @Test
    fun changelog_json_has_history() {
        val stream = javaClass.classLoader.getResourceAsStream("changelog.json")
        assertTrue(stream != null)
        val entries = ChangelogJson.parse(stream!!.bufferedReader().use { it.readText() })
        assertTrue(entries.isNotEmpty())
    }
}
