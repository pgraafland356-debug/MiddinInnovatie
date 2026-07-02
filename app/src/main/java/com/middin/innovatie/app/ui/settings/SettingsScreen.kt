package com.middin.innovatie.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.middin.innovatie.app.BuildConfig
import com.middin.innovatie.app.R
import com.middin.innovatie.app.UserPreferencesRepository
import com.middin.innovatie.app.notifications.NotificationHelper
import com.middin.innovatie.app.update.MinimalRelease
import com.middin.innovatie.app.update.PrivateAppUpdater
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.ui.theme.MiddinDimens
import com.middin.innovatie.app.ui.theme.ThemePreference
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(rememberAppContainer().userPreferences),
    ),
) {
    val container = rememberAppContainer()
    val ctx = LocalContext.current
    val resources = LocalResources.current
    val localeTag by container.userPreferences.localeTag.collectAsStateWithLifecycle(initialValue = "en")
    val themePref by container.userPreferences.themePreference.collectAsStateWithLifecycle(
        initialValue = ThemePreference.SYSTEM,
    )
    val overrideRaw by container.userPreferences.apiBaseUrlOverride.collectAsStateWithLifecycle(initialValue = null)
    val effectiveUrl by container.userPreferences.effectiveApiBaseUrl.collectAsStateWithLifecycle(
        initialValue = BuildConfig.API_BASE_URL.trimEnd('/'),
    )
    val geminiStored by container.userPreferences.geminiApiKey.collectAsStateWithLifecycle(initialValue = null)
    val updateFeedRaw by container.userPreferences.updateFeedUrlOverride.collectAsStateWithLifecycle(initialValue = null)
    val effectiveUpdateFeed by container.userPreferences.effectiveUpdateFeedUrl.collectAsStateWithLifecycle(initialValue = BuildConfig.UPDATE_FEED_URL)
    val useLocalSignIn by container.userPreferences.useLocalSignIn.collectAsStateWithLifecycle(
        initialValue = BuildConfig.DEBUG && BuildConfig.USE_LOCAL_SIGN_IN,
    )
    val username by container.userPreferences.username.collectAsStateWithLifecycle(initialValue = null)
    val showEndpointSettings = UserPreferencesRepository.canConfigureEndpoints(username)

    var serverDraft by remember(overrideRaw) { mutableStateOf(overrideRaw.orEmpty()) }
    var geminiDraft by remember(geminiStored) { mutableStateOf(geminiStored.orEmpty()) }
    var updateFeedDraft by remember(updateFeedRaw) { mutableStateOf(updateFeedRaw.orEmpty()) }
    var updateStatus by remember { mutableStateOf("") }
    var pendingRelease by remember { mutableStateOf<MinimalRelease?>(null) }
    var updateBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val updater = remember(ctx) { PrivateAppUpdater(ctx.applicationContext) }

    val notifPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) NotificationHelper.showTestNotification(ctx)
    }

    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            text = { Text(stringResource(R.string.logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.logout()
                    },
                ) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = MiddinDimens.screenHorizontalPadding(),
                vertical = MiddinDimens.screenVerticalPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            if (BuildConfig.DEBUG) {
                Text(
                    stringResource(R.string.settings_dev_signin_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.settings_dev_signin_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.settings_dev_signin_local),
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = useLocalSignIn,
                        onCheckedChange = { viewModel.setUseLocalSignIn(it) },
                    )
                }
            }
        }
        item {
            Text(stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                ThemeRow(
                    selected = themePref == ThemePreference.SYSTEM,
                    label = stringResource(R.string.settings_theme_system),
                    onSelect = { viewModel.setTheme(ThemePreference.SYSTEM) },
                )
                ThemeRow(
                    selected = themePref == ThemePreference.LIGHT,
                    label = stringResource(R.string.settings_theme_light),
                    onSelect = { viewModel.setTheme(ThemePreference.LIGHT) },
                )
                ThemeRow(
                    selected = themePref == ThemePreference.DARK,
                    label = stringResource(R.string.settings_theme_dark),
                    onSelect = { viewModel.setTheme(ThemePreference.DARK) },
                )
            }
        }
        if (showEndpointSettings) {
            item {
                Text(
                    stringResource(R.string.settings_gemini_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    stringResource(R.string.settings_gemini_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = geminiDraft,
                    onValueChange = { geminiDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text(stringResource(R.string.settings_gemini_key_label)) },
                    singleLine = false,
                    minLines = 2,
                )
                Button(
                    onClick = { viewModel.saveGeminiApiKey(geminiDraft) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.settings_gemini_save))
                }
            }
        }
        item {
            Text(
                stringResource(R.string.settings_notifications_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        val ok = ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (ok) {
                            NotificationHelper.showTestNotification(ctx)
                        } else {
                            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        NotificationHelper.showTestNotification(ctx)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.settings_test_notification))
            }
        }
        if (showEndpointSettings) {
            item {
                Text(
                    stringResource(R.string.settings_api_server_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    stringResource(R.string.settings_api_build_default, BuildConfig.API_BASE_URL),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = serverDraft,
                    onValueChange = { serverDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text(stringResource(R.string.settings_api_server_label)) },
                    placeholder = { Text(stringResource(R.string.settings_api_server_placeholder)) },
                    singleLine = false,
                    minLines = 2,
                )
                Text(
                    stringResource(R.string.settings_api_effective, effectiveUrl),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { viewModel.saveApiServerUrl(serverDraft) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.settings_api_save))
                    }
                    TextButton(
                        onClick = {
                            serverDraft = ""
                            viewModel.saveApiServerUrl("")
                        },
                    ) {
                        Text(stringResource(R.string.settings_api_use_default))
                    }
                }
                Text(
                    stringResource(R.string.settings_api_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Text(
                    stringResource(R.string.settings_update_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    stringResource(R.string.settings_update_current, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = updateFeedDraft,
                    onValueChange = { updateFeedDraft = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text(stringResource(R.string.settings_update_feed_label)) },
                    placeholder = { Text(stringResource(R.string.settings_update_feed_placeholder)) },
                    singleLine = false,
                    minLines = 2,
                )
                Text(
                    stringResource(R.string.settings_update_feed_effective, effectiveUpdateFeed.ifBlank { "-" }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { viewModel.saveUpdateFeedUrl(updateFeedDraft) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.settings_update_feed_save))
                    }
                    TextButton(
                        onClick = {
                            updateFeedDraft = ""
                            viewModel.saveUpdateFeedUrl("")
                        },
                    ) {
                        Text(stringResource(R.string.settings_api_use_default))
                    }
                }
                Button(
                    onClick = {
                        val endpoint = effectiveUpdateFeed.trim()
                        if (endpoint.isBlank()) {
                            updateStatus = resources.getString(R.string.settings_update_missing_url)
                            return@Button
                        }
                        updateBusy = true
                        updateStatus = resources.getString(R.string.settings_update_checking)
                        pendingRelease = null
                        scope.launch {
                            try {
                                val release = updater.fetchLatestRelease(endpoint)
                                if (release == null) {
                                    updateStatus = resources.getString(R.string.settings_update_none)
                                } else {
                                    pendingRelease = release
                                    updateStatus = resources.getString(
                                        R.string.settings_update_found,
                                        release.versionName,
                                        release.versionCode,
                                    )
                                }
                            } catch (_: Exception) {
                                updateStatus = resources.getString(R.string.settings_update_failed_check)
                            } finally {
                                updateBusy = false
                            }
                        }
                    },
                    enabled = !updateBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.settings_update_check_button))
                }
                if (pendingRelease != null) {
                    Button(
                        onClick = {
                            val release = pendingRelease ?: return@Button
                            updateBusy = true
                            updateStatus = resources.getString(R.string.settings_update_downloading)
                            scope.launch {
                                try {
                                    val apk = updater.downloadApk(release)
                                    val ok = updater.verifySha256(apk, release.sha256)
                                    if (!ok) {
                                        apk.delete()
                                        updateStatus = resources.getString(R.string.settings_update_checksum_failed)
                                        return@launch
                                    }
                                    if (!updater.canInstallUnknownApps()) {
                                        updater.openUnknownAppsSettings()
                                        updateStatus = resources.getString(R.string.settings_update_enable_unknown)
                                        return@launch
                                    }
                                    val launched = updater.promptInstall(apk)
                                    updateStatus = if (launched) {
                                        resources.getString(R.string.settings_update_install_prompted)
                                    } else {
                                        resources.getString(R.string.settings_update_install_failed)
                                    }
                                } catch (_: Exception) {
                                    updateStatus = resources.getString(R.string.settings_update_download_failed)
                                } finally {
                                    updateBusy = false
                                }
                            }
                        },
                        enabled = !updateBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        Text(stringResource(R.string.settings_update_install_button))
                    }
                }
                if (updateStatus.isNotBlank()) {
                    Text(
                        updateStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
        item {
            Text(
                stringResource(R.string.settings_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
            Column(Modifier.selectableGroup()) {
                LanguageRow(
                    selected = localeTag == "en",
                    label = stringResource(R.string.language_en),
                    onSelect = { viewModel.setLocale("en") },
                )
                LanguageRow(
                    selected = localeTag == "nl",
                    label = stringResource(R.string.language_nl),
                    onSelect = { viewModel.setLocale("nl") },
                )
            }
        }
        item {
            Text(
                stringResource(R.string.settings_security_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        item {
            Button(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 32.dp),
            ) {
                Text(stringResource(R.string.logout))
            }
        }
    }
}

@Composable
private fun ThemeRow(
    selected: Boolean,
    label: String,
    onSelect: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun LanguageRow(
    selected: Boolean,
    label: String,
    onSelect: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 12.dp))
    }
}
