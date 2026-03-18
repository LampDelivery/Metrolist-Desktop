package com.metrolist.shared.api.innertube.pages

import com.metrolist.shared.api.innertube.models.MusicResponsiveListItemRenderer
import com.metrolist.shared.api.innertube.models.Run

object PageHelper {
    private val LIBRARY_ADD_ICONS = setOf("LIBRARY_ADD", "BOOKMARK_BORDER")
    private val LIBRARY_SAVED_ICONS = setOf("LIBRARY_SAVED", "BOOKMARK", "LIBRARY_REMOVE")
    private val ALL_LIBRARY_ICONS = LIBRARY_ADD_ICONS + LIBRARY_SAVED_ICONS

    data class LibraryFeedbackTokens(
        val addToken: String?,
        val removeToken: String?
    )

    fun isLibraryIcon(iconType: String?): Boolean {
        if (iconType == null) return false
        if (iconType == "KEEP" || iconType == "KEEP_OFF") return false
        return iconType in ALL_LIBRARY_ICONS || iconType.startsWith("LIBRARY_")
    }

    fun isAddLibraryIcon(iconType: String?): Boolean {
        return iconType in LIBRARY_ADD_ICONS
    }

    fun isSavedLibraryIcon(iconType: String?): Boolean {
        return iconType in LIBRARY_SAVED_ICONS
    }

    fun extractRuns(columns: List<MusicResponsiveListItemRenderer.FlexColumn>, typeLike: String): List<Run> {
        val filteredRuns = mutableListOf<Run>()
        for (column in columns) {
            val runs = column.musicResponsiveListItemFlexColumnRenderer.text?.runs
                ?: continue

            for (run in runs) {
                // For KMP migration, we access endpoints directly. 
                // Note: More complex matching logic from original app may need to be added later.
                val browseId = run.navigationEndpoint?.browseEndpoint?.browseId
                val videoId = run.navigationEndpoint?.watchEndpoint?.videoId

                if (browseId != null || videoId != null) {
                    filteredRuns.add(run)
                }
            }
        }
        return filteredRuns
    }
}
