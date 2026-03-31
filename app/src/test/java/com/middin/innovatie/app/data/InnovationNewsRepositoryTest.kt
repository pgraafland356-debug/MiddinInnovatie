package com.middin.innovatie.app.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InnovationNewsRepositoryTest {

    @Test
    fun fallbackFeedIsSortedNewestFirst() {
        val items = stubRepository().fallbackFeed()
        assertTrue(items.isNotEmpty())
        assertEquals(items, items.sortedByDescending { it.sortEpochMs })
    }

    @Test
    fun fallbackItemsHaveHttpsUrl() {
        val items = stubRepository().fallbackFeed()
        assertTrue(items.all { it.articleUrl.startsWith("https://") })
    }

    private fun stubRepository() = InnovationNewsRepository(
        httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { respond("") }
            }
        },
    )
}
