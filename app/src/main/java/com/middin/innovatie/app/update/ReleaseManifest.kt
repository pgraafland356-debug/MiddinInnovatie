package com.middin.innovatie.app.update

import org.json.JSONObject

/**
 * Unified release manifest for Android + Windows (hosted on GitHub raw or any HTTPS URL).
 *
 * Example: releases/latest.json in the repo.
 */
object ReleaseManifest {

    fun parseAndroidRelease(jsonBody: String, currentVersionCode: Int): MinimalRelease? {
        val root = JSONObject(jsonBody.trim().removePrefix("\uFEFF"))
        val versionCode = root.optInt("versionCode", -1)
        if (versionCode <= currentVersionCode) return null

        if (root.has("android")) {
            val android = root.getJSONObject("android")
            return MinimalRelease(
                versionCode = versionCode,
                versionName = root.optString("versionName", ""),
                apkUrl = android.getString("apkUrl"),
                sha256 = android.getString("sha256"),
                changelog = root.optString("changelog", ""),
            )
        }

        // Legacy flat JSON (older feeds).
        return MinimalRelease(
            versionCode = versionCode,
            versionName = root.optString("versionName", ""),
            apkUrl = root.getString("apkUrl"),
            sha256 = root.getString("sha256"),
            changelog = root.optString("changelog", ""),
        )
    }
}
