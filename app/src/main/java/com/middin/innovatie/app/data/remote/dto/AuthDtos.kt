package com.middin.innovatie.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * POST `{prefix}auth/login` — the app sends either [LoginUsernameBody] or [LoginEmailBody] only,
 * depending on `API_LOGIN_FIELD` in `app/build.gradle.kts` (`username` or `email`).
 */
@Serializable
data class LoginUsernameBody(
    val username: String,
    val password: String,
)

@Serializable
data class LoginEmailBody(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    val accessToken: String? = null,
    @SerialName("access_token")
    val access_token: String? = null,
) {
    fun bearerToken(): String? = token ?: accessToken ?: access_token
}
