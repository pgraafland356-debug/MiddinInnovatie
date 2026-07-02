package com.middin.innovatie.app.ui.products

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.middin.innovatie.app.R
import com.middin.innovatie.app.ui.components.BackTopBar

private const val ROUTE_LIST = "products_list"
private const val ROUTE_ADD = "products_add"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ROUTE_LIST,
        modifier = Modifier,
    ) {
        composable(ROUTE_LIST) {
            ProductsScreen(onAddClick = { navController.navigate(ROUTE_ADD) })
        }
        composable(ROUTE_ADD) {
            Scaffold(
                topBar = {
                    BackTopBar(R.string.product_add_title) { navController.popBackStack() }
                },
            ) { padding ->
                AddProductScreen(
                    onClose = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        }
    }
}
