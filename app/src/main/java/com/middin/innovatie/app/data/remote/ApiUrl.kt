package com.middin.innovatie.app.data.remote

internal fun String.trimApiBaseUrl(): String = trimEnd('/')

/**
 * @param pathPrefix from BuildConfig, no leading/trailing slashes (e.g. `api/v1`).
 * @param path endpoint path (e.g. `auth/login`).
 */
internal fun joinApiPath(baseUrl: String, pathPrefix: String, path: String): String {
    val base = baseUrl.trimApiBaseUrl()
    val prefix = pathPrefix.trim().trim('/').let { if (it.isEmpty()) "" else "$it/" }
    val suffix = path.trimStart('/')
    return "$base/$prefix$suffix"
}
