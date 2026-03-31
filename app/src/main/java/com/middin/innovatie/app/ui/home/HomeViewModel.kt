package com.middin.innovatie.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.middin.innovatie.app.data.InnovationNewsItem
import com.middin.innovatie.app.data.InnovationNewsRepository
import com.middin.innovatie.app.data.local.ProductDao
import com.middin.innovatie.app.data.local.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    productDao: ProductDao,
    newsRepository: InnovationNewsRepository,
) : ViewModel() {
    val topProducts: StateFlow<List<Product>> = productDao.observeTop3()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _innovationNews = MutableStateFlow(newsRepository.fallbackFeed())
    val innovationNews: StateFlow<List<InnovationNewsItem>> = _innovationNews.asStateFlow()

    init {
        viewModelScope.launch {
            _innovationNews.value = newsRepository.loadFeed()
        }
    }

    companion object {
        fun factory(dao: ProductDao, newsRepository: InnovationNewsRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(dao, newsRepository) as T
            }
    }
}
