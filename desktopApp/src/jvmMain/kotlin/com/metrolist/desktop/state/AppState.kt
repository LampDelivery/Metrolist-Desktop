@file:Suppress("UNUSED_VARIABLE", "NAME_SHADOWING", "UNUSED_PARAMETER")
package com.metrolist.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.metrolist.shared.api.YouTubeRepository
import com.metrolist.shared.api.innertube.InnerTube
import com.metrolist.shared.model.*
import com.metrolist.shared.playback.MusicPlayer
import com.metrolist.desktop.constants.DefaultThemeColor
import com.metrolist.desktop.constants.RepeatMode
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.desktop.utils.DiscordRPCManager
import com.metrolist.desktop.utils.ScrobbleManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.util.prefs.Preferences

data class NavItem(val id: String, val label: String, val visible: Boolean = true)


object GlobalInnerTube {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    val instance: InnerTube = InnerTube(client)
}

object GlobalYouTubeRepository {
    val instance: YouTubeRepository = YouTubeRepository(GlobalInnerTube.instance)
}

object AppState {
    val client get() = GlobalInnerTube.client
    val prefs: Preferences = Preferences.userNodeForPackage(AppState::class.java)
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main + kotlinx.coroutines.Job())

    val player = MusicPlayer()
    private val scrobbleManager = ScrobbleManager(scope)
    
    var currentTrack: SongItem? by mutableStateOf(null)
    var isPlaying by mutableStateOf(false)
    var progress by mutableStateOf(0f)
    var volume by mutableStateOf(prefs.getFloat("PLAYER_VOLUME", 0.7f))
    
    // Remember expanded state
    var isExpanded by mutableStateOf(prefs.getBoolean("PLAYER_EXPANDED", false))
    
    var seedColor by mutableStateOf(DefaultThemeColor)
    var isWindowFocused by mutableStateOf(true)
    var isMaximized by mutableStateOf(false)
    var showSettings by mutableStateOf(false)
    var showSignIn by mutableStateOf(false)
    var showIntegrations by mutableStateOf(false)
    
    var selectedArtistId by mutableStateOf<String?>(null)
    var artistData by mutableStateOf<Map<String, List<YTItem>>>(emptyMap())
    var isArtistLoading by mutableStateOf(false)

    var selectedPlaylistId by mutableStateOf<String?>(null)
    var playlistData by mutableStateOf<Map<String, List<YTItem>>>(emptyMap())
    var isPlaylistLoading by mutableStateOf(false)

    var selectedAlbumId by mutableStateOf<String?>(null)
    var albumPageData by mutableStateOf<AlbumPage?>(null)
    var isAlbumLoading by mutableStateOf(false)

    var isHomeLoading by mutableStateOf(false)
    
    var isSignedIn by mutableStateOf(false)
    var profilePicUrl by mutableStateOf<String?>(null)
    var homeSections by mutableStateOf<Map<String, List<YTItem>>>(emptyMap())
    var librarySections by mutableStateOf<Map<String, List<YTItem>>>(emptyMap())
    
    var isFloatingPlayer by mutableStateOf(prefs.getBoolean("FLOATING_PLAYER", false))
    var sliderStyleState by mutableStateOf(SliderStyle.valueOf(prefs.get("SLIDER_STYLE", SliderStyle.DEFAULT.name)))
    var shuffleEnabled by mutableStateOf(false)
    var repeatMode by mutableStateOf(RepeatMode.OFF)
    
    var swapPlayerControls by mutableStateOf(prefs.getBoolean("SWAP_PLAYER_CONTROLS", false))
    var animatedGradient by mutableStateOf(prefs.getBoolean("ANIMATED_GRADIENT", false))
    
    var currentLyrics by mutableStateOf<String?>(null)
    var isLyricsLoading by mutableStateOf(false)

    // Discord RPC Preferences
    var discordRpcEnabled by mutableStateOf(prefs.getBoolean("DISCORD_RPC_ENABLED", true))
    var discordRpcShowIdle by mutableStateOf(prefs.getBoolean("DISCORD_RPC_SHOW_IDLE", true))
    var discordRpcUseDetails by mutableStateOf(prefs.getBoolean("DISCORD_RPC_USE_DETAILS", true))
    var discordRpcShowButtons by mutableStateOf(prefs.getBoolean("DISCORD_RPC_SHOW_BUTTONS", true))
    var discordRpcButton1Text by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON1_TEXT", "Listen on YouTube Music"))
    var discordRpcButton2Text by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON2_TEXT", "Visit Metrolist"))

    // Sidebar Navigation items
    var sidebarNavItems by mutableStateOf(loadSidebarNavItems())
    var isEditingSidebar by mutableStateOf(false)

    // Search History
    var searchHistory by mutableStateOf(loadSearchHistory())

    init {
        // Apply initial volume to player
        player.setVolume((volume * 100).toInt())

        // Sync UI state with shared player state
        scope.launch {
            player.isPlaying.collectLatest { 
                isPlaying = it 
                updateDiscordRpc()
            }
        }
        scope.launch {
            player.currentSong.collectLatest { song ->
                currentTrack = song
                if (song != null) {
                    fetchLyrics(song)
                } else {
                    currentLyrics = null
                }
                updateDiscordRpc()
            }
        }
        scope.launch {
            player.currentPosition.collectLatest {
                updateDiscordRpc()
            }
        }
        scope.launch {
            combine(player.isPlaying, player.currentSong) { playing, song ->
                playing to song
            }.collectLatest { (playing, song) ->
                scrobbleManager.onPlayerStateChanged(playing, song)
            }
        }
        scope.launch {
            combine(player.currentPosition, player.duration) { pos, dur ->
                if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
            }.collectLatest { 
                progress = it 
            }
        }
        
        // Periodic Discord RPC update for when nothing is playing (keep Paused state alive)
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000)
                if (!isPlaying || currentTrack == null) updateDiscordRpc()
            }
        }
    }

    private fun updateDiscordRpc() {
        if (discordRpcEnabled) {
            DiscordRPCManager.update(currentTrack, isPlaying, player.currentPosition.value)
        } else {
            DiscordRPCManager.clear()
        }
    }

    fun toggleDiscordRpc(enabled: Boolean) {
        discordRpcEnabled = enabled
        prefs.putBoolean("DISCORD_RPC_ENABLED", enabled)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun toggleDiscordRpcShowIdle(enabled: Boolean) {
        discordRpcShowIdle = enabled
        prefs.putBoolean("DISCORD_RPC_SHOW_IDLE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun toggleDiscordRpcUseDetails(enabled: Boolean) {
        discordRpcUseDetails = enabled
        prefs.putBoolean("DISCORD_RPC_USE_DETAILS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun toggleDiscordRpcShowButtons(enabled: Boolean) {
        discordRpcShowButtons = enabled
        prefs.putBoolean("DISCORD_RPC_SHOW_BUTTONS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun updateDiscordRpcButton1Text(text: String) {
        discordRpcButton1Text = text
        prefs.put("DISCORD_RPC_BUTTON1_TEXT", text)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun updateDiscordRpcButton2Text(text: String) {
        discordRpcButton2Text = text
        prefs.put("DISCORD_RPC_BUTTON2_TEXT", text)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    private fun loadSearchHistory(): List<String> {
        val saved = prefs.get("SEARCH_HISTORY", "")
        if (saved.isEmpty()) return emptyList()
        return saved.split("|")
    }

    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        val current = searchHistory.toMutableList()
        current.remove(query)
        current.add(0, query)
        val limited = current.take(10)
        searchHistory = limited
        prefs.put("SEARCH_HISTORY", limited.joinToString("|"))
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun removeSearchQuery(query: String) {
        val current = searchHistory.toMutableList()
        current.remove(query)
        searchHistory = current
        prefs.put("SEARCH_HISTORY", current.joinToString("|"))
        try { prefs.flush() } catch (_: Exception) {}
    }

    private fun loadSidebarNavItems(): List<NavItem> {
        val saved = prefs.get("SIDEBAR_NAV_ITEMS", null)
        val defaultItems = listOf(
            NavItem("home", "Home"),
            NavItem("library", "Library"),
            NavItem("history", "History"),
            NavItem("stats", "Stats"),
            NavItem("together", "Together")
        )
        if (saved == null) return defaultItems
        
        return try {
            saved.split(",").map { it ->
                val parts = it.split(":")
                val id = parts[0]
                val visible = parts[1].toBoolean()
                val label = defaultItems.find { it.id == id }?.label ?: id.replaceFirstChar { it.uppercase() }
                NavItem(id, label, visible)
            }
        } catch (_: Exception) {
            defaultItems
        }
    }

    fun saveSidebarNavItems(items: List<NavItem>) {
        sidebarNavItems = items
        val stringValue = items.joinToString(",") { "${it.id}:${it.visible}" }
        prefs.put("SIDEBAR_NAV_ITEMS", stringValue)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Theme Settings
    var pureBlack by mutableStateOf(prefs.getBoolean("PURE_BLACK", false))
    var pureBlackMiniPlayer by mutableStateOf(prefs.getBoolean("PURE_BLACK_MINI_PLAYER", false))

    fun playTrack(track: SongItem) {
        scope.launch {
            println("[DEBUG] playTrack called for: ${track.title} (${track.id})")
            val url = GlobalYouTubeRepository.instance.getStreamUrl(track.id)
            println("[DEBUG] getStreamUrl returned: $url")
            if (url != null) {
                println("[DEBUG] Calling player.play with url: $url")
                player.play(track, url)
            } else {
                println("[DEBUG] No stream URL found for track: ${track.title} (${track.id})")
            }
        }
    }

    fun togglePlay() {
        if (isPlaying) player.pause() else player.resume()
    }

    fun seekTo(fraction: Float) {
        val dur = player.duration.value
        if (dur > 0) {
            player.seekTo((fraction * dur).toLong())
        }
    }

    fun setVolumeLevel(level: Float) {
        volume = level
        player.setVolume((level * 100).toInt())
        prefs.putFloat("PLAYER_VOLUME", level)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateFloatingPlayerMode(enabled: Boolean) {
        isFloatingPlayer = enabled
        prefs.putBoolean("FLOATING_PLAYER", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }
    
    fun setSliderStyle(style: SliderStyle) {
        sliderStyleState = style
        prefs.put("SLIDER_STYLE", style.name)
        try { prefs.flush() } catch (_: Exception) {}
    }
    
    fun toggleSwapPlayerControls(enabled: Boolean) {
        swapPlayerControls = enabled
        prefs.putBoolean("SWAP_PLAYER_CONTROLS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }
    
    fun togglePureBlack(enabled: Boolean) {
        pureBlack = enabled
        prefs.putBoolean("PURE_BLACK", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }
    
    fun togglePureBlackMiniPlayer(enabled: Boolean) {
        pureBlackMiniPlayer = enabled
        prefs.putBoolean("PURE_BLACK_MINI_PLAYER", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleAnimatedGradient(enabled: Boolean) {
        animatedGradient = enabled
        prefs.putBoolean("ANIMATED_GRADIENT", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateAuth(cookie: String, visitorData: String, dataSyncId: String) {
        prefs.put("COOKIES", cookie)
        prefs.put("VISITOR_DATA", visitorData)
        prefs.put("DATASYNC_ID", dataSyncId)

        GlobalYouTubeRepository.instance.setCookie(cookie)
        GlobalInnerTube.instance.visitorData = visitorData
        GlobalInnerTube.instance.dataSyncId = dataSyncId

        isSignedIn = true
        showSignIn = false
        fetchAccountInfo()
        fetchHomeData()
        fetchLibraryData()
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun loadSession() {
        val cookies = prefs.get("COOKIES", null)
        val visitorData = prefs.get("VISITOR_DATA", null)
        val dataSyncId = prefs.get("DATASYNC_ID", null)

        GlobalInnerTube.instance.visitorData = visitorData
        GlobalInnerTube.instance.dataSyncId = dataSyncId

        if (cookies != null) {
            GlobalYouTubeRepository.instance.setCookie(cookies)
            isSignedIn = true
            profilePicUrl = prefs.get("PFP_URL", null)
            fetchAccountInfo()
            fetchLibraryData()
        }

        // Always try to fetch home data (will use visitorData if not signed in)
        fetchHomeData()
    }
    
    private fun fetchAccountInfo() {
        scope.launch {
            try {
                val info = GlobalYouTubeRepository.instance.getAccountInfo()
                if (info != null) {
                    profilePicUrl = info.thumbnailUrl
                    prefs.put("PFP_URL", info.thumbnailUrl ?: "")
                    prefs.flush()
                }
            } catch (_: Exception) {}
        }
    }

    fun fetchHomeData() {
        if (isHomeLoading) return
        isHomeLoading = true
        scope.launch {
            try {
                if (GlobalInnerTube.instance.visitorData == null) {
                    val data = GlobalInnerTube.instance.fetchFreshVisitorData()
                    if (data != null) {
                        prefs.put("VISITOR_DATA", data)
                        prefs.flush()
                    }
                }
                var sections = GlobalYouTubeRepository.instance.getHomeData()

                // If empty, try clearing visitor data and fetching again (likely expired visitor data)
                if (sections.isEmpty()) {
                    GlobalInnerTube.instance.visitorData = null
                    val data = GlobalInnerTube.instance.fetchFreshVisitorData()
                    if (data != null) {
                        prefs.put("VISITOR_DATA", data)
                        prefs.flush()
                    }
                    sections = GlobalYouTubeRepository.instance.getHomeData()
                }

                homeSections = sections
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isHomeLoading = false
            }
        }
    }

    fun fetchLibraryData() {
        scope.launch {
            try {
                val page = GlobalYouTubeRepository.instance.getLibrary()
                librarySections = mapOf("Library" to page.items)
            } catch (_: Exception) {}
        }
    }

    fun fetchArtistData(id: String) {
        isExpanded = false
        selectedArtistId = id
        selectedPlaylistId = null
        selectedAlbumId = null
        isArtistLoading = true
        scope.launch {
            try {
                artistData = GlobalYouTubeRepository.instance.getArtist(id)
            } catch (_: Exception) {}
            finally {
                isArtistLoading = false
            }
        }
    }

    fun fetchPlaylistData(id: String) {
        isExpanded = false
        selectedPlaylistId = id
        selectedArtistId = null
        selectedAlbumId = null
        isPlaylistLoading = true
        scope.launch {
            try {
                playlistData = GlobalYouTubeRepository.instance.getPlaylist(id)
            } catch (_: Exception) {}
            finally {
                isPlaylistLoading = false
            }
        }
    }

    fun fetchAlbumData(id: String) {
        isExpanded = false
        selectedAlbumId = id
        selectedArtistId = null
        selectedPlaylistId = null
        isAlbumLoading = true
        scope.launch {
            try {
                albumPageData = GlobalYouTubeRepository.instance.getAlbum(id)
            } catch (_: Exception) {}
            finally {
                isAlbumLoading = false
            }
        }
    }

    private fun fetchLyrics(song: SongItem) {
        currentLyrics = null
        isLyricsLoading = true
        scope.launch {
            currentLyrics = try {
                GlobalYouTubeRepository.instance.getLyrics(
                    song.title,
                    song.artists.joinToString { it.name },
                    song.duration,
                    song.album?.name ?: song.title
                )
            } catch (_: Exception) {
                "Lyrics not found"
            } finally {
                isLyricsLoading = false
            }
        }
    }

    fun signOut() {
        prefs.remove("COOKIES")
        prefs.remove("VISITOR_DATA")
        prefs.remove("DATASYNC_ID")
        prefs.remove("PFP_URL")
        isSignedIn = false
        GlobalYouTubeRepository.instance.setCookie(null)
        GlobalInnerTube.instance.visitorData = null
        GlobalInnerTube.instance.dataSyncId = null
        profilePicUrl = null
        homeSections = emptyMap()
        librarySections = emptyMap()
        try { prefs.flush() } catch (_: Exception) {}
        fetchHomeData() // Refresh home for guest
    }
}
