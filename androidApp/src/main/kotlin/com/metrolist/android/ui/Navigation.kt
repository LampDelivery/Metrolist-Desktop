package com.metrolist.android.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.metrolist.music.playback.PlayerConnection

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.AutoMirrored.Filled.List)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun rememberMetrolistNavController() = rememberNavController()

@UnstableApi
@Composable
fun MetrolistNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playerConnection: PlayerConnection? = null
) {
    NavHost(navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) { 
            HomeScreen(
                onNavigateToAlbum = { albumId ->
                    // TODO: Navigate to album
                },
                onNavigateToArtist = { artistId ->
                    // TODO: Navigate to artist
                },
                onNavigateToPlaylist = { playlistId ->
                    navController.navigate("playlist/$playlistId")
                }
            )
        }
        composable(Screen.Library.route) { LibraryScreen() }
        composable(Screen.Search.route) { SearchScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        
        // Playlist screen with argument
        composable(
            route = "playlist/{playlistId}",
            arguments = listOf(
                navArgument("playlistId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            PlaylistScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(Screen.Home, Screen.Library, Screen.Search, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Only show bottom bar on main screens
    val currentRoute = currentDestination?.route
    val showBottomBar = items.any { it.route == currentRoute }
    
    if (!showBottomBar) return

    NavigationBar(modifier = modifier) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.route == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
