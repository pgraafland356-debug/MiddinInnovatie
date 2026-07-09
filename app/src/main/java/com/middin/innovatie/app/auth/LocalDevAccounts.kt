package com.middin.innovatie.app.auth

import android.content.Context
import org.json.JSONObject
import java.io.IOException

/** Offline dev accounts from assets/dev-accounts.json (source: dev-accounts/dev-accounts.json). */
object LocalDevAccounts {
  private const val ASSET_PATH = "dev-accounts.json"

  fun matches(context: Context, username: String, password: String): Boolean {
    if (username.isBlank() || password.isBlank()) return false
    val u = username.trim()
    return loadAccounts(context).any { (name, pass) ->
      name.equals(u, ignoreCase = true) && pass == password
    }
  }

  fun usernames(context: Context): List<String> =
    loadAccounts(context).map { it.first }

  private fun loadAccounts(context: Context): List<Pair<String, String>> {
    val json = try {
      context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
    } catch (_: IOException) {
      return emptyList()
    }
    return parseAccounts(json)
  }

  internal fun parseAccounts(jsonBody: String): List<Pair<String, String>> {
    val root = JSONObject(jsonBody.trim().removePrefix("\uFEFF"))
    val arr = root.optJSONArray("accounts") ?: return emptyList()
    val out = mutableListOf<Pair<String, String>>()
    for (i in 0 until arr.length()) {
      val item = arr.optJSONObject(i) ?: continue
      val username = item.optString("username", "").trim()
      val password = item.optString("password", "")
      if (username.isNotEmpty() && password.isNotEmpty()) {
        out.add(username to password)
      }
    }
    return out
  }
}
