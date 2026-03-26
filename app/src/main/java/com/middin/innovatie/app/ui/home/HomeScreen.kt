package com.middin.innovatie.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.middin.innovatie.app.R
import com.middin.innovatie.app.data.InnovationNewsItem
import com.middin.innovatie.app.data.local.Product
import com.middin.innovatie.app.ui.rememberAppContainer

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            rememberAppContainer().database.productDao(),
            rememberAppContainer().innovationNewsRepository,
        ),
    ),
) {
    val top by viewModel.topProducts.collectAsStateWithLifecycle()
    val news by viewModel.innovationNews.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.home_tagline), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.home_intro),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            stringResource(R.string.home_news_title),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            stringResource(R.string.home_news_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        news.forEach { item ->
            InnovationNewsCard(
                item = item,
                onOpen = { uriHandler.openUri(item.articleUrl) },
            )
            Spacer(Modifier.height(10.dp))
        }

        Text(
            stringResource(R.string.home_top_products),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleSmall,
        )
        if (top.isEmpty()) {
            Text(stringResource(R.string.home_no_products_yet), style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(top, key = { it.id }) { product ->
                    ProductHighlightCard(product)
                }
            }
        }
    }
}

@Composable
private fun InnovationNewsCard(
    item: InnovationNewsItem,
    onOpen: () -> Unit,
) {
    val meta = stringResource(R.string.home_news_meta, item.sourceLabel, item.dateLabel)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { role = Role.Button }
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall)
            Text(
                meta,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                item.summary,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.home_news_open_hint),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ProductHighlightCard(product: Product) {
    Card(modifier = Modifier.width(280.dp)) {
        Column(Modifier.padding(12.dp)) {
            val uri = product.imageUri
            if (!uri.isNullOrBlank()) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(product.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
            Text(
                product.description.take(80) + if (product.description.length > 80) "…" else "",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
    }
}
