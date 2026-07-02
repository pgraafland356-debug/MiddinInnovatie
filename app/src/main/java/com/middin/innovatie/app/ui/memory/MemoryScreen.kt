package com.middin.innovatie.app.ui.memory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.middin.innovatie.app.R
import com.middin.innovatie.app.data.local.MemoryEntry
import com.middin.innovatie.app.ui.rememberAppContainer
import com.middin.innovatie.app.ui.theme.MiddinDimens
import java.text.DateFormat
import java.util.Date

@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = viewModel(
        factory = MemoryViewModel.factory(rememberAppContainer().database.memoryDao()),
    ),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val username by rememberAppContainer().userPreferences.username.collectAsStateWithLifecycle(
        initialValue = null,
    )
    var draft by rememberSaveable { mutableStateOf("") }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editDraft by remember { mutableStateOf("") }
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

    editingEntryId?.let { id ->
        AlertDialog(
            onDismissRequest = { editingEntryId = null },
            title = { Text(stringResource(R.string.memory_edit_title)) },
            text = {
                OutlinedTextField(
                    value = editDraft,
                    onValueChange = { editDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.memory_hint)) },
                    minLines = 3,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.update(id, editDraft)
                        editingEntryId = null
                    },
                    enabled = editDraft.isNotBlank(),
                ) {
                    Text(stringResource(R.string.memory_update_note))
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntryId = null }) {
                    Text(stringResource(R.string.product_cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = MiddinDimens.screenHorizontalPadding(),
                vertical = MiddinDimens.screenVerticalPadding(),
            ),
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.memory_hint)) },
            minLines = 2,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.add(draft, username.orEmpty())
                draft = ""
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.memory_add))
        }
        Spacer(Modifier.height(16.dp))
        if (items.isEmpty()) {
            Text(stringResource(R.string.memory_empty), style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(items, key = { it.id }) { entry ->
                    MemoryEntryCard(
                        entry = entry,
                        dateFormat = dateFormat,
                        onEdit = {
                            editingEntryId = entry.id
                            editDraft = entry.content
                        },
                        onRemove = { viewModel.remove(entry.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryEntryCard(
    entry: MemoryEntry,
    dateFormat: DateFormat,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    entry.content,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            onClickLabel = stringResource(R.string.memory_edit_cd),
                            onClick = onEdit,
                        ),
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.memory_edit_cd),
                        )
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.memory_remove_cd),
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${entry.authorName} · ${dateFormat.format(Date(entry.createdAtEpochMs))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
