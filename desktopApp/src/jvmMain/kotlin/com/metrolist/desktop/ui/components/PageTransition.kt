package com.metrolist.desktop.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public fun PageTransition(pageKey: String, content: @Composable (String) -> Unit) {
    AnimatedContent(
        targetState = pageKey,
        transitionSpec = {
            (fadeIn(tween(200, easing = FastOutSlowInEasing)) +
             slideInVertically(tween(200, easing = FastOutSlowInEasing)) { (it * 0.03f).toInt() }) togetherWith
            fadeOut(tween(150, easing = FastOutSlowInEasing))
        },
        label = "pageTransition"
    ) { key ->
        content(key)
    }
}