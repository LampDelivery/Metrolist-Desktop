/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.utils

import androidx.navigation.NavController
// TODO: Migrate import - Screens may not exist in KMP
// import com.metrolist.music.ui.screens.Screens

// TODO: Uncomment when Screens is migrated
/*
fun NavController.backToMain() {
    val mainRoutes = Screens.MainScreens.map { it.route }

    while (previousBackStackEntry != null &&
        currentBackStackEntry?.destination?.route !in mainRoutes
    ) {
        popBackStack()
    }
}
*/

// Temporary placeholder implementation
fun NavController.backToMain() {
    // Placeholder - implement when Screens is available
    while (previousBackStackEntry != null) {
        popBackStack()
    }
}
