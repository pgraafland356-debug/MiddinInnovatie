package com.middin.innovatie.app.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.middin.innovatie.app.ui.theme.MiddinDimens

@Composable
fun ProductsScreen(
    onAddClick: () -> Unit,
    viewModel: ProductsViewModel = viewModel(
        factory = ProductsViewModel.factory(rememberAppContainer().database.productDao()),
    ),
) {
    val list by viewModel.products.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            text = { Text(stringResource(R.string.product_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteId = null
                        viewModel.removeProduct(id)
                    },
                ) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }

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
                .padding(horizontal = MiddinDimens.screenHorizontalPadding()),
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
                    ProductRowCard(
                        product = product,
                        onRemove = { pendingDeleteId = product.id },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductRowCard(
    product: Product,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.products_remove_cd),
                    )
                }
            }
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
            Text(product.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
