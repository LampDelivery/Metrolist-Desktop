/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
// TODO: Migrate import - Innertube/YouTube may not exist in KMP
// import com.metrolist.innertube.YouTube
// import com.metrolist.innertube.models.MediaInfo
// TODO: Migrate import - Local providers may not exist in KMP
// import com.metrolist.music.LocalDatabase
// import com.metrolist.music.LocalPlayerConnection
// TODO: Migrate import - R class resources may differ in KMP
// import com.metrolist.music.R
// TODO: Migrate import - Database entities may not exist in KMP
// import com.metrolist.music.db.entities.FormatEntity
// import com.metrolist.music.db.entities.Song
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.component.shimmer.ShimmerHost
import com.metrolist.music.ui.component.shimmer.TextPlaceholder

@Composable
fun ShowMediaInfo(videoId: String) {
    if (videoId.isBlank() || videoId.isEmpty()) return

    val windowInsets = WindowInsets.systemBars

    var info by remember {
        mutableStateOf<Any?>(null) // Changed from MediaInfo? to Any? since MediaInfo may not exist
    }

    // TODO: Uncomment when LocalDatabase is available
    // val database = LocalDatabase.current
    var song by remember { mutableStateOf<Any?>(null) } // Changed from Song? to Any?

    var currentFormat by remember { mutableStateOf<Any?>(null) } // Changed from FormatEntity? to Any?

    // TODO: Uncomment when LocalPlayerConnection is available
    // val playerConnection = LocalPlayerConnection.current
    val context = LocalContext.current

    LaunchedEffect(Unit, videoId) {
        // TODO: Uncomment when YouTube/MediaInfo is available
        // info = YouTube.getMediaInfo(videoId).getOrNull()
    }

    LaunchedEffect(Unit, videoId) {
        // TODO: Uncomment when database is available
        // database.song(videoId).collect {
        //     song = it
        // }
    }

    LaunchedEffect(Unit, videoId) {
        // TODO: Uncomment when database is available
        // database.format(videoId).collect {
        //     currentFormat = it
        // }
    }

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .padding(
                windowInsets
                    .asPaddingValues()
            )
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TODO: Re-implement when database and innertube are available
        // This is a placeholder implementation
        item(contentType = "MediaInfoPlaceholder") {
            Column {
                // TODO: Replace with actual implementation when dependencies are available
                Text(
                    text = "Media Info - Video ID: $videoId",
                    modifier = Modifier.padding(16.dp)
                )
                
                // TODO: Replace stringResource when R class is available
                val cardsList = mutableListOf<Material3SettingsItem>()
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                
                cardsList += Material3SettingsItem(
                    title = { Text("Video ID") },
                    description = { Text(videoId) },
                    onClick = {
                        cm.setPrimaryClip(ClipData.newPlainText("text", videoId))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    },
                )

                Material3SettingsGroup(
                    title = "Information",
                    items = cardsList
                )
            }
        }
        
        /*
        if (info != null && song != null) {
            item(contentType = "MediaDetails") {
                Column {
                    val baseList = listOf(
                        // TODO: Replace stringResource when R class is available
                        "Song Title" to (song as? Any)?.toString(),
                        "Artists" to "",
                        "Media ID" to videoId
                    )

                    val baseIconsList = listOf(
                        // TODO: Replace R.drawable when resources are available
                        android.R.drawable.ic_media_play,
                        android.R.drawable.ic_menu_myplaces,
                        android.R.drawable.ic_menu_info_details,
                    )

                    val iconsList = listOf(
                        android.R.drawable.ic_menu_info_details,
                        android.R.drawable.ic_menu_preferences,
                        android.R.drawable.ic_menu_close_clear_cancel,
                        android.R.drawable.ic_lock_idle_lock,
                        android.R.drawable.ic_menu_info_details,
                        android.R.drawable.ic_menu_search,
                        android.R.drawable.ic_menu_gallery,
                        android.R.drawable.ic_menu_crop,
                        android.R.drawable.ic_media_volume_on,
                        android.R.drawable.ic_media_volume_mute,
                        android.R.drawable.ic_menu_copy,
                    )

                    val extendedList = if (currentFormat != null) {
                        listOf(
                            "Views" to "",
                            "Likes" to "",
                            "Dislikes" to "",
                            "Itag" to "",
                            "Mime Type" to "",
                            "Codecs" to "",
                            "Bitrate" to "",
                            "Sample Rate" to "",
                            "Loudness" to "",
                            "Volume" to "",
                            "File Size" to "",
                        )
                    } else {
                        emptyList()
                    }

                    val cardsBaseList = mutableListOf<Material3SettingsItem>()
                    val cardsExtendedList = mutableListOf<Material3SettingsItem>()
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                    baseList.forEachIndexed { index, (label, text) ->
                        val displayText = text ?: "Unknown"
                        cardsBaseList += Material3SettingsItem(
                            title = { Text(label) },
                            description = { Text(displayText) },
                            icon = painterResource(baseIconsList[index]),
                            onClick = {
                                cm.setPrimaryClip(ClipData.newPlainText("text", displayText))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }

                    extendedList.forEachIndexed { index, (label, text) ->
                        val displayText = text ?: "Unknown"
                        cardsExtendedList += Material3SettingsItem(
                            title = { Text(label) },
                            description = { Text(displayText) },
                            icon = painterResource(iconsList[index]),
                            onClick = {
                                cm.setPrimaryClip(ClipData.newPlainText("text", displayText))
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }

                    Material3SettingsGroup(
                        title = "General",
                        items = cardsBaseList
                    )

                    Spacer(Modifier.height(8.dp))

                    Material3SettingsGroup(
                        title = "Information",
                        items = cardsExtendedList
                    )

                    Spacer(Modifier.height(8.dp))

                    val descriptionText = "Description placeholder"

                    Material3SettingsGroup(
                        title = "Description",
                        items = listOf(
                            Material3SettingsItem(
                                title = { Text("Description") },
                                description = { Text(descriptionText) },
                                onClick = {
                                    cm.setPrimaryClip(ClipData.newPlainText("text", descriptionText))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                }
                            )
                        )
                    )
                }
            }
        } else {
            item(contentType = "MediaInfoLoader") {
                ShimmerHost {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp)
                    ) {
                        TextPlaceholder()
                    }
                }
            }
        }
        */
    }
}
