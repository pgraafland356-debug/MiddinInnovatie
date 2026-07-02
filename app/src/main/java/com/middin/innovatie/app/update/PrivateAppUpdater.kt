package com.middin.innovatie.app.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.middin.innovatie.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class MinimalRelease(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String,
    val changelog: String = "",
)

class PrivateAppUpdater(
    private val context: Context,
) {
    suspend fun fetchLatestRelease(endpointUrl: String): MinimalRelease? = withContext(Dispatchers.IO) {
        val conn = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
        }
        try {
            if (conn.responseCode !in 200..299) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            ReleaseManifest.parseAndroidRelease(body, BuildConfig.VERSION_CODE)
        } finally {
            conn.disconnect()
        }
    }

    suspend fun downloadApk(release: MinimalRelease): File = withContext(Dispatchers.IO) {
        val outFile = File(context.cacheDir, "update-${release.versionCode}.apk")
        URL(release.apkUrl).openStream().use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
        outFile
    }

    suspend fun verifySha256(file: File, expectedSha: String): Boolean = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = fis.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        actual.equals(expectedSha.trim(), ignoreCase = true)
    }

    fun canInstallUnknownApps(): Boolean = context.packageManager.canRequestPackageInstalls()

    fun openUnknownAppsSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${context.packageName}".toUri(),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun promptInstall(apkFile: File): Boolean {
        val authority = "${context.packageName}.fileprovider"
        val apkUri = FileProvider.getUriForFile(context, authority, apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
