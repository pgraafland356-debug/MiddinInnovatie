package com.middin.innovatie.app.ui.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.update.MinimalRelease
import com.middin.innovatie.app.update.PrivateAppUpdater
import kotlinx.coroutines.launch

@Composable
fun UpdateAvailablePrompt() {
    val container = rememberAppContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updater = remember { PrivateAppUpdater(context.applicationContext) }
    var pending by remember { mutableStateOf<MinimalRelease?>(null) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val feed = container.userPreferences.resolvedUpdateFeedUrl().trim()
        if (feed.isBlank()) return@LaunchedEffect
        try {
            val release = updater.fetchLatestRelease(feed) ?: return@LaunchedEffect
            val dismissed = container.userPreferences.getUpdateNoticeDismissedCode()
            if (release.versionCode > dismissed) {
                pending = release
            }
        } catch (_: Exception) {
        }
    }

    val release = pending
    if (release != null && status == null) {
        AlertDialog(
            onDismissRequest = {
                scope.launch {
                    container.userPreferences.setUpdateNoticeDismissedCode(release.versionCode)
                    pending = null
                }
            },
            title = { Text(stringResource(R.string.update_prompt_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.update_prompt_message,
                        release.versionName.ifBlank { release.versionCode.toString() },
                        release.versionCode,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            try {
                                val apk = updater.downloadApk(release)
                                val ok = updater.verifySha256(apk, release.sha256)
                                if (!ok) {
                                    apk.delete()
                                    status = context.getString(R.string.settings_update_checksum_failed)
                                    return@launch
                                }
                                if (!updater.canInstallUnknownApps()) {
                                    updater.openUnknownAppsSettings()
                                    status = context.getString(R.string.settings_update_enable_unknown)
                                    return@launch
                                }
                                updater.promptInstall(apk)
                                container.userPreferences.setUpdateNoticeDismissedCode(release.versionCode)
                                pending = null
                            } catch (_: Exception) {
                                status = context.getString(R.string.settings_update_download_failed)
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.update_prompt_install))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        scope.launch {
                            container.userPreferences.setUpdateNoticeDismissedCode(release.versionCode)
                            pending = null
                        }
                    },
                ) {
                    Text(stringResource(R.string.update_prompt_later))
                }
            },
        )
    }

    status?.let { message ->
        AlertDialog(
            onDismissRequest = { status = null },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { status = null }) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
        )
    }
}
