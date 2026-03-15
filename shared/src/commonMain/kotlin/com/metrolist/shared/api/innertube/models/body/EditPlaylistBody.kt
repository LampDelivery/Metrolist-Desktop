package com.metrolist.shared.api.innertube.models.body

import com.metrolist.shared.api.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class EditPlaylistBody(
    val context: Context,
    val playlistId: String,
    val actions: List<Action>
) {
    @Serializable
    data class Action(
        val addedVideoId: String? = null,
        val action: String
    )
}
