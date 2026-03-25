package com.middin.innovatie.app.ui.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.middin.innovatie.app.UserPreferencesRepository
import com.middin.innovatie.app.data.remote.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GeminiUiState(
    val loading: Boolean = false,
    val output: String? = null,
    val error: String? = null,
)

class GeminiViewModel(
    private val repository: GeminiRepository,
    private val userPreferences: UserPreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(GeminiUiState())
    val state: StateFlow<GeminiUiState> = _state.asStateFlow()

    fun runPrompt(prompt: String) {
        viewModelScope.launch {
            val key = userPreferences.geminiApiKey.first().orEmpty()
            _state.update { GeminiUiState(loading = true) }
            repository.generate(key, prompt)
                .onSuccess { text ->
                    _state.update { GeminiUiState(loading = false, output = text) }
                }
                .onFailure { e ->
                    _state.update {
                        GeminiUiState(loading = false, error = e.message ?: "Error")
                    }
                }
        }
    }

    companion object {
        fun factory(
            repository: GeminiRepository,
            userPreferences: UserPreferencesRepository,
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GeminiViewModel(repository, userPreferences) as T
        }
    }
}
