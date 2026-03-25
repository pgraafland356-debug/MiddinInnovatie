package com.middin.innovatie.app.ui.more

import androidx.compose.foundation.clickable
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

data class MoreMenuItem(
    val titleRes: Int,
    val route: String,
)

@Composable
fun MoreMenuScreen(onOpen: (String) -> Unit) {
    val menuRows = listOf(
        MoreMenuItem(R.string.more_settings, MoreRoutes.SETTINGS),
        MoreMenuItem(R.string.nav_changelog, MoreRoutes.CHANGELOG),
        MoreMenuItem(R.string.nav_updates, MoreRoutes.UPDATES),
        MoreMenuItem(R.string.nav_info, MoreRoutes.INFO),
        MoreMenuItem(R.string.nav_about, MoreRoutes.ABOUT),
        MoreMenuItem(R.string.nav_credits, MoreRoutes.CREDITS),
        MoreMenuItem(R.string.nav_gemini, MoreRoutes.GEMINI),
        MoreMenuItem(R.string.nav_bluetooth, MoreRoutes.BLUETOOTH),
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        item {
            Text(stringResource(R.string.more_title), style = MaterialTheme.typography.titleLarge)
        }
        items(menuRows) { row ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clickable { onOpen(row.route) },
            ) {
                Text(
                    stringResource(row.titleRes),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

object MoreRoutes {
    const val MENU = "more_menu"
    const val SETTINGS = "more_settings"
    const val CHANGELOG = "more_changelog"
    const val UPDATES = "more_updates"
    const val INFO = "more_info"
    const val ABOUT = "more_about"
    const val CREDITS = "more_credits"
    const val GEMINI = "more_gemini"
    const val BLUETOOTH = "more_bluetooth"
}
