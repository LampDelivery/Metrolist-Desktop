package com.metrolist.shared.api.innertube.models.body

import com.metrolist.shared.api.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class BrowseBody(
    val context: Context,
    val browseId: String? = null,
    val params: String? = null,
    val continuation: String? = null
)
