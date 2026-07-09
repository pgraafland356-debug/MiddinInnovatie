package com.middin.innovatie.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.middin.innovatie.app.R
import com.middin.innovatie.app.UserPreferencesRepository
import com.middin.innovatie.app.auth.LocalDevAccounts
import com.middin.innovatie.app.data.remote.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val emptyFields: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferencesRepository,
) : AndroidViewModel(application) {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun setPreferLocalSignIn(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setUseLocalSignIn(enabled)
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _ui.value = LoginUiState(emptyFields = true)
            return
        }
        viewModelScope.launch {
            _ui.update { LoginUiState(isLoading = true) }
            if (userPreferences.isLocalSignInEnabled()) {
                runCatching {
                    val u = username.trim()
                    val app = getApplication<Application>()
                    check(LocalDevAccounts.matches(app, u, password)) {
                        app.getString(R.string.login_error_local_credentials)
                    }
                    userPreferences.setAuthenticatedSession(
                        username = u,
                        token = "local-dev-token",
                    )
                }.fold(
                    onSuccess = { _ui.update { LoginUiState() } },
                    onFailure = { e ->
                        _ui.update {
                            LoginUiState(
                                errorMessage = e.message?.takeUnless { it.isBlank() }
                                    ?: getApplication<Application>().getString(R.string.login_failed),
                            )
                        }
                    },
                )
            } else {
                authRepository.signIn(username.trim(), password)
                    .onSuccess {
                        _ui.update { LoginUiState() }
                    }
                    .onFailure { e ->
                        _ui.update {
                            LoginUiState(
                                errorMessage = e.message?.ifBlank { null }
                                    ?: getApplication<Application>().getString(R.string.login_failed),
                            )
                        }
                    }
            }
        }
    }

    companion object {
        fun factory(
            application: Application,
            authRepository: AuthRepository,
            userPreferences: UserPreferencesRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LoginViewModel(application, authRepository, userPreferences) as T
        }
    }
}
