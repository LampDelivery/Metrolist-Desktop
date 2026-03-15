@file:Suppress("UNUSED_VARIABLE", "NAME_SHADOWING", "UNUSED_PARAMETER")
package com.metrolist.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.metrolist.shared.api.YouTubeRepository
import com.metrolist.shared.api.innertube.InnerTube
import com.metrolist.shared.model.*
import com.metrolist.shared.playback.MusicPlayer
import com.metrolist.shared.playback.MusicQueue
import com.metrolist.desktop.constants.DefaultThemeColor
import com.metrolist.desktop.constants.RepeatMode
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.desktop.utils.DiscordRPCManager
import com.metrolist.desktop.utils.HistoryEntry
import com.metrolist.desktop.utils.HistoryRepository
import com.metrolist.desktop.utils.HistoryStat
import com.metrolist.desktop.utils.ScrobbleManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val queue = MusicQueue()
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
    var pendingPlaylistThumbnail by mutableStateOf<String?>(null)

    var selectedAlbumId by mutableStateOf<String?>(null)
    var albumPageData by mutableStateOf<AlbumPage?>(null)
    var isAlbumLoading by mutableStateOf(false)

    var isHomeLoading by mutableStateOf(false)
    
    var isSignedIn by mutableStateOf(false)
    var profilePicUrl by mutableStateOf<String?>(null)
    
    // New Home Data Structure
    var homePageData by mutableStateOf(HomePageData())
    var selectedChip by mutableStateOf<Chip?>(null)
    private var previousHomePageData: HomePageData? = null

    var librarySections by mutableStateOf<Map<String, List<YTItem>>>(emptyMap())
    
    var isFloatingPlayer by mutableStateOf(prefs.getBoolean("FLOATING_PLAYER", false))
    var sliderStyleState by mutableStateOf(SliderStyle.valueOf(prefs.get("SLIDER_STYLE", SliderStyle.DEFAULT.name)))
    var discordRpcButton1Visible by mutableStateOf(prefs.getBoolean("DISCORD_RPC_BUTTON1_VISIBLE", true))
    var discordRpcButton2Visible by mutableStateOf(prefs.getBoolean("DISCORD_RPC_BUTTON2_VISIBLE", true))
    var shuffleEnabled by mutableStateOf(false)
    var repeatMode by mutableStateOf(RepeatMode.OFF)
    
    var swapPlayerControls by mutableStateOf(prefs.getBoolean("SWAP_PLAYER_CONTROLS", false))
    var animatedGradient by mutableStateOf(prefs.getBoolean("ANIMATED_GRADIENT", false))
    
    var currentLyrics by mutableStateOf<String?>(null)
    var isLyricsLoading by mutableStateOf(false)

    // Search Results
    var searchSummaryPage by mutableStateOf<SearchSummaryPage?>(null)
    var searchResultPage by mutableStateOf<SearchResultPage?>(null)
    var selectedSearchFilter by mutableStateOf<SearchFilter?>(null)
    var isSearchLoading by mutableStateOf(false)
    var currentSearchQuery by mutableStateOf("")

    // Discord RPC Preferences
    var discordRpcEnabled by mutableStateOf(prefs.getBoolean("DISCORD_RPC_ENABLED", true))
    var discordRpcShowIdle by mutableStateOf(prefs.getBoolean("DISCORD_RPC_SHOW_IDLE", true))
    var discordRpcUseDetails by mutableStateOf(prefs.getBoolean("DISCORD_RPC_USE_DETAILS", true))
    var discordRpcShowButtons by mutableStateOf(prefs.getBoolean("DISCORD_RPC_SHOW_BUTTONS", true))
    var discordRpcButton1Text by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON1_TEXT", "Listen on YouTube Music"))
    var discordRpcButton2Text by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON2_TEXT", "Visit Metrolist"))
    var discordRpcAppId by mutableStateOf(prefs.get("DISCORD_RPC_APP_ID", ""))
    var discordRpcActivityType by mutableStateOf(prefs.get("DISCORD_RPC_ACTIVITY_TYPE", "LISTENING"))
    var discordRpcButton1Url by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON1_URL", "https://music.youtube.com/watch?v={video_id}"))
    var discordRpcButton2Url by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON2_URL", "https://github.com/MetrolistGroup/Metrolist"))

    // Sidebar Navigation items
    var sidebarNavItems by mutableStateOf(loadSidebarNavItems())
    var isEditingSidebar by mutableStateOf(false)

    // Sidebar playlist order/visibility
    var playlistOrder by mutableStateOf(loadPlaylistOrder())
    var hiddenPlaylistIds by mutableStateOf(loadHiddenPlaylistIds())

    // Search History
    var searchHistory by mutableStateOf(loadSearchHistory())

    // Miniplayer state
    var showMiniplayer by mutableStateOf(prefs.getBoolean("SHOW_MINIPLAYER", false))
    var miniplayerNightMode by mutableStateOf(prefs.getBoolean("MINIPLAYER_NIGHT_MODE", false))
    var miniplayerFocusMode by mutableStateOf(prefs.getBoolean("MINIPLAYER_FOCUS_MODE", false))

    // Like / Queue / Playlist
    var likedSongIds by mutableStateOf(mutableSetOf<String>())
    var userPlaylists by mutableStateOf<List<PlaylistItem>>(emptyList())
    var showAddToPlaylistSong by mutableStateOf<SongItem?>(null)

    // Discord RPC - cached artist icon
    var cachedArtistIcon by mutableStateOf<String?>(null)
    val artistIconCache = mutableMapOf<String, String>()

    // Lyrics provider preference
    var lyricsProviderPref by mutableStateOf(
        try { LyricsProvider.valueOf(prefs.get("LYRICS_PROVIDER", LyricsProvider.AUTO.name)) }
        catch (_: Exception) { LyricsProvider.AUTO }
    )

    // Sleep timer
    var sleepTimerEnabled by mutableStateOf(false)
    var sleepTimerTimeLeftMs by mutableStateOf(0L)
    var sleepTimerStopAfterCurrentSong by mutableStateOf(false)
    private var sleepTimerJob: Job? = null
    private var preSleepVolume: Float = 1f

    // Related songs for expanded player
    var relatedSongs by mutableStateOf<List<SongItem>>(emptyList())
    var isRelatedLoading by mutableStateOf(false)
    private var lastRelatedFetchId: String? = null

    // Play history
    var historyEntries by mutableStateOf<List<HistoryEntry>>(emptyList())
    var topSongs by mutableStateOf<List<HistoryStat>>(emptyList())

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
                    // Reset cached icon then try to resolve it
                    cachedArtistIcon = null
                    val firstArtist = song.artists.firstOrNull()
                    if (firstArtist != null) {
                        val existingThumb = firstArtist.thumbnail
                        if (existingThumb != null) {
                            cachedArtistIcon = existingThumb
                        } else {
                            scope.launch {
                                val artistId = firstArtist.id
                                val cacheKey = artistId ?: firstArtist.name
                                val icon = artistIconCache[cacheKey]
                                    ?: if (!artistId.isNullOrEmpty()) {
                                        // Kizzy approach: browse artist page by channel ID for real YTM thumbnail
                                        GlobalYouTubeRepository.instance.fetchArtistThumbnail(artistId)
                                            ?.also { artistIconCache[cacheKey] = it }
                                    } else if (firstArtist.name.isNotBlank()) {
                                        GlobalYouTubeRepository.instance.fetchArtistIcon(firstArtist.name)
                                            ?.also { artistIconCache[cacheKey] = it }
                                    } else null
                                cachedArtistIcon = icon
                                updateDiscordRpc()
                            }
                        }
                    }
                    fetchLyrics(song)
                    // Record play in persistent history
                    withContext(Dispatchers.IO) { HistoryRepository.recordPlay(song) }
                    refreshHistory()
                } else {
                    currentLyrics = null
                    cachedArtistIcon = null
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

        // Auto-play next song
        scope.launch {
            player.onEOF.collect {
                skipNext()
            }
        }
        
        // Periodic Discord RPC update for when nothing is playing (keep Paused state alive)
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000)
                if (!isPlaying || currentTrack == null) updateDiscordRpc()
            }
        }

        // Load play history on startup
        refreshHistory()
    }

    private fun updateDiscordRpc() {
        if (discordRpcEnabled) {
            DiscordRPCManager.update(currentTrack, isPlaying, player.currentPosition.value, cachedArtistIcon)
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

    fun toggleDiscordRpcButton1Visible(enabled: Boolean) {
        discordRpcButton1Visible = enabled
        prefs.putBoolean("DISCORD_RPC_BUTTON1_VISIBLE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun toggleDiscordRpcButton2Visible(enabled: Boolean) {
        discordRpcButton2Visible = enabled
        prefs.putBoolean("DISCORD_RPC_BUTTON2_VISIBLE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun updateDiscordRpcAppId(id: String) {
        discordRpcAppId = id
        prefs.put("DISCORD_RPC_APP_ID", id)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun applyDiscordRpcAppId() {
        DiscordRPCManager.reinit()
        updateDiscordRpc()
    }

    fun updateDiscordRpcActivityType(type: String) {
        discordRpcActivityType = type
        prefs.put("DISCORD_RPC_ACTIVITY_TYPE", type)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun updateDiscordRpcButton1Url(url: String) {
        discordRpcButton1Url = url
        prefs.put("DISCORD_RPC_BUTTON1_URL", url)
        try { prefs.flush() } catch (_: Exception) {}
        updateDiscordRpc()
    }

    fun updateDiscordRpcButton2Url(url: String) {
        discordRpcButton2Url = url
        prefs.put("DISCORD_RPC_BUTTON2_URL", url)
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

    private fun loadPlaylistOrder(): List<String> {
        val saved = prefs.get("PLAYLIST_ORDER", null) ?: return emptyList()
        return saved.split(",").filter { it.isNotBlank() }
    }

    private fun loadHiddenPlaylistIds(): Set<String> {
        val saved = prefs.get("HIDDEN_PLAYLIST_IDS", null) ?: return emptySet()
        return saved.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun savePlaylistOrder(ids: List<String>) {
        playlistOrder = ids
        prefs.put("PLAYLIST_ORDER", ids.joinToString(","))
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun togglePlaylistVisibility(playlistId: String) {
        val newSet = hiddenPlaylistIds.toMutableSet()
        if (playlistId in newSet) newSet.remove(playlistId) else newSet.add(playlistId)
        hiddenPlaylistIds = newSet
        prefs.put("HIDDEN_PLAYLIST_IDS", newSet.joinToString(","))
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun getOrderedPlaylists(playlists: List<PlaylistItem>): List<PlaylistItem> {
        if (playlistOrder.isEmpty()) return playlists
        val orderMap = playlistOrder.withIndex().associate { (i, id) -> id to i }
        return playlists.sortedWith(compareBy { orderMap[it.id] ?: Int.MAX_VALUE })
    }

    // Theme Settings
    var pureBlack by mutableStateOf(prefs.getBoolean("PURE_BLACK", false))
    var pureBlackMiniPlayer by mutableStateOf(prefs.getBoolean("PURE_BLACK_MINI_PLAYER", false))

    fun playTrack(track: SongItem, list: List<SongItem>? = null) {
        scope.launch {
            if (list != null) {
                queue.setQueue(list, list.indexOf(track).coerceAtLeast(0))
            } else {
                queue.setQueue(listOf(track), 0)
            }
            
            val url = GlobalYouTubeRepository.instance.getStreamUrl(track.id)
            if (url != null) {
                player.play(track, url)
            }
        }
    }

    fun skipNext() {
        // Sleep timer: stop after current song ends
        if (sleepTimerStopAfterCurrentSong) {
            player.pause()
            clearSleepTimer()
            return
        }
        scope.launch {
            val next = queue.getNext()
            if (next != null) {
                val url = GlobalYouTubeRepository.instance.getStreamUrl(next.id)
                if (url != null) {
                    player.play(next, url)
                }
            } else if (repeatMode == RepeatMode.ALL && queue.items.value.isNotEmpty()) {
                queue.setQueue(queue.items.value, 0)
                val first = queue.currentItem()
                if (first != null) {
                    val url = GlobalYouTubeRepository.instance.getStreamUrl(first.id)
                    if (url != null) {
                        player.play(first, url)
                    }
                }
            }
        }
    }

    fun skipPrevious() {
        scope.launch {
            val currentPos = player.currentPosition.value
            if (currentPos > 3000) {
                player.seekTo(0)
            } else {
                val prev = queue.getPrevious()
                if (prev != null) {
                    val url = GlobalYouTubeRepository.instance.getStreamUrl(prev.id)
                    if (url != null) {
                        player.play(prev, url)
                    }
                } else {
                    player.seekTo(0)
                }
            }
        }
    }

    fun addToQueue(songs: List<SongItem>) {
        queue.addToQueue(songs)
    }

    fun addToQueue(song: SongItem) = addToQueue(listOf(song))

    fun startSleepTimer(minutes: Int, stopAfterCurrentSong: Boolean, fadeOut: Boolean) {
        sleepTimerJob?.cancel()
        sleepTimerStopAfterCurrentSong = stopAfterCurrentSong
        sleepTimerEnabled = true
        if (stopAfterCurrentSong) {
            sleepTimerTimeLeftMs = 0L
            return
        }
        preSleepVolume = volume
        val totalMs = minutes * 60 * 1000L
        sleepTimerTimeLeftMs = totalMs
        sleepTimerJob = scope.launch {
            while (sleepTimerTimeLeftMs > 0) {
                delay(500L)
                sleepTimerTimeLeftMs = (sleepTimerTimeLeftMs - 500L).coerceAtLeast(0L)
                // Fade out in last 60 seconds
                if (fadeOut && sleepTimerTimeLeftMs in 1L..60_000L) {
                    val fadeFraction = sleepTimerTimeLeftMs.toFloat() / 60_000f
                    player.setVolume((preSleepVolume * 100f * fadeFraction).toInt().coerceAtLeast(0))
                }
            }
            player.pause()
            if (fadeOut) player.setVolume((preSleepVolume * 100f).toInt())
            clearSleepTimer()
        }
    }

    fun clearSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepTimerEnabled = false
        sleepTimerTimeLeftMs = 0L
        sleepTimerStopAfterCurrentSong = false
        player.setVolume((volume * 100f).toInt())
    }

    private fun refreshHistory() {
        scope.launch(Dispatchers.IO) {
            val recent = HistoryRepository.getRecentHistory(50)
            val top    = HistoryRepository.getMostPlayed(20)
            withContext(Dispatchers.Main) {
                historyEntries = recent
                topSongs = top
            }
        }
    }

    fun fetchRelated(videoId: String) {
        if (isRelatedLoading || lastRelatedFetchId == videoId) return
        isRelatedLoading = true
        lastRelatedFetchId = videoId
        scope.launch {
            try {
                val songs = GlobalYouTubeRepository.instance.startRadio(videoId)
                relatedSongs = songs.drop(1).take(25) // skip current song itself
            } catch (_: Exception) {
                relatedSongs = emptyList()
            } finally {
                isRelatedLoading = false
            }
        }
    }

    fun playNext(songs: List<SongItem>) {
        queue.playNext(songs)
    }

    fun playNext(song: SongItem) = playNext(listOf(song))

    fun toggleLike(song: SongItem) {
        scope.launch {
            val isLiked = song.id in likedSongIds
            val token = if (isLiked) song.libraryRemoveToken else song.libraryAddToken
            if (token != null) {
                GlobalYouTubeRepository.instance.sendFeedback(listOf(token))
                val newSet = likedSongIds.toMutableSet()
                if (isLiked) newSet.remove(song.id) else newSet.add(song.id)
                likedSongIds = newSet
            }
        }
    }

    fun startRadio(song: SongItem) {
        scope.launch {
            val radioSongs = GlobalYouTubeRepository.instance.startRadio(song.id)
            if (radioSongs.isNotEmpty()) {
                queue.setQueue(radioSongs, 0)
                val url = GlobalYouTubeRepository.instance.getStreamUrl(radioSongs[0].id)
                if (url != null) player.play(radioSongs[0], url)
            }
        }
    }

    fun playFromId(videoId: String) {
        scope.launch {
            try {
                val songs = GlobalYouTubeRepository.instance.startRadio(videoId)
                if (songs.isNotEmpty()) {
                    queue.setQueue(songs, 0)
                    val url = GlobalYouTubeRepository.instance.getStreamUrl(songs[0].id)
                    if (url != null) player.play(songs[0], url)
                }
            } catch (_: Exception) {}
        }
    }

    fun fetchUserPlaylists() {
        scope.launch {
            userPlaylists = GlobalYouTubeRepository.instance.getUserPlaylists()
        }
    }

    fun addToPlaylist(playlistId: String, song: SongItem) {
        scope.launch {
            GlobalYouTubeRepository.instance.addToPlaylist(playlistId, song.id)
        }
    }

    fun setLyricsProvider(provider: LyricsProvider) {
        lyricsProviderPref = provider
        prefs.put("LYRICS_PROVIDER", provider.name)
        try { prefs.flush() } catch (_: Exception) {}
        // Re-fetch lyrics with new provider
        currentTrack?.let { fetchLyrics(it) }
    }

    fun copyToClipboard(text: String) {
        try {
            val sel = java.awt.datatransfer.StringSelection(text)
            java.awt.Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, sel)
        } catch (_: Exception) {}
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
    
    fun toggleMiniplayerNightMode(enabled: Boolean) {
        miniplayerNightMode = enabled
        prefs.putBoolean("MINIPLAYER_NIGHT_MODE", enabled)
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

    fun toggleMiniplayerFocusMode() {
        miniplayerFocusMode = !miniplayerFocusMode
        prefs.putBoolean("MINIPLAYER_FOCUS_MODE", miniplayerFocusMode)
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

    fun fetchHomeData(params: String? = null) {
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
                
                val result = GlobalYouTubeRepository.instance.getHomePageData(params)

                if (params == null) {
                    // Follow all continuation pages (like Android reference) to surface all sections
                    val allSections = result.sections.toMutableList()
                    var continuation = result.continuation
                    while (continuation != null) {
                        val (moreSections, nextToken) = GlobalYouTubeRepository.instance.getHomeSectionsContinuation(continuation)
                        allSections.addAll(moreSections)
                        continuation = nextToken
                    }
                    homePageData = result.copy(sections = allSections)
                } else {
                    homePageData = result.copy(chips = homePageData.chips)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isHomeLoading = false
            }
        }
    }

    fun toggleChip(chip: Chip?) {
        if (chip == null || chip == selectedChip && previousHomePageData != null) {
            homePageData = previousHomePageData ?: homePageData
            previousHomePageData = null
            selectedChip = null
            return
        }

        if (selectedChip == null) {
            previousHomePageData = homePageData
        }

        selectedChip = chip
        fetchHomeData(chip.params)
    }

    fun search(query: String) {
        if (query.isBlank()) return
        currentSearchQuery = query
        isSearchLoading = true
        selectedSearchFilter = null
        searchSummaryPage = null
        searchResultPage = null
        scope.launch {
            try {
                searchSummaryPage = GlobalYouTubeRepository.instance.getSearchSummary(query)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSearchLoading = false
            }
        }
    }

    fun updateSearchFilter(filter: SearchFilter?) {
        if (selectedSearchFilter == filter) return
        selectedSearchFilter = filter
        if (filter == null) {
            search(currentSearchQuery)
        } else {
            isSearchLoading = true
            scope.launch {
                try {
                    searchResultPage = GlobalYouTubeRepository.instance.searchFiltered(currentSearchQuery, filter)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isSearchLoading = false
                }
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

    fun fetchPlaylistData(id: String, thumbnailHint: String? = null) {
        isExpanded = false
        selectedPlaylistId = id
        selectedArtistId = null
        selectedAlbumId = null
        pendingPlaylistThumbnail = thumbnailHint
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
                    title = song.title,
                    artist = song.artists.joinToString { it.name },
                    duration = song.duration,
                    album = song.album?.name,
                    videoId = song.id,
                    provider = lyricsProviderPref
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
        homePageData = HomePageData()
        librarySections = emptyMap()
        try { prefs.flush() } catch (_: Exception) {}
        fetchHomeData() // Refresh home for guest
    }
}
