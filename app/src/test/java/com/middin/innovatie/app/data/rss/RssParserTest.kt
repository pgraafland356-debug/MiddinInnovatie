package com.middin.innovatie.app.data.rss

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssParserTest {

    @Test
    fun parsesChannelWithTwoItems() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Test</title>
                <item>
                  <title>First &amp; title</title>
                  <link>https://example.com/a</link>
                  <description><![CDATA[<p>Hello <b>world</b></p>]]></description>
                  <pubDate>Mon, 01 Jan 2024 12:00:00 GMT</pubDate>
                </item>
                <item>
                  <title>Second</title>
                  <link>https://example.com/b</link>
                  <description>Plain</description>
                </item>
              </channel>
            </rss>
        """.trimIndent()

        val items = RssParser.parseItems(xml)
        assertEquals(2, items.size)
        assertEquals("First & title", items[0].title)
        assertEquals("https://example.com/a", items[0].link)
        assertEquals("Hello world", items[0].descriptionPlain)
        assertEquals("Mon, 01 Jan 2024 12:00:00 GMT", items[0].pubDateRaw)
        assertEquals("Second", items[1].title)
        assertTrue(items[1].pubDateRaw == null)
    }
}
