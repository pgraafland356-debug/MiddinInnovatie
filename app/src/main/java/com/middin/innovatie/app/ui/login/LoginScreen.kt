package com.middin.innovatie.app.ui.login

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import com.middin.innovatie.app.BuildConfig
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.components.MiddinLogoMark
import com.middin.innovatie.app.ui.i18n.LanguageToggleBar
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.ui.theme.MiddinDimens

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val container = rememberAppContainer()
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.factory(
            context.applicationContext as Application,
            container.authRepository,
            container.userPreferences,
        ),
    )
    val useLocalSignIn by container.userPreferences.useLocalSignIn.collectAsStateWithLifecycle(
        initialValue = BuildConfig.USE_LOCAL_SIGN_IN,
    )
    var user by rememberSaveable { mutableStateOf(BuildConfig.PRESET_LOGIN_USER) }
    var pass by rememberSaveable { mutableStateOf(BuildConfig.PRESET_LOGIN_PASS) }
    val state by viewModel.ui.collectAsStateWithLifecycle()

    val hPad = MiddinDimens.screenHorizontalPadding()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = hPad, vertical = MiddinDimens.screenVerticalPadding() + 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LanguageToggleBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )
        Spacer(Modifier.height(8.dp))
        MiddinLogoMark(size = 88.dp)
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.titleLarge)
        if (BuildConfig.DEBUG) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.settings_dev_signin_local),
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = useLocalSignIn,
                    onCheckedChange = { viewModel.setPreferLocalSignIn(it) },
                )
            }
            Text(
                stringResource(R.string.settings_dev_signin_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_username)) },
            singleLine = true,
            enabled = !state.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !state.isLoading,
        )
        if (state.emptyFields) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.login_error_empty_fields),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        state.errorMessage?.let { msg ->
            Spacer(Modifier.height(8.dp))
            Text(
                msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.login(user, pass) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(R.string.login_button))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (BuildConfig.DEBUG) {
            Text(
                stringResource(
                    if (useLocalSignIn) R.string.login_local_mode_hint
                    else R.string.login_server_mode_hint,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            stringResource(R.string.login_api_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
    }
}
