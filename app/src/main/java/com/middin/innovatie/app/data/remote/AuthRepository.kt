package com.middin.innovatie.app.data.remote

import com.middin.innovatie.app.UserPreferencesRepository
import com.middin.innovatie.app.data.remote.dto.LoginEmailBody
import com.middin.innovatie.app.data.remote.dto.LoginResponse
import com.middin.innovatie.app.data.remote.dto.LoginUsernameBody
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthRepository(
    private val client: HttpClient,
    private val baseUrlProvider: suspend () -> String,
    private val pathPrefix: String,
    /** From BuildConfig: `"username"` or `"email"` (case-insensitive). */
    private val loginIdentifierField: String,
    private val userPreferences: UserPreferencesRepository,
) {
    suspend fun signIn(identifier: String, password: String): Result<Unit> =
        runNetworkResult {
            val baseUrl = baseUrlProvider()
            val url = joinApiPath(baseUrl, pathPrefix, "auth/login")
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    if (loginIdentifierField.equals("email", ignoreCase = true)) {
                        LoginEmailBody(email = identifier, password = password)
                    } else {
                        LoginUsernameBody(username = identifier, password = password)
                    },
                )
            }
            if (!response.status.isSuccess()) {
                val snippet = response.bodyAsText().take(500)
                error("HTTP ${response.status.value}: $snippet")
            }
            if (response.status == HttpStatusCode.NoContent) {
                error("Login succeeded but response had no JSON body; return a token from your API.")
            }
            val login: LoginResponse = response.body()
            val token = login.bearerToken()
                ?: error("Response missing token (expected token, accessToken, or access_token).")
            userPreferences.setAuthenticatedSession(username = identifier, token = token)
        }
}
