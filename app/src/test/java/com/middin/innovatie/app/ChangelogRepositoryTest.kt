package com.middin.innovatie.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogRepositoryTest {
    @Test
    fun changelog_lists_phase1() {
        assertTrue(ChangelogRepository().items.isNotEmpty())
    }
}
