package com.middin.innovatie.app.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.components.MiddinLogoMark
import com.middin.innovatie.app.ui.theme.MiddinDimens
import java.time.Year

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = MiddinDimens.screenHorizontalPadding(),
                vertical = MiddinDimens.screenVerticalPadding(),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MiddinLogoMark(size = 96.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.about_body),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.copyright_line, Year.now().value),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
