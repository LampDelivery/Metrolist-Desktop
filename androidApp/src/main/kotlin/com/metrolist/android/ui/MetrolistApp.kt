package com.metrolist.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.metrolist.shared.ui.theme.MetrolistTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetrolistApp() {
    MetrolistTheme(useDarkTheme = true) {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { BottomBar(navController) }
        ) { paddingValues ->
            MetrolistNavHost(navController, Modifier.padding(paddingValues))
        }
    }
}
