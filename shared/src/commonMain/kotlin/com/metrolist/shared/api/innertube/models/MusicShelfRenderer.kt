package com.metrolist.shared.api.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val title: Runs? = null,
    val contents: List<Content>? = null,
    // val continuations: List<Continuation>? = null,
) {
    @Serializable
    data class Content(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
        // val continuationItemRenderer: ContinuationItemRenderer? = null,
    )
}
