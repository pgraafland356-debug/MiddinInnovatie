package com.middin.innovatie.app.data.rss

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RssPubDateParserTest {

    @Test
    fun parseOrNull_returnsNullForBlank() {
        assertNull(RssPubDateParser.parseToEpochMillisOrNull(null))
        assertNull(RssPubDateParser.parseToEpochMillisOrNull(""))
        assertNull(RssPubDateParser.parseToEpochMillisOrNull("   "))
    }

    @Test
    fun parseOrNull_parsesRfc1123() {
        val ms = RssPubDateParser.parseToEpochMillisOrNull("Mon, 01 Jan 2024 12:00:00 GMT")
        assertNotNull(ms)
        assertTrue(ms!! > 0)
    }
}
