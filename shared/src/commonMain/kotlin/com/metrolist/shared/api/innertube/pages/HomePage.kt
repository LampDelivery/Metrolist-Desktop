package com.metrolist.shared.api.innertube.pages

import com.metrolist.shared.api.innertube.models.*
import com.metrolist.shared.api.innertube.models.response.BrowseResponse
import com.metrolist.shared.model.YTItem
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.model.AlbumItem
import com.metrolist.shared.model.ArtistItem
import com.metrolist.shared.model.PlaylistItem
import com.metrolist.shared.model.PodcastItem
import com.metrolist.shared.model.EpisodeItem
import com.metrolist.shared.model.ArtistTiny
import com.metrolist.shared.model.AlbumTiny

data class HomePage(
    val chips: List<Chip>?,
    val sections: List<Section>,
    val continuation: String? = null,
) {
    data class Chip(
        val title: String,
        val endpoint: BrowseEndpoint?,
    )

    data class Section(
        val title: String,
        val label: String?,
        val thumbnail: String?,
        val endpoint: BrowseEndpoint?,
        val items: List<YTItem>,
    )
}
