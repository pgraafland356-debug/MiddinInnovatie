package com.middin.innovatie.app.data.rss

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Minimal RSS 2.0 parser (channel/item). Handles CDATA and simple nested HTML in descriptions.
 */
object RssParser {

    fun parseItems(xml: String): List<ParsedRssItem> {
        if (xml.isBlank()) return emptyList()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val p = factory.newPullParser()
        p.setInput(StringReader(xml))
        val out = mutableListOf<ParsedRssItem>()
        while (p.eventType != XmlPullParser.END_DOCUMENT) {
            if (p.eventType == XmlPullParser.START_TAG && p.name == "item") {
                out.add(parseItem(p))
            }
            p.next()
        }
        return out
    }

    private fun parseItem(p: XmlPullParser): ParsedRssItem {
        p.require(XmlPullParser.START_TAG, null, "item")
        var title = ""
        var link = ""
        var description = ""
        var pubDate: String? = null
        var event = p.next()
        while (!(event == XmlPullParser.END_TAG && p.name == "item")) {
            if (event == XmlPullParser.START_TAG) {
                when (p.name) {
                    "title" -> title = readTaggedText(p, "title")
                    "link" -> link = readTaggedText(p, "link")
                    "description" -> description = readTaggedText(p, "description")
                    "pubDate" -> pubDate = readTaggedText(p, "pubDate")
                    "encoded" -> {
                        if (description.isEmpty()) {
                            description = readTaggedText(p, "encoded")
                        } else {
                            skip(p)
                        }
                    }
                    else -> skip(p)
                }
            }
            event = p.next()
        }
        return ParsedRssItem(
            title = title.trim(),
            link = link.trim(),
            descriptionPlain = stripHtml(description),
            pubDateRaw = pubDate?.trim()?.ifBlank { null },
        )
    }

    private fun readTaggedText(p: XmlPullParser, tag: String): String {
        p.require(XmlPullParser.START_TAG, null, tag)
        val sb = StringBuilder()
        var event = p.next()
        while (!(event == XmlPullParser.END_TAG && p.name == tag)) {
            when (event) {
                XmlPullParser.TEXT, XmlPullParser.CDSECT -> sb.append(p.text)
                XmlPullParser.START_TAG -> skip(p)
            }
            event = p.next()
        }
        return sb.toString()
    }

    private fun skip(p: XmlPullParser) {
        if (p.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth != 0) {
            when (p.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
        }
    }

    fun stripHtml(html: String): String =
        html.replace(Regex("<[^>]+>"), " ")
            .replace(Regex("&nbsp;"), " ")
            .replace(Regex("&amp;"), "&")
            .replace(Regex("&lt;"), "<")
            .replace(Regex("&gt;"), ">")
            .replace(Regex("&quot;"), "\"")
            .replace(Regex("\\s+"), " ")
            .trim()
}
