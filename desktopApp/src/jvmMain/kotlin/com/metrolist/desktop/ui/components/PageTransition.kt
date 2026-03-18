package com.metrolist.desktop.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable

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