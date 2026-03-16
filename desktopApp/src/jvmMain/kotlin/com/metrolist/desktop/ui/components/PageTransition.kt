package com.metrolist.desktop.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Page class no longer needed for transition key

@Composable
public fun PageTransition(pageKey: String, content: @Composable (String) -> Unit) {
    AnimatedContent(
        targetState = pageKey,
        transitionSpec = {
            // Use fadeIn and fadeOut to prevent overlap
            fadeIn(
                animationSpec = tween(250, delayMillis = 125, easing = EaseOut)
            ) togetherWith fadeOut(
                animationSpec = tween(125, easing = EaseIn)
            )
        },
        label = "pageTransition"
    ) { key ->
        content(key)
    }
}