package com.middin.innovatie.app.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.components.MiddinLogoMark
import com.middin.innovatie.app.ui.theme.MiddinBrandColors

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MiddinBrandColors.PrimaryLight,
            MiddinBrandColors.White,
            MiddinBrandColors.Secondary.copy(alpha = 0.45f),
        ),
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MiddinLogoMark(size = 112.dp)
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.brand_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MiddinBrandColors.Text,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.brand_subtitle),
                style = MaterialTheme.typography.titleMedium,
                color = MiddinBrandColors.TextLight,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.welcome_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MiddinBrandColors.Text.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiddinBrandColors.Primary,
                    contentColor = MiddinBrandColors.White,
                ),
            ) {
                Text(stringResource(R.string.welcome_cta))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MiddinBrandColors.Green.copy(alpha = 0.22f),
                            MiddinBrandColors.Green.copy(alpha = 0f),
                        ),
                    ),
                ),
        )
    }
}
