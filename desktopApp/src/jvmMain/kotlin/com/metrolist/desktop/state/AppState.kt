@file:Suppress("UNUSED_VARIABLE", "NAME_SHADOWING", "UNUSED_PARAMETER")
package com.metrolist.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.metrolist.shared.api.LyricsWithProvider
import com.metrolist.shared.api.lastfm.LastFM
import com.metrolist.shared.model.*
import com.metrolist.shared.playback.MusicPlayer
import com.metrolist.shared.playback.MusicQueue
import com.metrolist.shared.state.GlobalInnerTube
import com.metrolist.shared.state.GlobalYouTubeRepository
import com.metrolist.desktop.constants.DefaultThemeColor
import com.metrolist.desktop.BuildConfig
import com.metrolist.desktop.constants.RepeatMode
import com.metrolist.desktop.constants.SliderStyle
import com.metrolist.desktop.utils.DiscordRPCManager
import com.metrolist.desktop.utils.HistoryEntry
import com.metrolist.desktop.utils.HistoryRepository
import com.metrolist.desktop.utils.HistoryStat
import com.metrolist.desktop.utils.ScrobbleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.awt.Desktop
import java.net.URI
import java.util.prefs.Preferences
import io.ktor.client.request.*
import io.ktor.client.statement.*

enum class ThemeMode {
    LIGHT,
    DARK,
    AUTO  // Follows system theme
}

data class NavItem(val id: String, val label: String, val visible: Boolean = true)

// Language and country mappings
const val SYSTEM_DEFAULT = "SYSTEM_DEFAULT"

val LanguageCodeToName = mapOf(
    "SYSTEM_DEFAULT" to "System Default",
    "af" to "Afrikaans",
    "ar" to "العربية",
    "as" to "অসমীয়া",
    "az" to "Azərbaycan",
    "bg" to "Български",
    "bn" to "বাংলা",
    "ca" to "Català",
    "cs" to "Čeština",
    "da" to "Dansk",
    "de" to "Deutsch",
    "en" to "English (US)",
    "en-GB" to "English (UK)",
    "es" to "Español (España)",
    "es-419" to "Español (Latinoamérica)",
    "et" to "Eesti",
    "eu" to "Euskara",
    "fa" to "فارسی",
    "fi" to "Suomi",
    "fil" to "Filipino",
    "fr" to "Français",
    "fr-CA" to "Français (Canada)",
    "gl" to "Galego",
    "gu" to "ગુજરાતી",
    "he" to "עברית",
    "hi" to "हिन्दी",
    "hr" to "Hrvatski",
    "hu" to "Magyar",
    "id" to "Bahasa Indonesia",
    "is" to "Íslenska",
    "it" to "Italiano",
    "ja" to "日本語",
    "kk" to "Қазақ Тілі",
    "km" to "ខ្មែរ",
    "kn" to "ಕನ್ನಡ",
    "ko" to "한국어",
    "lt" to "Lietuvių",
    "mr" to "মরাठी",
    "ms" to "Bahasa Malaysia",
    "my" to "ဗမာ",
    "nl" to "Nederlands",
    "no" to "Norsk",
    "or" to "Odia",
    "pa" to "ਪੰਜਾਬੀ",
    "pl" to "Polski",
    "pt" to "Português (Brasil)",
    "pt-PT" to "Português",
    "ro" to "Română",
    "ru" to "Русский",
    "sk" to "Slovenčina",
    "sl" to "Slovenščina",
    "sq" to "Shqip",
    "sr" to "Срਪਸਕੀ",
    "sv" to "Svenska",
    "sw" to "Kiswahili",
    "ta" to "தமிழ்",
    "te" to "తెలుగు",
    "th" to "ไทย",
    "tr" to "Türkçe",
    "uk" to "Українська",
    "uz" to "O'zbe",
    "vi" to "Tiếng Việt",
    "zh-CN" to "中文 (简体)",
    "zh-HK" to "中文 (香港)",
    "zh-TW" to "中文 (繁體)",
    "zu" to "IsiZulu"
)

val CountryCodeToName = mapOf(
    "SYSTEM_DEFAULT" to "System Default",
    "AE" to "United Arab Emirates",
    "AR" to "Argentina",
    "AT" to "Austria",
    "AU" to "Australia",
    "AZ" to "Azerbaijan",
    "BA" to "Bosnia and Herzegovina",
    "BD" to "Bangladesh",
    "BE" to "Belgium",
    "BG" to "Bulgaria",
    "BH" to "Bahrain",
    "BO" to "Bolivia",
    "BR" to "Brazil",
    "BY" to "Belarus",
    "CA" to "Canada",
    "CH" to "Switzerland",
    "CL" to "Chile",
    "CM" to "Cameroon",
    "CN" to "China",
    "CO" to "Colombia",
    "CR" to "Costa Rica",
    "CZ" to "Czechia",
    "DE" to "Germany",
    "DK" to "Denmark",
    "DO" to "Dominican Republic",
    "DZ" to "Algeria",
    "EC" to "Ecuador",
    "EE" to "Estonia",
    "EG" to "Egypt",
    "ES" to "Spain",
    "FI" to "Finland",
    "FR" to "France",
    "GB" to "United Kingdom",
    "GE" to "Georgia",
    "GH" to "Ghana",
    "GR" to "Greece",
    "GT" to "Guatemala",
    "HK" to "Hong Kong",
    "HN" to "Honduras",
    "HR" to "Croatia",
    "HU" to "Hungary",
    "ID" to "Indonesia",
    "IE" to "Ireland",
    "IL" to "Israel",
    "IN" to "India",
    "IQ" to "Iraq",
    "IS" to "Iceland",
    "IT" to "Italy",
    "JM" to "Jamaica",
    "JO" to "Jordan",
    "JP" to "Japan",
    "KE" to "Kenya",
    "KH" to "Cambodia",
    "KR" to "South Korea",
    "KW" to "Kuwait",
    "KZ" to "Kazakhstan",
    "LB" to "Lebanon",
    "LI" to "Liechtenstein",
    "LK" to "Sri Lanka",
    "LT" to "Lithuania",
    "LU" to "Luxembourg",
    "LV" to "Latvia",
    "LY" to "Libya",
    "MA" to "Morocco",
    "MD" to "Moldova",
    "ME" to "Montenegro",
    "MK" to "Macedonia",
    "MN" to "Mongolia",
    "MT" to "Malta",
    "MX" to "Mexico",
    "MY" to "Malaysia",
    "NG" to "Nigeria",
    "NI" to "Nicaragua",
    "NL" to "Netherlands",
    "NO" to "Norway",
    "NP" to "Nepal",
    "NZ" to "New Zealand",
    "OM" to "Oman",
    "PA" to "Panama",
    "PE" to "Peru",
    "PG" to "Papua New Guinea",
    "PH" to "Philippines",
    "PK" to "Pakistan",
    "PL" to "Poland",
    "PR" to "Puerto Rico",
    "PT" to "Portugal",
    "PY" to "Paraguay",
    "QA" to "Qatar",
    "RO" to "Romania",
    "RS" to "Serbia",
    "RU" to "Russia",
    "SA" to "Saudi Arabia",
    "SE" to "Sweden",
    "SG" to "Singapore",
    "SI" to "Slovenia",
    "SK" to "Slovakia",
    "SN" to "Senegal",
    "SV" to "El Salvador",
    "TH" to "Thailand",
    "TN" to "Tunisia",
    "TR" to "Turkey",
    "TW" to "Taiwan",
    "TZ" to "Tanzania",
    "UA" to "Ukraine",
    "UG" to "Uganda",
    "US" to "United States",
    "UY" to "Uruguay",
    "VE" to "Venezuela",
    "VN" to "Vietnam",
    "YE" to "Yemen",
    "ZA" to "South Africa",
    "ZW" to "Zimbabwe"
)

object AppState {
    val client get() = GlobalInnerTube.client
    val prefs: Preferences = Preferences.userNodeForPackage(AppState::class.java)
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main + kotlinx.coroutines.Job())

    // SoundCloud artist image fetching
    private suspend fun fetchSoundCloudArtistImage(artistName: String): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            println("[DEBUG] fetchSoundCloudArtistImage: Starting fetch for artist '$artistName'")

            // Clean the artist name
            val cleanArtistName = artistName.trim()
                .replace("?", "")
                .replace("$", "s")
                .take(50)

            if (cleanArtistName.isBlank()) {
                return@withContext null
            }

            // Search SoundCloud user/artist
            val encodedName = cleanArtistName.replace(" ", "%20")
            val searchUrl = "https://soundcloud.com/search/people?q=$encodedName"

            val response = client.get(searchUrl) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
            }

            if (response.status.value == 200) {
                val html = response.bodyAsText()

                // Look for SoundCloud avatar images in the HTML
                val avatarRegex = Regex("""https://i1\.sndcdn\.com/avatars-[^"]*?-(?:large|t500x500)\.jpg""")
                val match = avatarRegex.find(html)

                if (match != null) {
                    val imageUrl = match.value
                    println("[DEBUG] fetchSoundCloudArtistImage: Found SoundCloud image: $imageUrl")
                    return@withContext imageUrl
                }
            }

            null
        } catch (e: Exception) {
            println("[ERROR] fetchSoundCloudArtistImage: Exception for '$artistName': ${e.message}")
            null
        }
    }

    // Custom banner storage and management
    private val customBanners = mutableMapOf<String, String>() // artistId -> imageUrl

    fun setCustomArtistBanner(artistId: String, imageUrl: String) {
        customBanners[artistId] = imageUrl
        // Save to preferences
        prefs.put("CUSTOM_BANNER_$artistId", imageUrl)
        try { prefs.flush() } catch (_: Exception) {}

        // Update banner cache
        artistBannerCache[artistId] = imageUrl
        println("[DEBUG] Set custom banner for artist $artistId: $imageUrl")
    }

    fun removeCustomArtistBanner(artistId: String) {
        customBanners.remove(artistId)
        prefs.remove("CUSTOM_BANNER_$artistId")
        try { prefs.flush() } catch (_: Exception) {}

        // Remove from cache
        artistBannerCache.remove(artistId)
        println("[DEBUG] Removed custom banner for artist $artistId")
    }

    fun getCustomArtistBanner(artistId: String): String? {
        return customBanners[artistId] ?: prefs.get("CUSTOM_BANNER_$artistId", null)
    }

    val player = MusicPlayer()
    val queue = MusicQueue()
    private val scrobbleManager = ScrobbleManager(scope)
    
    var currentTrack: SongItem? by mutableStateOf(null)
    var isPlaying by mutableStateOf(false)
    var progress by mutableStateOf(0f)
    var volume by mutableStateOf(prefs.getFloat("PLAYER_VOLUME", 0.7f))
    var crossfadeEnabled by mutableStateOf(prefs.getBoolean("CROSSFADE_ENABLED", false))
    var crossfadeDuration by mutableStateOf(prefs.getFloat("CROSSFADE_DURATION", 5f))
    private var crossfadeOutJob: Job? = null
    private var crossfadeInJob: Job? = null
    private var crossfadeOutTriggered = false
    
    // Remember expanded state
    var isExpanded by mutableStateOf(prefs.getBoolean("PLAYER_EXPANDED", false))
    
    var seedColor by mutableStateOf(DefaultThemeColor)
    var isWindowFocused by mutableStateOf(true)
    var isMaximized by mutableStateOf(false)
    var showSettings by mutableStateOf(false)
    var showSignIn by mutableStateOf(false)
    var showIntegrations by mutableStateOf(false)
    var availableUpdate by mutableStateOf<String?>(null)

    // Settings sub-screens
    var showAppearanceSettings by mutableStateOf(false)
    var showThemeSettings by mutableStateOf(false)
    var showSliderStyleDialog by mutableStateOf(false)
    var showPlayerSettings by mutableStateOf(false)
    var showContentSettings by mutableStateOf(false)
    var showAiSettings by mutableStateOf(false)
    var showPrivacySettings by mutableStateOf(false)
    var showStorageSettings by mutableStateOf(false)
    var showAboutSettings by mutableStateOf(false)
    
    var selectedArtistId by mutableStateOf<String?>(null)
    var artistData by mutableStateOf<Map<String, List<YTItem>>>(emptyMap())
    var artistSectionBrowseIds by mutableStateOf<Map<String, Pair<String?, String?>>>(emptyMap())
    var isArtistLoading by mutableStateOf(false)
    var artistIsSubscribed by mutableStateOf(false)
    var topBarScrollOffset by mutableStateOf(Int.MAX_VALUE)

    // Artist photos and source
    var artistPhotos by mutableStateOf<List<ArtistPhoto>>(emptyList())
    var isArtistPhotosLoading by mutableStateOf(false)
    var artistBannerSource by mutableStateOf(runCatching { ArtistSource.valueOf(prefs.get("ARTIST_BANNER_SOURCE", ArtistSource.YOUTUBE.name)) }.getOrDefault(ArtistSource.YOUTUBE))
    var artistIconSource by mutableStateOf(runCatching { ArtistSource.valueOf(prefs.get("ARTIST_ICON_SOURCE", ArtistSource.YOUTUBE.name)) }.getOrDefault(ArtistSource.YOUTUBE))
    // Transient per-artist: which specific Last.fm photo is actively shown as banner (null = none selected)
    var selectedLastFmPhotoUrl by mutableStateOf<String?>(null)
    var lastFmPhotoAutoCycle by mutableStateOf(prefs.getBoolean("LASTFM_PHOTO_AUTO_CYCLE", false))
    var lastFmPhotoCycleInterval by mutableStateOf(prefs.getFloat("LASTFM_PHOTO_CYCLE_INTERVAL", 6f))
    private var autoCycleJob: Job? = null

    // Checkmark display control: show briefly when manually selected, hide during auto-cycle
    var recentlySelectedPhotoUrl by mutableStateOf<String?>(null)
    private var checkmarkHideJob: Job? = null

    // Artist section "See all" view
    var artistSectionTitle by mutableStateOf<String?>(null)
    var artistSectionItems by mutableStateOf<List<YTItem>>(emptyList())
    var isArtistSectionLoading by mutableStateOf(false)

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
    var sliderStyleState by mutableStateOf(runCatching { SliderStyle.valueOf(prefs.get("SLIDER_STYLE", SliderStyle.DEFAULT.name)) }.getOrDefault(SliderStyle.DEFAULT))
    var discordRpcButton1Visible by mutableStateOf(prefs.getBoolean("DISCORD_RPC_BUTTON1_VISIBLE", true))
    var discordRpcButton2Visible by mutableStateOf(prefs.getBoolean("DISCORD_RPC_BUTTON2_VISIBLE", true))
    var shuffleEnabled by mutableStateOf(false)
    var repeatMode by mutableStateOf(RepeatMode.OFF)

    var swapPlayerControls by mutableStateOf(prefs.getBoolean("SWAP_PLAYER_CONTROLS", false))
    var animatedGradient by mutableStateOf(prefs.getBoolean("ANIMATED_GRADIENT", false))

    var currentLyrics by mutableStateOf<String?>(null)
    var isLyricsLoading by mutableStateOf(false)
    var currentLyricsProvider by mutableStateOf<String?>(null)

    // Search Results
    var searchSummaryPage by mutableStateOf<SearchSummaryPage?>(null)
    var searchResultPage by mutableStateOf<SearchResultPage?>(null)
    var selectedSearchFilter by mutableStateOf<SearchFilter?>(null)
    var isSearchLoading by mutableStateOf(false)
    var currentSearchQuery by mutableStateOf("")

    // Discord RPC Preferences
    var discordRpcEnabled by mutableStateOf(prefs.getBoolean("DISCORD_RPC_ENABLED", false))
    var discordRpcShowIdle by mutableStateOf(prefs.getBoolean("DISCORD_RPC_SHOW_IDLE", true))
    var discordRpcUseDetails by mutableStateOf(prefs.getBoolean("DISCORD_RPC_USE_DETAILS", true))
    var discordRpcShowButtons by mutableStateOf(prefs.getBoolean("DISCORD_RPC_SHOW_BUTTONS", true))
    var discordRpcAppId by mutableStateOf(prefs.get("DISCORD_RPC_APP_ID", ""))
    var discordRpcActivityType by mutableStateOf(prefs.get("DISCORD_RPC_ACTIVITY_TYPE", "LISTENING"))
    var discordRpcButton1Text by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON1_TEXT", "Listen on YouTube Music"))
    var discordRpcButton2Text by mutableStateOf(prefs.get("DISCORD_RPC_BUTTON2_TEXT", "Visit Metrolist"))
    var discordRpcAppIdPref by mutableStateOf(prefs.get("DISCORD_RPC_APP_ID", "")) // duplicate appId workaround
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
    var likedSongIds by mutableStateOf(
        prefs.get("LIKED_SONG_IDS", "").split(",").filter { it.isNotBlank() }.toMutableSet()
    )
    var userPlaylists by mutableStateOf<List<PlaylistItem>>(emptyList())
    var showAddToPlaylistSong by mutableStateOf<SongItem?>(null)

    // Discord RPC - cached artist icon
    var cachedArtistIcon by mutableStateOf<String?>(null)
    val artistIconCache = mutableMapOf<String, String>()

    // Banner cache to prevent YouTube banner flash when using Last.fm
    val artistBannerCache = mutableMapOf<String, String>()

    // Lyrics provider preferences — ordered list with per-provider enable/disable (matches Android app default)
    private val defaultLyricsOrder = listOf(
        LyricsProvider.LRCLIB, LyricsProvider.BETTERLYRICS, LyricsProvider.SIMPMUSIC,
        LyricsProvider.LYRICSPLUS, LyricsProvider.YOUTUBE
    )
    var lyricsProviderOrder by mutableStateOf(
        prefs.get("LYRICS_PROVIDER_ORDER", null)
            ?.split(",")
            ?.mapNotNull { runCatching { LyricsProvider.valueOf(it.trim()) }.getOrNull() }
            ?.filter { it != LyricsProvider.AUTO }
            ?.takeIf { it.isNotEmpty() }
            ?: defaultLyricsOrder
    )
    var lyricsEnabledProviders by mutableStateOf(
        prefs.get("LYRICS_ENABLED_PROVIDERS", null)
            ?.split(",")
            ?.mapNotNull { runCatching { LyricsProvider.valueOf(it.trim()) }.getOrNull() }
            ?.toSet()
            ?: defaultLyricsOrder.toSet()
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
                        
                        // Icon Resolution Priority: Source Preference -> Explicit Selection -> Default Thumbnail
                        scope.launch {
                            val artistId = firstArtist.id
                            val cacheKey = artistId ?: firstArtist.name
                            
                            val icon = when (artistIconSource) {
                                ArtistSource.YOUTUBE -> existingThumb ?: (if (!artistId.isNullOrEmpty()) GlobalYouTubeRepository.instance.fetchArtistThumbnail(artistId) else null)
                                ArtistSource.ITUNES -> GlobalYouTubeRepository.instance.fetchArtistIcon(firstArtist.name)
                                ArtistSource.LASTFM -> fetchLastFmIcon(firstArtist.name)
                                ArtistSource.SOUNDCLOUD -> fetchSoundCloudArtistImage(firstArtist.name)
                                    ?: fetchLastFmIcon(firstArtist.name) // Fallback to Last.fm only, not YouTube
                                ArtistSource.CUSTOM -> getCustomArtistBanner(artistId ?: firstArtist.name)
                                    ?: fetchLastFmIcon(firstArtist.name) // Fallback to Last.fm if no custom banner set
                            }

                            cachedArtistIcon = icon ?: when (artistIconSource) {
                                ArtistSource.YOUTUBE -> existingThumb // Only fallback to YouTube thumbnail if YouTube is the selected source
                                else -> null // For non-YouTube sources, don't fallback to YouTube thumbnail
                            }
                            updateDiscordRpc()
                        }
                    }
                    fetchLyrics(song)
                    // If the song has no album (common for homepage/artist page carousels),
                    // fetch it in the background via /next and patch currentTrack.
                    if (song.album == null) {
                        scope.launch {
                            val album = GlobalYouTubeRepository.instance.getSongAlbum(song.id)
                            if (album != null && currentTrack?.id == song.id) {
                                currentTrack = currentTrack?.copy(album = album)
                                updateDiscordRpc()
                            }
                        }
                    }
                    // Record play in persistent history
                    if (!pauseListenHistory) {
                        withContext(Dispatchers.IO) { HistoryRepository.recordPlay(song) }
                    }
                    refreshHistory()
                } else {
                    currentLyrics = null
                    currentLyricsProvider = null
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
                skipNext(fromEOF = true)
            }
        }

        // Crossfade: detect when track is near end and trigger fade-out
        scope.launch {
            combine(player.currentPosition, player.duration) { pos, dur -> pos to dur }
                .collectLatest { (pos, dur) ->
                    if (!crossfadeEnabled || dur <= 0L) return@collectLatest
                    val remaining = dur - pos
                    val threshold = (crossfadeDuration * 1000f).toLong()
                    if (remaining in 1L..threshold && !crossfadeOutTriggered) {
                        crossfadeOutTriggered = true
                        triggerCrossfadeOut(remaining)
                    }
                }
        }
        
        // Periodic Discord RPC update for when nothing is playing (keep Paused state alive)
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000)
                if (!isPlaying || currentTrack == null) updateDiscordRpc()
            }
        }

        // Check for a newer release on GitHub
        scope.launch(Dispatchers.IO) {
            try {
                val conn = URI("https://api.github.com/repos/LampDelivery/Metrolist-Desktop/releases/latest")
                    .toURL().openConnection() as java.net.HttpURLConnection
                conn.setRequestProperty("User-Agent", "Metrolist-Desktop/${BuildConfig.APP_VERSION}")
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.connect()
                val body = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                val tag = Json.parseToJsonElement(body).jsonObject["tag_name"]
                    ?.jsonPrimitive?.contentOrNull?.trimStart('v') ?: return@launch
                if (isNewerVersion(tag, BuildConfig.APP_VERSION)) availableUpdate = tag
            } catch (_: Exception) {}
        }

        // Load play history on startup
        refreshHistory()
    }

    private suspend fun fetchLastFmIcon(name: String): String? {
        println("[DEBUG] fetchLastFmIcon: Starting fetch for artist '$name'")
        return try {
            // Use Last.fm API to get the highest quality artist image
            val artistInfo = LastFM.getArtistInfo(client, name)
            println("[DEBUG] fetchLastFmIcon: Last.fm API response: ${artistInfo != null}")

            val images = artistInfo?.get("image")?.jsonArray
            println("[DEBUG] fetchLastFmIcon: Images array size: ${images?.size}")

            // Last.fm returns images in multiple sizes, get the largest one
            val largeImage = images?.lastOrNull()?.jsonObject?.get("#text")?.jsonPrimitive?.contentOrNull
            if (largeImage?.isNotBlank() == true) {
                println("[DEBUG] fetchLastFmIcon: Found large image: $largeImage")
                return largeImage
            } else {
                println("[DEBUG] fetchLastFmIcon: No large image found")
            }

            // Fallback to any available image if large is missing
            images?.forEach { imageElement ->
                val url = imageElement.jsonObject["#text"]?.jsonPrimitive?.contentOrNull
                println("[DEBUG] fetchLastFmIcon: Checking image URL: $url")
                if (url?.isNotBlank() == true && url != "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png") {
                    println("[DEBUG] fetchLastFmIcon: Using fallback image: $url")
                    return url
                }
            }

            println("[DEBUG] fetchLastFmIcon: No valid images found")
            null
        } catch (e: Exception) {
            println("[DEBUG] fetchLastFmIcon: Exception occurred for '$name': ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private suspend fun fetchSpotifyIcon(name: String): String? {
        println("[DEBUG] fetchSpotifyIcon: Starting fetch for artist '$name'")
        return try {
            // Skip direct Spotify API due to anti-bot measures, start with fallbacks

            // Fallback to Last.fm for high-quality artist images
            println("[DEBUG] fetchSpotifyIcon: Trying Last.fm for '$name'")
            val lastfmIcon = fetchLastFmIcon(name)
            if (lastfmIcon != null) {
                println("[DEBUG] fetchSpotifyIcon: Last.fm returned: $lastfmIcon")
                return lastfmIcon
            } else {
                println("[DEBUG] fetchSpotifyIcon: Last.fm returned null")
            }

            // Fallback to DuckDuckGo image search (music-focused)
            println("[DEBUG] fetchSpotifyIcon: Trying DuckDuckGo for '$name'")
            val duckDuckGoIcon = fetchDuckDuckGoIcon(name)
            if (duckDuckGoIcon != null) {
                println("[DEBUG] fetchSpotifyIcon: DuckDuckGo returned: $duckDuckGoIcon")
                return duckDuckGoIcon
            } else {
                println("[DEBUG] fetchSpotifyIcon: DuckDuckGo returned null")
            }

            // Final fallback to YouTube Music search (only if Spotify is not the selected source)
            println("[DEBUG] fetchSpotifyIcon: Trying YouTube Music for '$name' as final fallback")
            val ytMusicIcon = GlobalYouTubeRepository.instance.fetchArtistIcon(name)
            if (ytMusicIcon != null) {
                println("[DEBUG] fetchSpotifyIcon: YouTube Music returned: $ytMusicIcon")
                return ytMusicIcon
            } else {
                println("[DEBUG] fetchSpotifyIcon: YouTube Music returned null")
            }

            println("[DEBUG] fetchSpotifyIcon: All sources failed for '$name'")
            null
        } catch (e: Exception) {
            println("[DEBUG] fetchSpotifyIcon: Exception occurred for '$name': ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private suspend fun fetchDuckDuckGoIcon(name: String): String? {
        return try {
            val encodedName = name.replace(" ", "+")
            val response = client.get("https://duckduckgo.com/i.js?q=${encodedName}+artist+music&o=json&p=1&s=0").bodyAsText()
            val regex = Regex("\"image\":\"([^\"]+)\"")
            regex.find(response)?.groups?.get(1)?.value
        } catch (_: Exception) { null }
    }

    private suspend fun fetchBingIcon(name: String): String? {
        return try {
            val encodedName = name.replace(" ", "%20")
            val response = client.get("https://www.bing.com/images/search?q=${encodedName}+artist+musician").bodyAsText()
            // Look for high-quality image URLs in Bing search results
            val regex = Regex("murl&quot;:&quot;([^&]+)&quot;")
            val matches = regex.findAll(response)
            // Filter for likely artist images (avoid low-res or irrelevant images)
            for (match in matches) {
                val url = match.groups[1]?.value?.replace("\\u0026", "&") ?: continue
                if (url.contains("jpg", true) || url.contains("jpeg", true) || url.contains("png", true)) {
                    return url
                }
            }
            null
        } catch (_: Exception) { null }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val l = latest.split(".").mapNotNull { it.toIntOrNull() }
        val c = current.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(l.size, c.size)) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv > cv) return true
            if (lv < cv) return false
        }
        return false
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
        if (query.isBlank() || pauseSearchHistory) return
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
    var autoNightMode by mutableStateOf(prefs.getBoolean("AUTO_NIGHT_MODE", false))

    // Modern theme system (replaces pure black with proper dark/light/auto modes)
    private var _themeMode by mutableStateOf(
        when (prefs.get("THEME_MODE", "AUTO")) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.AUTO
        }
    )

    var themeMode: ThemeMode
        get() = _themeMode
        set(value) {
            _themeMode = value
            prefs.put("THEME_MODE", value.name)
            try { prefs.flush() } catch (_: Exception) {}
        }
    var dynamicTheme by mutableStateOf(prefs.getBoolean("DYNAMIC_THEME", true))
    var selectedThemeColor by mutableStateOf(
        prefs.getLong("SELECTED_THEME_COLOR", DefaultThemeColor.value.toLong())
    )

    // Appearance Settings
    var enableHighRefreshRate by mutableStateOf(prefs.getBoolean("ENABLE_HIGH_REFRESH_RATE", true))
    var newMiniPlayerDesign by mutableStateOf(prefs.getBoolean("NEW_MINI_PLAYER_DESIGN", true))
    var newPlayerDesign by mutableStateOf(prefs.getBoolean("NEW_PLAYER_DESIGN", true))
    var hidePlayerThumbnail by mutableStateOf(prefs.getBoolean("HIDE_PLAYER_THUMBNAIL", false))
    var cropAlbumArt by mutableStateOf(prefs.getBoolean("CROP_ALBUM_ART", false))
    var playerPanelSide by mutableStateOf(prefs.get("PLAYER_PANEL_SIDE", "right"))
    var useLoginForBrowse by mutableStateOf(prefs.getBoolean("USE_LOGIN_FOR_BROWSE", true))
    var ytmSync by mutableStateOf(prefs.getBoolean("YTM_SYNC", true))

    // Content Settings
    var hideExplicit by mutableStateOf(prefs.getBoolean("HIDE_EXPLICIT", false))
    var hideVideoSongs by mutableStateOf(prefs.getBoolean("HIDE_VIDEO_SONGS", false))
    var showArtistPhotos by mutableStateOf(prefs.getBoolean("SHOW_ARTIST_PHOTOS", true))

    // Library playlist visibility settings
    var showLikedPlaylist by mutableStateOf(prefs.getBoolean("SHOW_LIKED_PLAYLIST", true))
    var showDownloadedPlaylist by mutableStateOf(prefs.getBoolean("SHOW_DOWNLOADED_PLAYLIST", true))
    var showTopPlaylist by mutableStateOf(prefs.getBoolean("SHOW_TOP_PLAYLIST", true))
    var showUploadedPlaylist by mutableStateOf(prefs.getBoolean("SHOW_UPLOADED_PLAYLIST", true))
    var showCachedPlaylist by mutableStateOf(prefs.getBoolean("SHOW_CACHED_PLAYLIST", true))

    // Home section configuration
    var homeSectionOrder by mutableStateOf(
        prefs.get("HOME_SECTION_ORDER", "").split(",").filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
    )
    var showMixedForYou by mutableStateOf(prefs.getBoolean("SHOW_MIXED_FOR_YOU", true))
    var showListenAgain by mutableStateOf(prefs.getBoolean("SHOW_LISTEN_AGAIN", true))
    var showRecentlyPlayed by mutableStateOf(prefs.getBoolean("SHOW_RECENTLY_PLAYED", true))
    var showQuickPicks by mutableStateOf(prefs.getBoolean("SHOW_QUICK_PICKS", true))
    var showCharts by mutableStateOf(prefs.getBoolean("SHOW_CHARTS", true))
    var showNewReleases by mutableStateOf(prefs.getBoolean("SHOW_NEW_RELEASES", true))
    var showMadeForYou by mutableStateOf(prefs.getBoolean("SHOW_MADE_FOR_YOU", true))
    var showSimilarTo by mutableStateOf(prefs.getBoolean("SHOW_SIMILAR_TO", true))
    var showTrendingMusic by mutableStateOf(prefs.getBoolean("SHOW_TRENDING_MUSIC", true))

    // Content and language settings
    var contentLanguage by mutableStateOf(prefs.get("CONTENT_LANGUAGE", "SYSTEM_DEFAULT"))
    var contentCountry by mutableStateOf(prefs.get("CONTENT_COUNTRY", "SYSTEM_DEFAULT"))
    var appLanguage by mutableStateOf(prefs.get("APP_LANGUAGE", "SYSTEM_DEFAULT"))

    // Artist page display settings
    var showArtistDescription by mutableStateOf(prefs.getBoolean("SHOW_ARTIST_DESCRIPTION", true))
    var showArtistSubscriberCount by mutableStateOf(prefs.getBoolean("SHOW_ARTIST_SUBSCRIBER_COUNT", true))
    var showMonthlyListeners by mutableStateOf(prefs.getBoolean("SHOW_MONTHLY_LISTENERS", true))

    // Proxy settings
    var proxyEnabled by mutableStateOf(prefs.getBoolean("PROXY_ENABLED", false))
    var proxyUrl by mutableStateOf(prefs.get("PROXY_URL", "host:port"))
    var proxyType by mutableStateOf(prefs.get("PROXY_TYPE", "HTTP"))
    var proxyUsername by mutableStateOf(prefs.get("PROXY_USERNAME", "username"))
    var proxyPassword by mutableStateOf(prefs.get("PROXY_PASSWORD", "password"))

    // Home page settings
    var randomizeHomeOrder by mutableStateOf(prefs.getBoolean("RANDOMIZE_HOME_ORDER", true))
    var topListLength by mutableStateOf(prefs.get("TOP_SIZE", "50").toFloatOrNull() ?: 50f)
    var quickPicksMode by mutableStateOf(prefs.get("QUICK_PICKS_MODE", "QUICK_PICKS"))

    // Player Settings
    var audioQuality by mutableStateOf(prefs.get("AUDIO_QUALITY", "AUTO"))
    var audioOffload by mutableStateOf(prefs.getBoolean("AUDIO_OFFLOAD", false))
    var seekExtraSeconds by mutableStateOf(prefs.getBoolean("SEEK_EXTRA_SECONDS", false))
    var pauseOnMute by mutableStateOf(prefs.getBoolean("PAUSE_ON_MUTE", false))
    var resumeOnBluetoothConnect by mutableStateOf(prefs.getBoolean("RESUME_ON_BLUETOOTH_CONNECT", false))
    var keepScreenOn by mutableStateOf(prefs.getBoolean("KEEP_SCREEN_ON", false))

    // Appearance Settings
    var miniPlayerOutline by mutableStateOf(prefs.getBoolean("MINI_PLAYER_OUTLINE", false))
    var squigglySlider by mutableStateOf(prefs.getBoolean("SQUIGGLY_SLIDER", false))
    var sliderStyle by mutableStateOf(SliderStyle.valueOf(prefs.get("SLIDER_STYLE", "DEFAULT")))

    // Developer & Updates
    var developerMode by mutableStateOf(prefs.getBoolean("DEVELOPER_MODE", false))
    var checkForUpdates by mutableStateOf(prefs.getBoolean("CHECK_FOR_UPDATES", true))

    // UI Navigation States
    var showIntegrationsSettings by mutableStateOf(false)

    // Player Behavior Settings
    var persistentQueue by mutableStateOf(prefs.getBoolean("PERSISTENT_QUEUE", true))
    var persistentShuffleAcrossQueues by mutableStateOf(prefs.getBoolean("PERSISTENT_SHUFFLE_ACROSS_QUEUES", false))
    var rememberShuffleAndRepeat by mutableStateOf(prefs.getBoolean("REMEMBER_SHUFFLE_AND_REPEAT", false))
    var swipeToSong by mutableStateOf(prefs.getBoolean("SWIPE_TO_SONG", false))
    var swipeToRemoveSong by mutableStateOf(prefs.getBoolean("SWIPE_TO_REMOVE_SONG", false))
    var showLyrics by mutableStateOf(prefs.getBoolean("SHOW_LYRICS", false))

    // Integration Settings
    var lastFmUsername by mutableStateOf(prefs.get("LASTFM_USERNAME", ""))
    var showLastFmSettings by mutableStateOf(false)

    // Privacy Settings
    var pauseListenHistory by mutableStateOf(prefs.getBoolean("PAUSE_LISTEN_HISTORY", false))
    var pauseSearchHistory by mutableStateOf(prefs.getBoolean("PAUSE_SEARCH_HISTORY", false))

    // Default open tab (nav item ID)
    var defaultOpenTab by mutableStateOf(prefs.get("DEFAULT_OPEN_TAB", "home"))

    // Local playlist nav ("top50", "downloaded", "cached", "uploaded")
    var selectedLocalPlaylist: String? by mutableStateOf(null)

    fun playTrack(track: SongItem, list: List<SongItem>? = null) {
        crossfadeOutJob?.cancel()
        crossfadeInJob?.cancel()
        crossfadeOutTriggered = false
        player.setVolume((volume * 100).toInt())
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

    fun skipNext(fromEOF: Boolean = false) {
        // Sleep timer: stop after current song ends
        if (sleepTimerStopAfterCurrentSong) {
            player.pause()
            clearSleepTimer()
            return
        }
        // On manual skip, cancel crossfade and restore full volume immediately
        if (!fromEOF) {
            crossfadeOutJob?.cancel()
            crossfadeInJob?.cancel()
            crossfadeOutTriggered = false
            player.setVolume((volume * 100).toInt())
        }
        scope.launch {
            crossfadeOutTriggered = false // reset for incoming track
            val next = queue.getNext()
            if (next != null) {
                val url = GlobalYouTubeRepository.instance.getStreamUrl(next.id)
                if (url != null) {
                    if (crossfadeEnabled && fromEOF) player.setVolume(0)
                    player.play(next, url)
                    if (crossfadeEnabled && fromEOF) triggerCrossfadeIn()
                }
            } else if (repeatMode == RepeatMode.ALL && queue.items.value.isNotEmpty()) {
                queue.setQueue(queue.items.value, 0)
                val first = queue.currentItem()
                if (first != null) {
                    val url = GlobalYouTubeRepository.instance.getStreamUrl(first.id)
                    if (url != null) {
                        if (crossfadeEnabled && fromEOF) player.setVolume(0)
                        player.play(first, url)
                        if (crossfadeEnabled && fromEOF) triggerCrossfadeIn()
                    }
                }
            }
        }
    }

    fun skipPrevious() {
        crossfadeOutJob?.cancel()
        crossfadeInJob?.cancel()
        crossfadeOutTriggered = false
        player.setVolume((volume * 100).toInt())
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
            val top    = HistoryRepository.getMostPlayed(50)
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
            var addToken = song.libraryAddToken
            var removeToken = song.libraryRemoveToken

            // If tokens are missing (common for songs not fetched from library context),
            // fetch them dynamically from the /next endpoint.
            if (addToken == null && removeToken == null && isSignedIn) {
                val tokens = GlobalYouTubeRepository.instance.getSongLibraryTokens(song.id)
                addToken = tokens.first
                removeToken = tokens.second
            }

            val token = if (isLiked) removeToken else addToken
            if (token != null) {
                GlobalYouTubeRepository.instance.sendFeedback(listOf(token))
            }
            // Always update local liked state so the UI responds immediately.
            val newSet = likedSongIds.toMutableSet()
            if (isLiked) newSet.remove(song.id) else newSet.add(song.id)
            likedSongIds = newSet

            // Persist to preferences
            prefs.put("LIKED_SONG_IDS", likedSongIds.joinToString(","))
            try { prefs.flush() } catch (_: Exception) {}
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

    fun updateLyricsProviderOrder(order: List<LyricsProvider>) {
        lyricsProviderOrder = order
        prefs.put("LYRICS_PROVIDER_ORDER", order.joinToString(",") { it.name })
        try { prefs.flush() } catch (_: Exception) {}
        currentTrack?.let { fetchLyrics(it) }
    }

    fun toggleLyricsProvider(provider: LyricsProvider, enabled: Boolean) {
        lyricsEnabledProviders = if (enabled) lyricsEnabledProviders + provider else lyricsEnabledProviders - provider
        prefs.put("LYRICS_ENABLED_PROVIDERS", lyricsEnabledProviders.joinToString(",") { it.name })
        try { prefs.flush() } catch (_: Exception) {}
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

    fun toggleCrossfade(enabled: Boolean) {
        crossfadeEnabled = enabled
        prefs.putBoolean("CROSSFADE_ENABLED", enabled)
        if (!enabled) {
            crossfadeOutJob?.cancel()
            crossfadeInJob?.cancel()
            crossfadeOutTriggered = false
            player.setVolume((volume * 100).toInt())
        }
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateCrossfadeDuration(seconds: Float) {
        crossfadeDuration = seconds
        prefs.putFloat("CROSSFADE_DURATION", seconds)
        try { prefs.flush() } catch (_: Exception) {}
    }

    private fun triggerCrossfadeOut(remainingMs: Long) {
        crossfadeOutJob?.cancel()
        crossfadeOutJob = scope.launch {
            val steps = 20
            val startVolume = (volume * 100).toInt()
            val stepDelayMs = (remainingMs / steps).coerceAtLeast(50L)
            for (i in 1..steps) {
                val t = i.toFloat() / steps
                val eased = 1f - t * t  // quadratic ease-out: slow start, fast end
                player.setVolume((startVolume * eased).toInt().coerceAtLeast(0))
                delay(stepDelayMs)
            }
            player.setVolume(0)
        }
    }

    private fun triggerCrossfadeIn() {
        crossfadeInJob?.cancel()
        crossfadeInJob = scope.launch {
            val steps = 20
            val targetVolume = (volume * 100).toInt()
            val stepDelayMs = (crossfadeDuration * 1000f / steps).toLong().coerceAtLeast(50L)
            player.setVolume(0)
            for (i in 1..steps) {
                val t = i.toFloat() / steps
                val eased = t * t  // quadratic ease-in: fast start, slow end
                player.setVolume((targetVolume * eased).toInt())
                delay(stepDelayMs)
            }
            player.setVolume(targetVolume)
        }
    }

    fun updateFloatingPlayerMode(enabled: Boolean) {
        isFloatingPlayer = enabled
        prefs.putBoolean("FLOATING_PLAYER", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }
    
    fun updateSliderStyle(style: SliderStyle) {
        sliderStyle = style
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

    fun toggleAutoNightMode(enabled: Boolean) {
        autoNightMode = enabled
        prefs.putBoolean("AUTO_NIGHT_MODE", enabled)
        // Immediately apply to the current track using seed color luma as a proxy
        if (enabled) {
            val luma = 0.299f * seedColor.red + 0.587f * seedColor.green + 0.114f * seedColor.blue
            pureBlack = luma <= 0.3f
        }
        try { prefs.flush() } catch (_: Exception) {}
    }

    /** Called after each track's album art is decoded; no-op when autoNightMode is off. */
    fun applyAutoNightMode(isBright: Boolean) {
        if (autoNightMode) pureBlack = !isBright
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

    // Modern theme system functions

    fun toggleDynamicTheme(enabled: Boolean) {
        dynamicTheme = enabled
        prefs.putBoolean("DYNAMIC_THEME", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Appearance Settings Functions
    fun toggleHighRefreshRate(enabled: Boolean) {
        enableHighRefreshRate = enabled
        prefs.putBoolean("ENABLE_HIGH_REFRESH_RATE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleNewMiniPlayerDesign(enabled: Boolean) {
        newMiniPlayerDesign = enabled
        prefs.putBoolean("NEW_MINI_PLAYER_DESIGN", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleNewPlayerDesign(enabled: Boolean) {
        newPlayerDesign = enabled
        prefs.putBoolean("NEW_PLAYER_DESIGN", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleHidePlayerThumbnail(enabled: Boolean) {
        hidePlayerThumbnail = enabled
        prefs.putBoolean("HIDE_PLAYER_THUMBNAIL", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleCropAlbumArt(enabled: Boolean) {
        cropAlbumArt = enabled
        prefs.putBoolean("CROP_ALBUM_ART", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updatePlayerPanelSide(side: String) {
        playerPanelSide = side
        prefs.put("PLAYER_PANEL_SIDE", side)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleUseLoginForBrowse(enabled: Boolean) {
        useLoginForBrowse = enabled
        prefs.putBoolean("USE_LOGIN_FOR_BROWSE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleYtmSync(enabled: Boolean) {
        ytmSync = enabled
        prefs.putBoolean("YTM_SYNC", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun setSelectedThemeColor(color: ULong) {
        selectedThemeColor = color.toLong()
        prefs.putLong("SELECTED_THEME_COLOR", color.toLong())
        try { prefs.flush() } catch (_: Exception) {}
        if (!dynamicTheme) {
            // Update seed color when not using dynamic theme
            seedColor = androidx.compose.ui.graphics.Color(color)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
    }

    fun updateThemeColor(color: Long) {
        selectedThemeColor = color
        prefs.putLong("SELECTED_THEME_COLOR", color)
        try { prefs.flush() } catch (_: Exception) {}
        if (!dynamicTheme) {
            seedColor = androidx.compose.ui.graphics.Color(color.toULong())
        }
    }

    fun getCurrentThemeIsDark(): Boolean {
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AUTO -> {
                // Check system dark mode (Windows registry or JavaFX preference)
                try {
                    val osName = System.getProperty("os.name").lowercase()
                    when {
                        osName.contains("windows") -> {
                            // Check Windows registry for dark mode
                            val process = ProcessBuilder("reg", "query",
                                "HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                                "/v", "AppsUseLightTheme").start()
                            val output = process.inputStream.readBytes().toString(Charsets.UTF_8)
                            process.waitFor()
                            // AppsUseLightTheme: 0 = dark, 1 = light
                            !output.contains("0x1")
                        }
                        osName.contains("mac") -> {
                            // Check macOS system preference
                            val process = ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start()
                            val output = process.inputStream.readBytes().toString(Charsets.UTF_8)
                            process.waitFor()
                            output.trim().lowercase() == "dark"
                        }
                        else -> {
                            // Linux/other: fallback to current pure black setting
                            pureBlack
                        }
                    }
                } catch (_: Exception) {
                    // Fallback to current pure black setting if detection fails
                    pureBlack
                }
            }
        }
    }

    fun toggleHideExplicit(enabled: Boolean) {
        hideExplicit = enabled
        prefs.putBoolean("HIDE_EXPLICIT", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleHideVideoSongs(enabled: Boolean) {
        hideVideoSongs = enabled
        prefs.putBoolean("HIDE_VIDEO_SONGS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowArtistPhotos(enabled: Boolean) {
        showArtistPhotos = enabled
        prefs.putBoolean("SHOW_ARTIST_PHOTOS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Content and language settings functions
    fun updateContentLanguage(language: String) {
        contentLanguage = language
        prefs.put("CONTENT_LANGUAGE", language)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateContentCountry(country: String) {
        contentCountry = country
        prefs.put("CONTENT_COUNTRY", country)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateAppLanguage(language: String) {
        appLanguage = language
        prefs.put("APP_LANGUAGE", language)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Artist page display settings functions
    fun toggleShowArtistDescription(enabled: Boolean) {
        showArtistDescription = enabled
        prefs.putBoolean("SHOW_ARTIST_DESCRIPTION", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowArtistSubscriberCount(enabled: Boolean) {
        showArtistSubscriberCount = enabled
        prefs.putBoolean("SHOW_ARTIST_SUBSCRIBER_COUNT", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowMonthlyListeners(enabled: Boolean) {
        showMonthlyListeners = enabled
        prefs.putBoolean("SHOW_MONTHLY_LISTENERS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Home page settings functions
    fun toggleRandomizeHomeOrder(enabled: Boolean) {
        randomizeHomeOrder = enabled
        prefs.putBoolean("RANDOMIZE_HOME_ORDER", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateTopListLength(size: Float) {
        topListLength = size
        prefs.put("TOP_SIZE", size.toInt().toString())
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateQuickPicksMode(mode: String) {
        quickPicksMode = mode
        prefs.put("QUICK_PICKS_MODE", mode)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun togglePauseListenHistory(enabled: Boolean) {
        pauseListenHistory = enabled
        prefs.putBoolean("PAUSE_LISTEN_HISTORY", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun togglePauseSearchHistory(enabled: Boolean) {
        pauseSearchHistory = enabled
        prefs.putBoolean("PAUSE_SEARCH_HISTORY", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Player Settings Functions
    fun updateAudioQuality(quality: String) {
        audioQuality = quality
        prefs.put("AUDIO_QUALITY", quality)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleAudioOffload(enabled: Boolean) {
        audioOffload = enabled
        prefs.putBoolean("AUDIO_OFFLOAD", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun togglePersistentQueue(enabled: Boolean) {
        persistentQueue = enabled
        prefs.putBoolean("PERSISTENT_QUEUE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun togglePersistentShuffleAcrossQueues(enabled: Boolean) {
        persistentShuffleAcrossQueues = enabled
        prefs.putBoolean("PERSISTENT_SHUFFLE_ACROSS_QUEUES", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleRememberShuffleAndRepeat(enabled: Boolean) {
        rememberShuffleAndRepeat = enabled
        prefs.putBoolean("REMEMBER_SHUFFLE_AND_REPEAT", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleSwipeToSong(enabled: Boolean) {
        swipeToSong = enabled
        prefs.putBoolean("SWIPE_TO_SONG", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleSwipeToRemoveSong(enabled: Boolean) {
        swipeToRemoveSong = enabled
        prefs.putBoolean("SWIPE_TO_REMOVE_SONG", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowLyrics(enabled: Boolean) {
        showLyrics = enabled
        prefs.putBoolean("SHOW_LYRICS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleSeekExtraSeconds(enabled: Boolean) {
        seekExtraSeconds = enabled
        prefs.putBoolean("SEEK_EXTRA_SECONDS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun togglePauseOnMute(enabled: Boolean) {
        pauseOnMute = enabled
        prefs.putBoolean("PAUSE_ON_MUTE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleResumeOnBluetoothConnect(enabled: Boolean) {
        resumeOnBluetoothConnect = enabled
        prefs.putBoolean("RESUME_ON_BLUETOOTH_CONNECT", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        keepScreenOn = enabled
        prefs.putBoolean("KEEP_SCREEN_ON", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Appearance Settings Functions
    fun toggleMiniPlayerOutline(enabled: Boolean) {
        miniPlayerOutline = enabled
        prefs.putBoolean("MINI_PLAYER_OUTLINE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleSquigglySlider(enabled: Boolean) {
        squigglySlider = enabled
        prefs.putBoolean("SQUIGGLY_SLIDER", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Privacy Settings Functions
    fun toggleDeveloperMode(enabled: Boolean) {
        developerMode = enabled
        prefs.putBoolean("DEVELOPER_MODE", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Integration Settings Functions
    fun toggleCheckForUpdates(enabled: Boolean) {
        checkForUpdates = enabled
        prefs.putBoolean("CHECK_FOR_UPDATES", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun openUpdatePage() {
        try {
            Desktop.getDesktop().browse(URI("https://github.com/LampDelivery/Metrolist-Desktop/releases/latest"))
        } catch (_: Exception) { }
    }

    fun clearListenHistory() {
        scope.launch(Dispatchers.IO) {
            HistoryRepository.clearHistory()
            withContext(Dispatchers.Main) {
                historyEntries = emptyList()
                topSongs = emptyList()
            }
        }
    }

    fun clearSearchHistory() {
        searchHistory = emptyList()
        prefs.put("SEARCH_HISTORY", "")
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun updateDefaultOpenTab(tabId: String) {
        defaultOpenTab = tabId
        prefs.put("DEFAULT_OPEN_TAB", tabId)
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
        
        // Navigation Logic: ensure we leave detail pages when searching
        selectedArtistId = null
        selectedPlaylistId = null
        selectedAlbumId = null
        showSettings = false
        showSignIn = false
        showIntegrations = false
        selectedLocalPlaylist = null

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

    fun fetchArtistSection(sectionTitle: String, browseId: String, params: String?) {
        artistSectionTitle = sectionTitle
        artistSectionItems = emptyList()
        isArtistSectionLoading = true
        scope.launch {
            try {
                val data = GlobalYouTubeRepository.instance.getPlaylist(browseId)
                artistSectionItems = data.values.flatten().filter { it !is PlaylistItem || it.id != "header" }
            } catch (_: Exception) {}
            finally { isArtistSectionLoading = false }
        }
    }

    fun clearArtistSection() {
        artistSectionTitle = null
        artistSectionItems = emptyList()
        isArtistSectionLoading = false
    }

    fun fetchArtistData(id: String) {
        isExpanded = false
        selectedArtistId = id
        selectedPlaylistId = null
        selectedAlbumId = null
        isArtistLoading = true
        artistIsSubscribed = false
        artistPhotos = emptyList()
        selectedLastFmPhotoUrl = null
        recentlySelectedPhotoUrl = null
        autoCycleJob?.cancel()
        checkmarkHideJob?.cancel()
        scope.launch {
            try {
                val result = GlobalYouTubeRepository.instance.getArtist(id)
                artistData = result.sections
                artistSectionBrowseIds = result.sectionBrowseIds
                val header = (result.sections["header"]?.firstOrNull() as? ArtistItem)
                artistIsSubscribed = header?.isSubscribed ?: false
                
                header?.title?.let { name ->
                    fetchArtistPhotos(name, header.id)
                }
            } catch (_: Exception) {}
            finally {
                isArtistLoading = false
            }
        }
    }

    fun fetchArtistPhotos(name: String, artistId: String? = null) {
        isArtistPhotosLoading = true
        scope.launch(Dispatchers.IO) {
            try {
                val photos = mutableListOf<ArtistPhoto>()

                // Fetch from Last.fm — deduplicate by URL to prevent the same image appearing twice
                val lastfmPhotos = LastFM.getArtistPhotos(client, name)
                lastfmPhotos.filter { it.isNotBlank() }.distinctBy { it }.forEach {
                    photos.add(ArtistPhoto(it, "Last.fm"))
                }

                // Fetch from iTunes (high res icon)
                val itunesPhoto = GlobalYouTubeRepository.instance.fetchArtistIcon(name)
                itunesPhoto?.let { photos.add(ArtistPhoto(it, "iTunes")) }

                // Fetch from SoundCloud
                val soundcloudIcon = fetchSoundCloudArtistImage(name)
                soundcloudIcon?.let { photos.add(ArtistPhoto(it, "SoundCloud")) }

                withContext(Dispatchers.Main) {
                    artistPhotos = photos
                    // Restore saved manual selection or auto-select first Last.fm photo when banner source is LASTFM
                    if (artistBannerSource == ArtistSource.LASTFM && selectedLastFmPhotoUrl == null) {
                        val savedPhotoUrl = selectedArtistId?.let { getSavedLastFmPhotoUrl(it) }
                        selectedLastFmPhotoUrl = if (savedPhotoUrl != null && photos.any { it.url == savedPhotoUrl && it.source == "Last.fm" }) {
                            savedPhotoUrl // Restore manual selection if still available
                        } else {
                            photos.firstOrNull { it.source == "Last.fm" }?.url // Auto-select first as fallback
                        }
                    }
                    // Populate icon cache for the current artist based on icon source preference
                    if (artistId != null && artistIconSource != ArtistSource.YOUTUBE) {
                        val iconUrl = when (artistIconSource) {
                            ArtistSource.ITUNES -> itunesPhoto
                            ArtistSource.LASTFM -> photos.firstOrNull { it.source == "Last.fm" }?.url
                            ArtistSource.SOUNDCLOUD -> soundcloudIcon
                            ArtistSource.CUSTOM -> getCustomArtistBanner(artistId)
                            else -> null
                        }
                        iconUrl?.let { artistIconCache[artistId] = it }
                    }
                    // Populate banner cache to prevent YouTube banner flash
                    if (artistId != null) {
                        val bannerUrl = when (artistBannerSource) {
                            ArtistSource.ITUNES -> itunesPhoto
                            ArtistSource.LASTFM -> selectedLastFmPhotoUrl ?: photos.firstOrNull { it.source == "Last.fm" }?.url
                            ArtistSource.SOUNDCLOUD -> soundcloudIcon
                            ArtistSource.CUSTOM -> getCustomArtistBanner(artistId)
                            else -> null
                        }
                        bannerUrl?.let { artistBannerCache[artistId] = it }
                    }
                    // Start auto-cycle if enabled and we have multiple Last.fm photos
                    if (lastFmPhotoAutoCycle && photos.filter { it.source == "Last.fm" }.size > 1) {
                        startLastFmAutoCycle()
                    }
                }
            } catch (_: Exception) {}
            finally {
                isArtistPhotosLoading = false
            }
        }
    }

    fun updateArtistBannerSource(source: ArtistSource) {
        artistBannerSource = source
        prefs.put("ARTIST_BANNER_SOURCE", source.name)
        try { prefs.flush() } catch (_: Exception) {}

        // Update banner cache for current artist
        selectedArtistId?.let { artistId ->
            val bannerUrl = when (source) {
                ArtistSource.ITUNES -> artistPhotos.find { it.source == "iTunes" }?.url
                ArtistSource.SOUNDCLOUD -> artistPhotos.find { it.source == "SoundCloud" }?.url
                ArtistSource.LASTFM -> selectedLastFmPhotoUrl ?: artistPhotos.find { it.source == "Last.fm" }?.url
                ArtistSource.CUSTOM -> getCustomArtistBanner(artistId)
                else -> null
            }
            if (bannerUrl != null) {
                artistBannerCache[artistId] = bannerUrl
            } else {
                artistBannerCache.remove(artistId)
            }
        }

        // Show checkmark briefly for manual selection (not during auto-cycle)
        if (!lastFmPhotoAutoCycle) {
            val selectedPhotoUrl = when (source) {
                ArtistSource.ITUNES -> artistPhotos.find { it.source == "iTunes" }?.url
                ArtistSource.SOUNDCLOUD -> artistPhotos.find { it.source == "SoundCloud" }?.url
                ArtistSource.LASTFM -> selectedLastFmPhotoUrl
                ArtistSource.CUSTOM -> selectedArtistId?.let { getCustomArtistBanner(it) }
                else -> null
            }
            if (selectedPhotoUrl != null) {
                recentlySelectedPhotoUrl = selectedPhotoUrl
                checkmarkHideJob?.cancel()
                checkmarkHideJob = scope.launch {
                    delay(2000) // Show checkmark for 2 seconds
                    recentlySelectedPhotoUrl = null
                }
            }
        }

        // When switching to LASTFM and photos are already loaded, restore saved selection or auto-select the first one
        if (source == ArtistSource.LASTFM && selectedLastFmPhotoUrl == null) {
            val savedPhotoUrl = selectedArtistId?.let { getSavedLastFmPhotoUrl(it) }
            selectedLastFmPhotoUrl = if (savedPhotoUrl != null && artistPhotos.any { it.url == savedPhotoUrl && it.source == "Last.fm" }) {
                savedPhotoUrl // Restore manual selection if still available
            } else {
                artistPhotos.firstOrNull { it.source == "Last.fm" }?.url // Auto-select first as fallback
            }
        }
    }

    fun updateArtistIconSource(source: ArtistSource) {
        artistIconSource = source
        prefs.put("ARTIST_ICON_SOURCE", source.name)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Persistent manual photo selection: saves and restores last selected photo per artist
    private fun getSavedLastFmPhotoUrl(artistId: String): String? {
        return prefs.get("LASTFM_MANUAL_PHOTO_$artistId", null)
    }

    private fun saveLastFmPhotoUrl(artistId: String, url: String) {
        prefs.put("LASTFM_MANUAL_PHOTO_$artistId", url)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun selectLastFmPhoto(url: String) {
        selectedLastFmPhotoUrl = url
        // Save manual selection persistently
        selectedArtistId?.let { artistId ->
            saveLastFmPhotoUrl(artistId, url)
        }
        // Update banner cache if using Last.fm as banner source
        if (artistBannerSource == ArtistSource.LASTFM) {
            selectedArtistId?.let { artistId ->
                artistBannerCache[artistId] = url
            }
        }
        // Show checkmark briefly for manual selection
        if (!lastFmPhotoAutoCycle) {
            recentlySelectedPhotoUrl = url
            checkmarkHideJob?.cancel()
            checkmarkHideJob = scope.launch {
                delay(2000) // Show checkmark for 2 seconds
                recentlySelectedPhotoUrl = null
            }
        }
        if (artistBannerSource != ArtistSource.LASTFM) {
            artistBannerSource = ArtistSource.LASTFM
            prefs.put("ARTIST_BANNER_SOURCE", ArtistSource.LASTFM.name)
            try { prefs.flush() } catch (_: Exception) {}
        }
    }

    fun toggleLastFmAutoCycle() {
        lastFmPhotoAutoCycle = !lastFmPhotoAutoCycle
        prefs.putBoolean("LASTFM_PHOTO_AUTO_CYCLE", lastFmPhotoAutoCycle)
        try { prefs.flush() } catch (_: Exception) {}
        if (lastFmPhotoAutoCycle) {
            // Clear checkmarks when entering auto-cycle mode
            recentlySelectedPhotoUrl = null
            checkmarkHideJob?.cancel()

            val lastFmPhotos = artistPhotos.filter { it.source == "Last.fm" }
            if (lastFmPhotos.isNotEmpty()) {
                // Switch to LASTFM source if we have Last.fm photos
                if (artistBannerSource != ArtistSource.LASTFM) {
                    artistBannerSource = ArtistSource.LASTFM
                    prefs.put("ARTIST_BANNER_SOURCE", ArtistSource.LASTFM.name)
                }
                // Auto-select first Last.fm photo if none selected yet
                if (selectedLastFmPhotoUrl == null) {
                    selectedLastFmPhotoUrl = lastFmPhotos.firstOrNull()?.url
                }
                // Start auto-cycle if we have multiple Last.fm photos
                if (lastFmPhotos.size > 1) {
                    startLastFmAutoCycle()
                }
            }
        } else {
            autoCycleJob?.cancel()
        }
    }

    fun updateLastFmPhotoCycleInterval(seconds: Float) {
        lastFmPhotoCycleInterval = seconds
        prefs.putFloat("LASTFM_PHOTO_CYCLE_INTERVAL", seconds)
        try { prefs.flush() } catch (_: Exception) {}
        // Restart auto-cycle with new interval if it's currently running
        if (lastFmPhotoAutoCycle && artistPhotos.filter { it.source == "Last.fm" }.size > 1) {
            startLastFmAutoCycle()
        }
    }

    private fun startLastFmAutoCycle() {
        autoCycleJob?.cancel()
        autoCycleJob = scope.launch {
            while (true) {
                delay((lastFmPhotoCycleInterval * 1000).toLong())
                val photos = artistPhotos.filter { it.source == "Last.fm" }
                if (photos.size > 1) {
                    val currentIdx = photos.indexOfFirst { it.url == selectedLastFmPhotoUrl }
                    val nextIdx = (currentIdx + 1) % photos.size
                    selectedLastFmPhotoUrl = photos[nextIdx].url
                    if (artistBannerSource != ArtistSource.LASTFM) {
                        artistBannerSource = ArtistSource.LASTFM
                    }
                    // Update banner cache with new auto-cycled photo
                    selectedArtistId?.let { artistId ->
                        artistBannerCache[artistId] = photos[nextIdx].url
                    }
                }
            }
        }
    }

    fun toggleArtistSubscription() {
        val artistInfo = artistData["header"]?.firstOrNull() as? ArtistItem ?: return
        val newState = !artistIsSubscribed
        artistIsSubscribed = newState // optimistic update
        scope.launch {
            GlobalYouTubeRepository.instance.subscribeChannel(artistInfo.id, newState)
        }
    }

    fun startArtistRadio() {
        val artistInfo = artistData["header"]?.firstOrNull() as? ArtistItem ?: return
        scope.launch {
            try {
                val radioSongs = GlobalYouTubeRepository.instance.startArtistRadio(artistInfo.id)
                if (radioSongs.isNotEmpty()) {
                    queue.setQueue(radioSongs, 0)
                    val url = GlobalYouTubeRepository.instance.getStreamUrl(radioSongs[0].id)
                    if (url != null) player.play(radioSongs[0], url)
                }
            } catch (_: Exception) {}
        }
    }

    fun shuffleArtistSongs() {
        val songs = artistData.entries
            .filter { it.key != "header" }
            .flatMap { it.value }
            .filterIsInstance<SongItem>()
        if (songs.isEmpty()) return
        val shuffled = songs.shuffled()
        scope.launch {
            queue.setQueue(shuffled, 0)
            val url = GlobalYouTubeRepository.instance.getStreamUrl(shuffled[0].id)
            if (url != null) player.play(shuffled[0], url)
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
        currentLyricsProvider = null
        isLyricsLoading = true
        scope.launch {
            try {
                val orderedEnabled = lyricsProviderOrder.filter { it in lyricsEnabledProviders }
                var bestResult: com.metrolist.shared.api.LyricsWithProvider? = null
                var fallbackResult: com.metrolist.shared.api.LyricsWithProvider? = null

                for (provider in orderedEnabled) {
                    val candidate = GlobalYouTubeRepository.instance.getLyrics(
                        title = song.title,
                        artist = song.artists.joinToString { it.name },
                        duration = song.duration,
                        album = song.album?.name,
                        videoId = song.id,
                        provider = provider
                    )
                    if (candidate.lyrics != null) {
                        val parsed = com.metrolist.desktop.ui.components.parseLrc(candidate.lyrics!!)
                        val hasWordSync = parsed.any { it.words != null }

                        if (hasWordSync) {
                            // Prefer first provider with word-level timing
                            bestResult = candidate
                            break
                        } else if (fallbackResult == null) {
                            // Keep first provider with any lyrics as fallback
                            fallbackResult = candidate
                        }
                    }
                }

                val result = bestResult ?: fallbackResult
                currentLyrics = result?.lyrics
                currentLyricsProvider = if (result?.lyrics != null) result.provider else null
            } catch (_: Exception) {
                currentLyrics = "Lyrics not found"
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

    // Artist photo fetching methods
    fun fetchArtistIconOnDemand(artistId: String, artistName: String, source: ArtistSource) {
        scope.launch(Dispatchers.IO) {
            try {
                val photoUrl = when (source) {
                    ArtistSource.LASTFM -> fetchLastFmIcon(artistName)
                    ArtistSource.SOUNDCLOUD -> fetchSoundCloudArtistImage(artistName)
                        ?: fetchLastFmIcon(artistName) // Fallback to Last.fm if SoundCloud fails
                    ArtistSource.ITUNES -> GlobalYouTubeRepository.instance.fetchArtistIcon(artistName)
                    ArtistSource.CUSTOM -> getCustomArtistBanner(artistId)
                    else -> null
                }
                if (photoUrl != null) {
                    withContext(Dispatchers.Main) {
                        artistIconCache[artistId] = photoUrl
                    }
                }
            } catch (e: Exception) {
                println("Failed to fetch artist icon on demand: ${e.message}")
            }
        }
    }

    suspend fun fetchArtistBannerOnDemand(artistId: String, artistName: String, source: ArtistSource): String? {
        return try {
            when (source) {
                ArtistSource.LASTFM -> fetchLastFmIcon(artistName)
                ArtistSource.SOUNDCLOUD -> fetchSoundCloudArtistImage(artistName)
                    ?: fetchLastFmIcon(artistName) // Fallback to Last.fm if SoundCloud fails
                ArtistSource.ITUNES -> GlobalYouTubeRepository.instance.fetchArtistIcon(artistName)
                ArtistSource.CUSTOM -> getCustomArtistBanner(artistId)
                else -> null
            }
        } catch (e: Exception) {
            println("Failed to fetch artist banner on demand: ${e.message}")
            null
        }
    }

    // Album playback method
    fun playAlbum(albumId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val albumPage = GlobalYouTubeRepository.instance.getAlbum(albumId)
                val songs = albumPage?.songs ?: emptyList()
                if (songs.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        playTrack(songs.first(), songs)
                    }
                }
            } catch (e: Exception) {
                println("Failed to play album: ${e.message}")
            }
        }
    }

    // Home section configuration methods
    fun updateHomeSectionOrder(order: List<Int>) {
        homeSectionOrder = order
        prefs.put("HOME_SECTION_ORDER", order.joinToString(","))
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Home section toggle methods
    fun toggleShowMixedForYou(enabled: Boolean) {
        showMixedForYou = enabled
        prefs.putBoolean("SHOW_MIXED_FOR_YOU", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowListenAgain(enabled: Boolean) {
        showListenAgain = enabled
        prefs.putBoolean("SHOW_LISTEN_AGAIN", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowRecentlyPlayed(enabled: Boolean) {
        showRecentlyPlayed = enabled
        prefs.putBoolean("SHOW_RECENTLY_PLAYED", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowQuickPicks(enabled: Boolean) {
        showQuickPicks = enabled
        prefs.putBoolean("SHOW_QUICK_PICKS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowCharts(enabled: Boolean) {
        showCharts = enabled
        prefs.putBoolean("SHOW_CHARTS", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowNewReleases(enabled: Boolean) {
        showNewReleases = enabled
        prefs.putBoolean("SHOW_NEW_RELEASES", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowMadeForYou(enabled: Boolean) {
        showMadeForYou = enabled
        prefs.putBoolean("SHOW_MADE_FOR_YOU", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowSimilarTo(enabled: Boolean) {
        showSimilarTo = enabled
        prefs.putBoolean("SHOW_SIMILAR_TO", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowTrendingMusic(enabled: Boolean) {
        showTrendingMusic = enabled
        prefs.putBoolean("SHOW_TRENDING_MUSIC", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    // Playlist visibility toggle methods (for future library customization)
    fun toggleShowLikedPlaylist(enabled: Boolean) {
        showLikedPlaylist = enabled
        prefs.putBoolean("SHOW_LIKED_PLAYLIST", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowDownloadedPlaylist(enabled: Boolean) {
        showDownloadedPlaylist = enabled
        prefs.putBoolean("SHOW_DOWNLOADED_PLAYLIST", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowTopPlaylist(enabled: Boolean) {
        showTopPlaylist = enabled
        prefs.putBoolean("SHOW_TOP_PLAYLIST", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowUploadedPlaylist(enabled: Boolean) {
        showUploadedPlaylist = enabled
        prefs.putBoolean("SHOW_UPLOADED_PLAYLIST", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }

    fun toggleShowCachedPlaylist(enabled: Boolean) {
        showCachedPlaylist = enabled
        prefs.putBoolean("SHOW_CACHED_PLAYLIST", enabled)
        try { prefs.flush() } catch (_: Exception) {}
    }
}
