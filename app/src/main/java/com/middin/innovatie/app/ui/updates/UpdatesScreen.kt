package com.middin.innovatie.app.ui.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.middin.innovatie.app.R
import com.middin.innovatie.app.UpdatesRepository
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.ui.theme.MiddinDimens

@Composable
fun UpdatesScreen(
    modifier: Modifier = Modifier,
    repo: UpdatesRepository = rememberAppContainer().updatesRepository,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MiddinDimens.screenHorizontalPadding()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
    ) {
        item {
            Text(stringResource(R.string.updates_intro), style = MaterialTheme.typography.bodyMedium)
        }
        items(repo.items) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "${item.title} · ${item.dateIso}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    item.bulletsEn.forEach { line ->
                        Text(
                            "• $line",
                            modifier = Modifier.padding(top = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
