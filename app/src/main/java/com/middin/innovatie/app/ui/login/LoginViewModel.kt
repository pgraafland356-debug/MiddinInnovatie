package com.middin.innovatie.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _ui.value = LoginUiState(emptyFields = true)
            return
        }
        viewModelScope.launch {
            _ui.update { LoginUiState(isLoading = true) }
            authRepository.signIn(username.trim(), password)
                .onSuccess {
                    _ui.update { LoginUiState() }
                }
                .onFailure { e ->
                    _ui.update {
                        LoginUiState(
                            errorMessage = e.message?.ifBlank { null } ?: "Login failed.",
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LoginViewModel(authRepository) as T
        }
    }
}
