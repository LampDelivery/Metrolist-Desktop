/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.shared.constants

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

val EnableDynamicIconKey = booleanPreferencesKey("enableDynamicIcon")
val EnableHighRefreshRateKey = booleanPreferencesKey("enableHighRefreshRate")
val DynamicThemeKey = booleanPreferencesKey("dynamicTheme")

// YouTube / network settings
val ContentCountryKey = stringPreferencesKey("contentCountry")
val ContentLanguageKey = stringPreferencesKey("contentLanguage")
val InnerTubeCookieKey = stringPreferencesKey("innerTubeCookie")

// Cache settings
val MaxSongCacheSizeKey = intPreferencesKey("maxSongCacheSize")

enum class LibrarySortOrder {
    NAME_ASC,
    NAME_DESC,
    ARTIST_ASC,
    ARTIST_DESC,
    DATE_ADDED_ASC,
    DATE_ADDED_DESC,
}

enum class LibrarySortBy {
    NAME,
    ARTIST,
    DATE_ADDED,
}

enum class TimePeriod {
    ALL_TIME,
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ;

    fun toTimeMillis(): Long =
        when (this) {
            DAY -> {
                Clock.System
                    .now()
                    .minus(kotlin.time.Duration.parse("24h"))
                    .toLocalDateTime(TimeZone.UTC)
                    .toInstant(TimeZone.UTC)
                    .toEpochMilliseconds()
            }

            WEEK -> {
                Clock.System
                    .now()
                    .minus(kotlin.time.Duration.parse("168h"))
                    .toLocalDateTime(TimeZone.UTC)
                    .toInstant(TimeZone.UTC)
                    .toEpochMilliseconds()
            }

            MONTH -> {
                Clock.System
                    .now()
                    .minus(kotlin.time.Duration.parse("720h")) // Rough estimate for a month
                    .toLocalDateTime(TimeZone.UTC)
                    .toInstant(TimeZone.UTC)
                    .toEpochMilliseconds()
            }

            YEAR -> {
                Clock.System
                    .now()
                    .minus(kotlin.time.Duration.parse("8760h")) // Rough estimate for a year
                    .toLocalDateTime(TimeZone.UTC)
                    .toInstant(TimeZone.UTC)
                    .toEpochMilliseconds()
            }

            ALL_TIME -> {
                0
            }
        }
}

enum class QuickPicks {
    QUICK_PICKS,
    LAST_LISTEN,
}
