package com.metrolist.desktop.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

// CompositionLocal for settings search query - defined here for settings screens to access
val LocalSettingsSearchQuery = compositionLocalOf { "" }

/**
 * Wraps content and only shows it if the search query matches any of the provided keywords.
 * If search query is empty, content is always shown.
 */
@Composable
fun SettingsSearchFilter(
    vararg keywords: String,
    content: @Composable () -> Unit
) {
    val searchQuery = LocalSettingsSearchQuery.current.lowercase()
    
    if (searchQuery.isEmpty()) {
        content()
        return
    }
    
    val matches = keywords.any { keyword ->
        keyword.lowercase().contains(searchQuery) || searchQuery.contains(keyword.lowercase())
    }
    
    AnimatedVisibility(
        visible = matches,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        content()
    }
}
