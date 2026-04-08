package com.middin.innovatie.app.data.local

import com.middin.innovatie.app.data.ChatRepository
import com.middin.innovatie.app.data.remote.dto.ChatMessageDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val CHAT_LOCALE: Locale = Locale.forLanguageTag("nl-NL")

/**
 * Chat volledig op het apparaat: berichten in Room, antwoorden uit [Product]-teksten.
 * Tijdstempels en systeemteksten zijn Nederlands, ongeacht de systeemtaal.
 */
class LocalProductChatRepository(
    private val chatDao: LocalChatMessageDao,
    private val productDao: ProductDao,
) : ChatRepository {

    override suspend fun listMessages(): Result<List<ChatMessageDto>> = runCatching {
        if (chatDao.count() == 0L) {
            chatDao.insert(welcomeRow())
        }
        chatDao.getAllOrdered().map { it.toDto() }
    }

    override suspend fun sendMessage(text: String): Result<Unit> = runCatching {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Leeg bericht." }
        val t0 = System.currentTimeMillis()
        chatDao.insert(
            LocalChatMessage(
                id = UUID.randomUUID().toString(),
                text = trimmed,
                authorName = AUTHOR_USER,
                createdAtEpochMs = t0,
            ),
        )
        val products = productDao.getAllOnce()
        val reply = ProductChatKnowledge.answer(trimmed, products)
        chatDao.insert(
            LocalChatMessage(
                id = UUID.randomUUID().toString(),
                text = reply,
                authorName = AUTHOR_BOT,
                createdAtEpochMs = t0 + 1,
            ),
        )
    }

    override suspend fun clearHistory(): Result<Unit> = runCatching {
        chatDao.deleteAll()
    }

    private fun welcomeRow(): LocalChatMessage {
        val text = buildString {
            appendLine("Hallo! Ik ben je productassistent.")
            appendLine()
            appendLine("Ik werk op je telefoon: je hebt geen internet nodig om mij te gebruiken.")
            appendLine()
            appendLine("Stel een vraag over de hulpmiddelen in deze app. Bijvoorbeeld:")
            appendLine("• «Wat doet de Somnox?»")
            appendLine("• «Welke producten zijn er?»")
            appendLine("• «Iets voor beter slapen?»")
            appendLine()
            append("Ik antwoord met eenvoudige taal op basis van de productinformatie hier in de app.")
        }.trim()
        return LocalChatMessage(
            id = "welcome-local-assistant",
            text = text,
            authorName = AUTHOR_BOT,
            createdAtEpochMs = System.currentTimeMillis(),
        )
    }

    private fun LocalChatMessage.toDto(): ChatMessageDto {
        val formatted = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", CHAT_LOCALE)
        val time = Instant.ofEpochMilli(createdAtEpochMs)
            .atZone(ZoneId.systemDefault())
            .format(formatted)
        return ChatMessageDto(
            id = id,
            text = text,
            createdAt = time,
            authorName = authorName,
        )
    }

    companion object {
        const val AUTHOR_USER = "Jij"
        const val AUTHOR_BOT = "Productassistent"
    }
}
