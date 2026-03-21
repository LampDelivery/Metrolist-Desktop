/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Immutable
@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey val id: String = generatePlaylistId(),
    val name: String,
    val browseId: String? = null,
    val createdAt: LocalDateTime? = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val lastUpdateTime: LocalDateTime? = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    @ColumnInfo(name = "isEditable", defaultValue = true.toString())
    val isEditable: Boolean = true,
    val bookmarkedAt: LocalDateTime? = null,
    val remoteSongCount: Int? = null,
    val playEndpointParams: String? = null,
    val thumbnailUrl: String? = null,
    val shuffleEndpointParams: String? = null,
    val radioEndpointParams: String? = null,
    @ColumnInfo(name = "isLocal", defaultValue = false.toString())
    val isLocal: Boolean = false,
    @ColumnInfo(name = "isAutoSync", defaultValue = false.toString())
    val isAutoSync: Boolean = false
) {
    companion object {
        const val LIKED_PLAYLIST_ID = "LP_LIKED"
        const val DOWNLOADED_PLAYLIST_ID = "LP_DOWNLOADED"
        const val WEEKLY_MOST_PLAYLIST_ID = "LP_WEEKLY_MOST"
        const val MONTHLY_MOST_PLAYLIST_ID = "LP_MONTHLY_MOST"

        fun generatePlaylistId() = "LP" + (1..8).map { (('A'..'Z') + ('a'..'z')).random() }.joinToString("")
    }

    val shareLink: String?
        get() {
            return if (browseId != null)
                "https://music.youtube.com/playlist?list=$browseId"
            else null
        }

    fun localToggleLike() = copy(
        bookmarkedAt = if (bookmarkedAt != null) null else Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )

    fun toggleLike() = localToggleLike().also {
        CoroutineScope(Dispatchers.IO).launch {
            if (browseId != null)
            {} // TODO: Migrate import - YouTube.likePlaylist
        }
    }
}
