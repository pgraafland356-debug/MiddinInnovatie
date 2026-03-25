package com.middin.innovatie.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.chat.ChatScreen
import com.middin.innovatie.app.ui.home.HomeScreen
import com.middin.innovatie.app.ui.login.LoginScreen
import com.middin.innovatie.app.ui.memory.MemoryScreen
import com.middin.innovatie.app.ui.more.MoreNavHost
import com.middin.innovatie.app.ui.products.ProductsNavHost
import kotlinx.coroutines.launch

private enum class RootTab(
    val labelRes: Int,
) {
    Home(R.string.nav_home),
    Memory(R.string.nav_memory),
    Chat(R.string.nav_chat),
    Products(R.string.nav_products),
    More(R.string.nav_more),
}

@Composable
fun MiddinApp() {
    val container = rememberAppContainer()
    val loggedIn by produceState<Boolean?>(initialValue = null, container) {
        container.userPreferences.session.collect { value = it }
    }

    when (loggedIn) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        false -> LoginScreen()
        true -> MainShell()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell() {
    var selected by rememberSaveable { mutableStateOf(RootTab.Home) }
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ColumnTitle(
                        title = stringResource(R.string.brand_title),
                        subtitle = stringResource(R.string.brand_subtitle),
                    )
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                container.userPreferences.setSession(loggedIn = false)
                            }
                        },
                    ) {
                        Text(stringResource(R.string.logout))
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                RootTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    RootTab.Home -> Icons.Filled.Home
                                    RootTab.Memory -> Icons.Outlined.Storage
                                    RootTab.Chat -> Icons.AutoMirrored.Outlined.Chat
                                    RootTab.Products -> Icons.Filled.Inventory2
                                    RootTab.More -> Icons.Filled.Menu
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selected) {
                RootTab.Home -> HomeScreen()
                RootTab.Memory -> MemoryScreen()
                RootTab.Chat -> ChatScreen()
                RootTab.Products -> ProductsNavHost()
                RootTab.More -> MoreNavHost()
            }
        }
    }
}

@Composable
private fun ColumnTitle(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
