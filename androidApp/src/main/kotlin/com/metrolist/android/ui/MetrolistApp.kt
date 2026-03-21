package com.metrolist.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.metrolist.music.playback.LocalPlayerConnection
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.ui.components.ExpandedPlayer
import com.metrolist.music.ui.components.MiniPlayer
import com.metrolist.music.viewmodels.PlaybackViewModel
import com.metrolist.shared.ui.theme.MetrolistTheme

@OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)
@Composable
fun MetrolistApp() {
    MetrolistTheme(useDarkTheme = true) {
        val navController = rememberNavController()
        val playbackViewModel: PlaybackViewModel = hiltViewModel()
        val playerConnection = playbackViewModel.playerConnection
        
        // Collect state to trigger recomposition
        val currentSong by playerConnection.currentSong.collectAsState(initial = null)
        val hasMedia = currentSong != null
        var isPlayerExpanded by remember { mutableStateOf(false) }
        
        // Sync mini player visibility with playback state
        // When a song is playing, show the mini player
        LaunchedEffect(hasMedia) {
            // This ensures UI updates when playback starts/stops
        }
        
        CompositionLocalProvider(
            LocalPlayerConnection provides playerConnection
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main content scaffold
                Scaffold(
                    bottomBar = { 
                        Column {
                            // Mini player above navigation bar - always visible when has media
                            AnimatedVisibility(
                                visible = hasMedia && !isPlayerExpanded,
                                enter = slideInVertically { it },
                                exit = slideOutVertically { it }
                            ) {
                                MiniPlayer(
                                    playerConnection = playerConnection,
                                    onExpand = { isPlayerExpanded = true }
                                )
                            }
                            BottomBar(navController)
                        }
                    }
                ) { paddingValues ->
                    MetrolistNavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues),
                        playerConnection = playerConnection
                    )
                }
                
                // Expanded player overlay - full screen
                AnimatedVisibility(
                    visible = isPlayerExpanded,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ExpandedPlayer(
                            playerConnection = playerConnection,
                            onCollapse = { isPlayerExpanded = false }
                        )
                    }
                }
            }
        }
    }
}
