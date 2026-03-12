package com.metrolist.desktop.constants

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val ListItemHeight = 64.dp
val ListThumbnailSize = 48.dp
val ThumbnailCornerRadius = 3.dp
val ExpandedThumbnailCornerRadius = 8.dp
val DefaultThemeColor = Color(0xFFED5564)

// Desktop specific dimensions
val SideRailWidth = 240.dp
val SideRailCollapsedWidth = 80.dp
val TopBarHeight = 64.dp
val BottomPlayerHeight = 72.dp

enum class SliderStyle {
    DEFAULT,
    WAVY,
    SLIM,
    SQUIGGLY 
}

enum class RepeatMode { OFF, ALL, ONE }

object Config {
    const val LASTFM_API_KEY = "04cc18fe458e201fb0b56e07be18420b"
    const val LASTFM_SECRET = "a091ad2db5df15d4f3901b6a94eb812a"
}
