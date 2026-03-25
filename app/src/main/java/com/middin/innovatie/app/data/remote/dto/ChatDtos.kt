package com.middin.innovatie.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * GET `{API_PATH_PREFIX}chat/messages` — JSON array of message objects.
 * Map your server fields via [ChatMessageDto.displayText] helpers.
 */
@Serializable
data class ChatMessageDto(
    val id: String? = null,
    val text: String? = null,
    val body: String? = null,
    val content: String? = null,
    val sentAt: String? = null,
    val createdAt: String? = null,
    val author: String? = null,
    val authorName: String? = null,
) {
    fun displayText(): String = listOfNotNull(text, body, content).firstOrNull { it.isNotBlank() } ?: ""

    fun displayAuthor(): String = listOfNotNull(author, authorName).firstOrNull { it.isNotBlank() } ?: "—"

    fun displayTime(): String = listOfNotNull(sentAt, createdAt).firstOrNull { it.isNotBlank() } ?: ""
}

@Serializable
data class PostChatMessageRequest(
    val text: String,
)
