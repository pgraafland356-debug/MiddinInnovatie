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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.middin.innovatie.app.R
import com.middin.innovatie.app.UserPreferencesRepository
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.ui.theme.MiddinDimens

data class MoreMenuItem(
    val titleRes: Int,
    val route: String,
)

@Composable
fun MoreMenuScreen(onOpen: (String) -> Unit) {
    val container = rememberAppContainer()
    val username by container.userPreferences.username.collectAsStateWithLifecycle(initialValue = null)
    val showGemini = UserPreferencesRepository.canConfigureEndpoints(username)
    val menuRows = listOfNotNull(
        MoreMenuItem(R.string.nav_settings, MoreRoutes.SETTINGS),
        MoreMenuItem(R.string.nav_changelog, MoreRoutes.CHANGELOG),
        MoreMenuItem(R.string.nav_updates, MoreRoutes.UPDATES),
        MoreMenuItem(R.string.nav_info, MoreRoutes.INFO),
        MoreMenuItem(R.string.nav_about, MoreRoutes.ABOUT),
        MoreMenuItem(R.string.nav_credits, MoreRoutes.CREDITS),
        if (showGemini) MoreMenuItem(R.string.nav_gemini, MoreRoutes.GEMINI) else null,
        MoreMenuItem(R.string.nav_bluetooth, MoreRoutes.BLUETOOTH),
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = MiddinDimens.screenHorizontalPadding(),
                vertical = MiddinDimens.screenVerticalPadding(),
            ),
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
