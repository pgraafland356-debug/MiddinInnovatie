package com.middin.innovatie.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.middin.innovatie.app.ui.components.MiddinLogoMark
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.chat.ChatScreen
import com.middin.innovatie.app.ui.home.HomeHubScreen
import com.middin.innovatie.app.ui.home.HomeNewsAndProductsScreen
import com.middin.innovatie.app.ui.login.LoginScreen
import com.middin.innovatie.app.ui.update.UpdateAvailablePrompt
import com.middin.innovatie.app.ui.welcome.WelcomeScreen
import com.middin.innovatie.app.ui.memory.MemoryScreen
import com.middin.innovatie.app.ui.more.MoreNavHost
import com.middin.innovatie.app.ui.products.ProductsNavHost
import com.middin.innovatie.app.ui.theme.MiddinDimens
import kotlinx.coroutines.launch

private enum class HomeSubRoute {
    Hub,
    Overview,
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
        false -> {
            var showLogin by remember { mutableStateOf(false) }
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            // Welcome first on every screen-on / resume while signed out; also when session becomes false (logout).
            LaunchedEffect(loggedIn) {
                if (loggedIn != false) return@LaunchedEffect
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    showLogin = false
                }
            }
            if (showLogin) {
                LoginScreen()
            } else {
                WelcomeScreen(onContinue = { showLogin = true })
            }
        }
        true -> MainShell()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell() {
    var selected by rememberSaveable { mutableStateOf(RootTab.Home) }
    var homeSubRoute by rememberSaveable { mutableStateOf(HomeSubRoute.Hub) }
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    UpdateAvailablePrompt()

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            text = { Text(stringResource(R.string.logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        scope.launch {
                            container.userPreferences.setSession(loggedIn = false)
                        }
                    },
                ) {
                    Text(stringResource(R.string.dialog_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(stringResource(R.string.dialog_no))
                }
            },
        )
    }

    val showHomeUp = selected != RootTab.Home || homeSubRoute != HomeSubRoute.Hub
    LaunchedEffect(selected) {
        if (selected != RootTab.Home) {
            homeSubRoute = HomeSubRoute.Hub
        }
    }

    val showNavLabels = MiddinDimens.navigationBarAlwaysShowLabels()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val topBarW = MiddinDimens.windowContainerWidthDp()
                        MiddinLogoMark(size = MiddinDimens.topBarLogoSize())
                        Spacer(Modifier.width(if (topBarW < 360) 8.dp else 12.dp))
                        ColumnTitle(
                            modifier = Modifier.weight(1f),
                            title = stringResource(R.string.brand_title),
                            subtitle = stringResource(R.string.brand_subtitle),
                        )
                    }
                },
                navigationIcon = {
                    if (showHomeUp) {
                        IconButton(
                            onClick = {
                                when {
                                    selected == RootTab.Home && homeSubRoute == HomeSubRoute.Overview ->
                                        homeSubRoute = HomeSubRoute.Hub

                                    else -> {
                                        selected = RootTab.Home
                                        homeSubRoute = HomeSubRoute.Hub
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.home_nav_back_to_menu),
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showLogoutConfirm = true },
                    ) {
                        Text(
                            stringResource(R.string.logout),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
                        alwaysShowLabel = showNavLabels,
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
                RootTab.Home ->
                    when (homeSubRoute) {
                        HomeSubRoute.Hub ->
                            HomeHubScreen(
                                onOpenNewsAndProducts = { homeSubRoute = HomeSubRoute.Overview },
                                onNavigateToTab = { selected = it },
                            )

                        HomeSubRoute.Overview -> HomeNewsAndProductsScreen()
                    }

                RootTab.Memory -> MemoryScreen()
                RootTab.Chat -> ChatScreen()
                RootTab.Products -> ProductsNavHost()
                RootTab.More -> MoreNavHost()
            }
        }
    }
}

@Composable
private fun ColumnTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val narrow = MiddinDimens.windowContainerWidthDp() < 380
    Column(modifier = modifier) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (narrow) 1 else 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
