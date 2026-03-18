package com.metrolist.shared.api.innertube.pages

import com.metrolist.shared.api.innertube.models.BrowseEndpoint
import com.metrolist.shared.model.YTItem

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
