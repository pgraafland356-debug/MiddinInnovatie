package com.middin.innovatie.app.ui.i18n

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.middin.innovatie.app.R
import com.middin.innovatie.app.locale.AppLocaleHelper
import com.middin.innovatie.app.ui.rememberAppContainer
import kotlinx.coroutines.launch

/** English / Dutch toggle; updates string resources across the app via [AppLocaleHelper]. */
@Composable
fun LanguageToggleBar(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()
    val localeTag by container.userPreferences.localeTag.collectAsStateWithLifecycle(initialValue = "en")
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = localeTag == "en",
            onClick = { scope.launch { AppLocaleHelper.apply(container.userPreferences, "en") } },
            label = { Text(stringResource(R.string.language_en)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        )
        FilterChip(
            selected = localeTag == "nl",
            onClick = { scope.launch { AppLocaleHelper.apply(container.userPreferences, "nl") } },
            label = { Text(stringResource(R.string.language_nl)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        )
    }
}
