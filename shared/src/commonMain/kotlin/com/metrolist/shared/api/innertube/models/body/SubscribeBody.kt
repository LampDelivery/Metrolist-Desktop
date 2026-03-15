package com.metrolist.shared.api.innertube.models.body

import com.metrolist.shared.api.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeBody(
    val context: Context,
    val channelIds: List<String>,
)
