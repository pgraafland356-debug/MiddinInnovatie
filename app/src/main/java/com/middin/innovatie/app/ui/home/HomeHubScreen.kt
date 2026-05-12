package com.middin.innovatie.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.RootTab
import com.middin.innovatie.app.ui.theme.MiddinDimens

@Composable
fun HomeHubScreen(
    onOpenNewsAndProducts: () -> Unit,
    onNavigateToTab: (RootTab) -> Unit,
) {
    val hPad = MiddinDimens.screenHorizontalPadding()
    val vPad = MiddinDimens.screenVerticalPadding()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = hPad, vertical = vPad),
    ) {
        Text(stringResource(R.string.home_tagline), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.home_intro),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onOpenNewsAndProducts,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        ) {
            Text(stringResource(R.string.home_hub_overview_cta))
        }

        Text(
            stringResource(R.string.home_hub_quick_nav_title),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 28.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RootTab.entries
                .filter { it != RootTab.Home }
                .forEach { tab ->
                    OutlinedButton(
                        onClick = { onNavigateToTab(tab) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(tab.labelRes))
                    }
                }
        }
    }
}
