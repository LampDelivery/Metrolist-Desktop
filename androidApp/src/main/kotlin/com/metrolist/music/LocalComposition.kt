package com.metrolist.music

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.playback.PlayerConnection

/**
 * CompositionLocal for PlayerConnection
 */
val LocalPlayerConnection: ProvidableCompositionLocal<PlayerConnection?> = compositionLocalOf { null }

/**
 * CompositionLocal for Database
 * TODO: Implement database in KMP base
 */
val LocalDatabase: ProvidableCompositionLocal<MusicDatabase?> = compositionLocalOf { null }

/**
 * CompositionLocal for DownloadUtil
 * TODO: Implement download util in KMP base
 */
val LocalDownloadUtil: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }

/**
 * CompositionLocal for ListenTogetherManager
 * TODO: Implement Listen Together in KMP base
 */
val LocalListenTogetherManager: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }

/**
 * CompositionLocal for PlayerAwareWindowInsets
 * TODO: Implement window insets handling
 */
val LocalPlayerAwareWindowInsets: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }

/**
 * CompositionLocal for BottomSheetPageState
 * TODO: Implement bottom sheet state
 */
val LocalBottomSheetPageState: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }

/**
 * CompositionLocal for MenuState
 * TODO: Implement menu state
 */
val LocalMenuState: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
