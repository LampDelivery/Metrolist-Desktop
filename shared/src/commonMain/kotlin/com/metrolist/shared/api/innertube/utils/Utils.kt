package com.metrolist.shared.api.innertube.utils

fun parseCookieString(cookie: String): Map<String, String> {
    return cookie.split(";").map { it.trim() }.filter { it.isNotEmpty() }.associate {
        val parts = it.split("=", limit = 2)
        val key = parts.getOrNull(0) ?: ""
        val value = parts.getOrNull(1) ?: ""
        key to value
    }
}

// Expect declaration for platform-specific SHA-1 implementation
expect fun sha1(input: String): String
