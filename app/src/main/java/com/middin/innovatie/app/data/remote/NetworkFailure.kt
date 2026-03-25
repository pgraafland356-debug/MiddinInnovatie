package com.middin.innovatie.app.data.remote

import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Maps transport / parsing failures to short user-facing messages.
 * Preserves explicit API-layer messages from repositories (HTTP snippets, validation).
 */
internal fun Throwable.toUserFacingOrSelf(): Throwable {
    if (this is CancellationException) return this
    val msg = message
    if (!msg.isNullOrBlank() && shouldPreserveMessage(msg)) return this
    val friendly = when (this) {
        is UnknownHostException ->
            "Cannot reach the server. Check the API URL in Settings and your network."
        is ConnectException ->
            "Connection refused. Is the API server running and reachable?"
        is SocketTimeoutException,
        is HttpRequestTimeoutException ->
            "The request timed out. Try again or check the server."
        is SSLException ->
            "Secure connection failed. Use https:// with a valid certificate."
        is SerializationException ->
            "Could not read the server response. Check that the API returns the expected JSON."
        is IOException ->
            msg?.takeIf { it.isNotBlank() } ?: "Network error. Check your connection."
        else ->
            msg?.takeIf { it.isNotBlank() } ?: "Something went wrong."
    }
    return Exception(friendly, this)
}

private fun shouldPreserveMessage(msg: String): Boolean {
    if (msg.startsWith("HTTP ")) return true
    if (msg.contains("token", ignoreCase = true)) return true
    if (msg.contains("Login succeeded but", ignoreCase = true)) return true
    if (msg.contains("Empty message", ignoreCase = true)) return true
    if (msg.contains("Not signed in", ignoreCase = true)) return true
    if (msg.contains("Add your Gemini API key", ignoreCase = true)) return true
    if (msg.contains("Enter a prompt", ignoreCase = true)) return true
    if (msg.contains("Empty model response", ignoreCase = true)) return true
    return false
}

internal suspend fun <T> runNetworkResult(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        Result.failure(e.toUserFacingOrSelf())
    }
