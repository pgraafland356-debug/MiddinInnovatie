package com.middin.innovatie.app.ui.info

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.theme.MiddinDimens

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.info_body),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = MiddinDimens.screenHorizontalPadding(),
                vertical = MiddinDimens.screenVerticalPadding(),
            ),
        style = MaterialTheme.typography.bodyLarge,
    )
}
