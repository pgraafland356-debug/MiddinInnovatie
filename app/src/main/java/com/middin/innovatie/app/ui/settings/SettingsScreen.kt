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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.middin.innovatie.app.BuildConfig
import com.middin.innovatie.app.R
import com.middin.innovatie.app.notifications.NotificationHelper
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.ui.theme.ThemePreference

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(rememberAppContainer().userPreferences),
    ),
) {
    val container = rememberAppContainer()
    val ctx = LocalContext.current
    val localeTag by container.userPreferences.localeTag.collectAsStateWithLifecycle(initialValue = "en")
    val themePref by container.userPreferences.themePreference.collectAsStateWithLifecycle(
        initialValue = ThemePreference.SYSTEM,
    )
    val overrideRaw by container.userPreferences.apiBaseUrlOverride.collectAsStateWithLifecycle(initialValue = null)
    val effectiveUrl by container.userPreferences.effectiveApiBaseUrl.collectAsStateWithLifecycle(
        initialValue = BuildConfig.API_BASE_URL.trimEnd('/'),
    )
    val geminiStored by container.userPreferences.geminiApiKey.collectAsStateWithLifecycle(initialValue = null)

    var serverDraft by remember(overrideRaw) { mutableStateOf(overrideRaw.orEmpty()) }
    var geminiDraft by remember(geminiStored) { mutableStateOf(geminiStored.orEmpty()) }

    val notifPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) NotificationHelper.showTestNotification(ctx)
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
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
                onClick = { viewModel.logout() },
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
