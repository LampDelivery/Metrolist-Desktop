package com.metrolist.shared.state

import com.metrolist.shared.api.YouTubeRepository
import com.metrolist.shared.api.innertube.InnerTube
import com.metrolist.shared.api.innertube.models.YouTubeLocale
import com.metrolist.shared.network.createHttpClient

object GlobalYouTube {
    val client = createHttpClient()
    val innerTube: InnerTube = InnerTube(client)
    val repository: YouTubeRepository = YouTubeRepository(innerTube)

    fun updateCookie(cookie: String?) {
        innerTube.cookie = cookie
    }

    fun updateLocale(locale: YouTubeLocale) {
        innerTube.locale = locale
    }
}

object GlobalInnerTube {
    val client get() = GlobalYouTube.client
    val instance get() = GlobalYouTube.innerTube
}

object GlobalYouTubeRepository {
    val instance get() = GlobalYouTube.repository
}
