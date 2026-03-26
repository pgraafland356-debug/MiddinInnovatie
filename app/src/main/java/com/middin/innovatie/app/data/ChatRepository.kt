package com.middin.innovatie.app.data

import com.middin.innovatie.app.data.remote.dto.ChatMessageDto

/** Team chat (remote) or product assistant (local). */
interface ChatRepository {
    suspend fun listMessages(): Result<List<ChatMessageDto>>
    suspend fun sendMessage(text: String): Result<Unit>
}
