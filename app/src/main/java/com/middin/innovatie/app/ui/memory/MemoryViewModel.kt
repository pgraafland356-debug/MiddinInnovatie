package com.middin.innovatie.app.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.middin.innovatie.app.data.local.MemoryDao
import com.middin.innovatie.app.data.local.MemoryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoryViewModel(
    private val dao: MemoryDao,
) : ViewModel() {
    val items: StateFlow<List<MemoryEntry>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(content: String, author: String) {
        val text = content.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            dao.insert(
                MemoryEntry(
                    content = text,
                    authorName = author.ifBlank { "user" },
                ),
            )
        }
    }

    fun remove(id: String) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    companion object {
        fun factory(dao: MemoryDao) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MemoryViewModel(dao) as T
        }
    }
}
