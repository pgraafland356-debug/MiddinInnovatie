package com.middin.innovatie.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogRepositoryTest {
    @Test
    fun changelog_static_history_not_empty() {
        assertTrue(ChangelogData.staticItems.isNotEmpty())
    }
}
