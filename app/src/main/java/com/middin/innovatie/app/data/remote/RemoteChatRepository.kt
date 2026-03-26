package com.middin.innovatie.app.data.remote

import com.middin.innovatie.app.UserPreferencesRepository
import com.middin.innovatie.app.data.ChatRepository
import com.middin.innovatie.app.data.remote.dto.ChatMessageDto
import com.middin.innovatie.app.data.remote.dto.PostChatMessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first

/** REST chat against `{base}/chat/messages` (e.g. local-api mock server). */
class RemoteChatRepository(
    private val client: HttpClient,
    private val baseUrlProvider: suspend () -> String,
    private val pathPrefix: String,
    private val userPreferences: UserPreferencesRepository,
) : ChatRepository {
    override suspend fun listMessages(): Result<List<ChatMessageDto>> =
        runNetworkResult {
            val token = userPreferences.authToken.first() ?: error("Not signed in.")
            val baseUrl = baseUrlProvider()
            val url = joinApiPath(baseUrl, pathPrefix, "chat/messages")
            val response = client.get(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (!response.status.isSuccess()) {
                val snippet = response.bodyAsText().take(500)
                error("HTTP ${response.status.value}: $snippet")
            }
            response.body()
        }

    override suspend fun sendMessage(text: String): Result<Unit> =
        runNetworkResult {
            val token = userPreferences.authToken.first() ?: error("Not signed in.")
            val trimmed = text.trim()
            require(trimmed.isNotEmpty()) { "Empty message." }
            val baseUrl = baseUrlProvider()
            val url = joinApiPath(baseUrl, pathPrefix, "chat/messages")
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(PostChatMessageRequest(text = trimmed))
            }
            if (!response.status.isSuccess()) {
                val snippet = response.bodyAsText().take(500)
                error("HTTP ${response.status.value}: $snippet")
            }
        }
}
