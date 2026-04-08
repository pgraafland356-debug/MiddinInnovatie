package com.middin.innovatie.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.middin.innovatie.app.data.ChatRepository
import com.middin.innovatie.app.data.remote.dto.ChatMessageDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val messages: List<ChatMessageDto> = emptyList(),
    val error: String? = null,
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(ChatUiState(isLoading = true))
    val ui: StateFlow<ChatUiState> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            chatRepository.listMessages()
                .onSuccess { list ->
                    _ui.update { it.copy(isLoading = false, messages = list, error = null) }
                }
                .onFailure { e ->
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            error = e.message?.ifBlank { null } ?: "Kon berichten niet laden.",
                        )
                    }
                }
        }
    }

    fun send(text: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isSending = true, error = null) }
            chatRepository.sendMessage(text)
                .onSuccess {
                    _ui.update { it.copy(isSending = false) }
                    refresh()
                }
                .onFailure { e ->
                    _ui.update {
                        it.copy(
                            isSending = false,
                            error = e.message?.ifBlank { null } ?: "Versturen is mislukt.",
                        )
                    }
                }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.clearHistory()
                .onSuccess { refresh() }
                .onFailure { e ->
                    _ui.update {
                        it.copy(
                            error = e.message?.ifBlank { null }
                                ?: "Geschiedenis wissen is mislukt.",
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(chatRepository: ChatRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChatViewModel(chatRepository) as T
        }
    }
}
