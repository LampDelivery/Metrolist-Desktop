package com.metrolist.desktop.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Page class no longer needed for transition key

@Composable
public fun PageTransition(pageKey: String, content: @Composable (String) -> Unit) {
    Crossfade(
        targetState = pageKey,
        animationSpec = tween(durationMillis = 400)
    ) { key ->
        content(key)
    }
}