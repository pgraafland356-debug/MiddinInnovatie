package com.middin.innovatie.app.ui.more

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.about.AboutScreen
import com.middin.innovatie.app.ui.bluetooth.BluetoothScreen
import com.middin.innovatie.app.ui.changelog.ChangelogScreen
import com.middin.innovatie.app.ui.components.BackTopBar
import com.middin.innovatie.app.ui.credits.CreditsScreen
import com.middin.innovatie.app.ui.gemini.GeminiScreen
import com.middin.innovatie.app.ui.info.InfoScreen
import com.middin.innovatie.app.ui.settings.SettingsScreen
import com.middin.innovatie.app.ui.updates.UpdatesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MoreRoutes.MENU,
        modifier = Modifier,
    ) {
        composable(MoreRoutes.MENU) {
            MoreMenuScreen(onOpen = { navController.navigate(it) })
        }
        composable(MoreRoutes.SETTINGS) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.more_settings) { navController.popBackStack() }
                },
            ) { padding ->
                SettingsScreen(modifier = Modifier.padding(padding))
            }
        }
        composable(MoreRoutes.CHANGELOG) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.nav_changelog) { navController.popBackStack() }
                },
            ) { padding ->
                ChangelogScreen(modifier = Modifier.padding(padding))
            }
        }
        composable(MoreRoutes.UPDATES) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.nav_updates) { navController.popBackStack() }
                },
            ) { padding ->
                UpdatesScreen(modifier = Modifier.padding(padding))
            }
        }
        composable(MoreRoutes.INFO) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.nav_info) { navController.popBackStack() }
                },
            ) { padding ->
                InfoScreen(modifier = Modifier.padding(padding))
            }
        }
        composable(MoreRoutes.ABOUT) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.nav_about) { navController.popBackStack() }
                },
            ) { padding ->
                AboutScreen(modifier = Modifier.padding(padding))
            }
        }
        composable(MoreRoutes.CREDITS) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.nav_credits) { navController.popBackStack() }
                },
            ) { padding ->
                CreditsScreen(modifier = Modifier.padding(padding))
            }
        }
        composable(MoreRoutes.GEMINI) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.nav_gemini) { navController.popBackStack() }
                },
            ) { padding ->
                GeminiScreen(modifier = Modifier.padding(padding))
            }
        }
        composable(MoreRoutes.BLUETOOTH) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.nav_bluetooth) { navController.popBackStack() }
                },
            ) { padding ->
                BluetoothScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}
