package com.metrolist.shared.api

import com.metrolist.shared.model.YTItem
import com.metrolist.shared.model.SongItem

fun List<YTItem>.filterExplicit(hideExplicit: Boolean): List<YTItem> =
    if (!hideExplicit) this else this.filter {
        when (it) {
            is SongItem -> !it.isExplicit
            is com.metrolist.shared.model.AlbumItem -> !it.isExplicit
            else -> true
        }
    }

fun List<YTItem>.filterVideoSongs(hideVideoSongs: Boolean): List<YTItem> =
    if (!hideVideoSongs) this else this.filter {
        // Placeholder: implement actual video song detection
        true
    }

fun List<YTItem>.filterYoutubeShorts(hideYoutubeShorts: Boolean): List<YTItem> =
    if (!hideYoutubeShorts) this else this.filter {
        // Placeholder: implement actual YouTube Shorts detection
        true
    }

