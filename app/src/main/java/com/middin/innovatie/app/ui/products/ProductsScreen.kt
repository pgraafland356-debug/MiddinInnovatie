package com.middin.innovatie.app.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.middin.innovatie.app.R
import com.middin.innovatie.app.data.local.Product
import com.middin.innovatie.app.ui.rememberAppContainer

@Composable
fun ProductsScreen(
    onAddClick: () -> Unit,
    viewModel: ProductsViewModel = viewModel(
        factory = ProductsViewModel.factory(rememberAppContainer().database.productDao()),
    ),
) {
    val list by viewModel.products.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.products_add_cd))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        ) {
            item {
                Text(stringResource(R.string.products_title), style = MaterialTheme.typography.titleLarge)
            }
            if (list.isEmpty()) {
                item {
                    Text(stringResource(R.string.products_empty), modifier = Modifier.padding(top = 8.dp))
                }
            } else {
                items(list, key = { it.id }) { product ->
                    ProductRowCard(product)
                }
            }
        }
    }
}

@Composable
private fun ProductRowCard(product: Product) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            val uri = product.imageUri
            if (!uri.isNullOrBlank()) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(product.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Text(product.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
