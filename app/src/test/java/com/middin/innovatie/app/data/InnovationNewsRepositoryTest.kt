package com.middin.innovatie.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InnovationNewsRepositoryTest {

    @Test
    fun feedIsSortedNewestFirst() {
        val items = InnovationNewsRepository().getFeed()
        assertTrue(items.isNotEmpty())
        assertEquals(items, items.sortedByDescending { it.sortEpochMs })
    }

    @Test
    fun allItemsHaveHttpsUrl() {
        val items = InnovationNewsRepository().getFeed()
        assertTrue(items.all { it.articleUrl.startsWith("https://") })
    }
}
