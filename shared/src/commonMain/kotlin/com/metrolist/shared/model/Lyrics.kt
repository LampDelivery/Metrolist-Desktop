package com.metrolist.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class WordTimestamp(
    val text: String,
    val startTime: Long,
    val endTime: Long
)

@Serializable
data class LyricsEntry(
    val time: Long,
    val text: String,
    val words: List<WordTimestamp>? = null
)

data class LyricsWithProvider(
    val lyrics: String,
    val provider: String
)
