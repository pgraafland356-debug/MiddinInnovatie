package com.middin.innovatie.app.ui.gemini

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.rememberAppContainer

@Composable
fun GeminiScreen(
    modifier: Modifier = Modifier,
    viewModel: GeminiViewModel = viewModel(
        factory = GeminiViewModel.factory(
            rememberAppContainer().geminiRepository,
            rememberAppContainer().userPreferences,
        ),
    ),
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    val ui by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.gemini_hint), style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            label = { Text(stringResource(R.string.gemini_prompt_label)) },
            minLines = 4,
            enabled = !ui.loading,
        )
        Button(
            onClick = { viewModel.runPrompt(prompt) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            enabled = !ui.loading && prompt.isNotBlank(),
        ) {
            Row {
                if (ui.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(22.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(stringResource(R.string.gemini_run))
            }
        }
        ui.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        ui.output?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
