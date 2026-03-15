package com.metrolist.desktop.utils

import dev.firstdark.rpc.DiscordRpc
import dev.firstdark.rpc.enums.ActivityType
import dev.firstdark.rpc.handlers.RPCEventHandler
import dev.firstdark.rpc.models.DiscordRichPresence
import dev.firstdark.rpc.models.User
import com.metrolist.desktop.state.AppState
import com.metrolist.shared.model.SongItem

object DiscordRPCManager {
    private const val DEFAULT_CLIENT_ID = "1481169442346893362"
    private var rpc = DiscordRpc()
    private var initialized = false
    private var currentClientId = ""

    private var lastSongId: String? = null
    private var lastIsPlaying: Boolean? = null
    private var lastUpdateMillis: Long = 0
    private var lastPositionSeconds: Long = 0
    private var lastDurationSeconds: Long = 0

    // Prevent hammering the IPC socket when Discord isn't running
    private var lastInitAttempt = 0L
    private const val INIT_RETRY_INTERVAL_MS = 30_000L

    private val handler = object : RPCEventHandler() {
        override fun ready(user: User) {
            println("Discord RPC ready: ${user.username}")
            initialized = true
        }
    }

    fun init() {
        val clientId = AppState.discordRpcAppId.ifBlank { DEFAULT_CLIENT_ID }
        if (initialized && currentClientId == clientId) return
        currentClientId = clientId
        try {
            rpc.init(clientId, handler, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reinit() {
        initialized = false
        currentClientId = ""
        lastSongId = null
        lastIsPlaying = null
        lastUpdateMillis = 0
        lastPositionSeconds = 0
        lastDurationSeconds = 0
        lastInitAttempt = 0L
        try { rpc.shutdown() } catch (_: Exception) {}
        try {
            rpc = DiscordRpc()
        } catch (_: Exception) {}
        init()
    }

    fun update(song: SongItem?, isPlaying: Boolean, currentPositionMs: Long, artistIconOverride: String? = null) {
        if (!initialized) {
            val now = System.currentTimeMillis()
            if (now - lastInitAttempt >= INIT_RETRY_INTERVAL_MS) {
                lastInitAttempt = now
                init()
            }
            if (!initialized) return  // Discord not running; skip until next retry window
        }

        if (song == null || (!isPlaying && !AppState.discordRpcShowIdle)) {
            clear()
            return
        }

        val now = System.currentTimeMillis()
        val currentPositionSeconds = currentPositionMs / 1000

        val songDuration = song.duration
        val durationSeconds: Long = if (songDuration != null && songDuration > 0) {
            songDuration
        } else {
            AppState.player.duration.value / 1000
        }

        val songChanged = song.id != lastSongId
        val playingChanged = isPlaying != lastIsPlaying
        val seeked = !songChanged && kotlin.math.abs(currentPositionSeconds - lastPositionSeconds) > 2
        val durationFetched = !songChanged && lastDurationSeconds == 0L && durationSeconds > 0L

        if (!songChanged && !playingChanged && !seeked && !durationFetched && (now - lastUpdateMillis < 15000)) {
            return
        }

        lastSongId = song.id
        lastIsPlaying = isPlaying
        lastUpdateMillis = now
        lastPositionSeconds = currentPositionSeconds
        lastDurationSeconds = durationSeconds

        try {
            val albumName = song.album?.name
            val artistName = song.artists.joinToString { it.name }
            val firstArtist = song.artists.firstOrNull()

            val actType = when (AppState.discordRpcActivityType) {
                "PLAYING" -> ActivityType.PLAYING
                "WATCHING" -> ActivityType.WATCHING
                "COMPETING" -> ActivityType.COMPETING
                else -> ActivityType.LISTENING
            }

            val builder = DiscordRichPresence.builder()
                .details(song.title)
                .state(artistName)
                .largeImageKey(song.thumbnail ?: "icon")
                .largeImageText(albumName ?: artistName)
                .activityType(actType)

            // Show artist portrait as small image; no fallback to album art while playing
            if (firstArtist != null) {
                val artistIconUrl = artistIconOverride ?: firstArtist.thumbnail
                if (isPlaying) {
                    if (artistIconUrl != null) {
                        builder.smallImageKey(artistIconUrl)
                        builder.smallImageText(firstArtist.name)
                    }
                    // no small image when artist icon is unavailable during playback
                } else {
                    builder.smallImageKey(artistIconUrl ?: "pause")
                    builder.smallImageText("⏸ ${firstArtist.name}")
                }
            } else if (!isPlaying) {
                builder.smallImageKey("pause")
                builder.smallImageText("Paused")
            }

            if (isPlaying) {
                val nowSeconds = now / 1000
                builder.startTimestamp(nowSeconds - currentPositionSeconds)
                if (durationSeconds > 0) {
                    builder.endTimestamp(nowSeconds - currentPositionSeconds + durationSeconds)
                }
            }

            if (AppState.discordRpcShowButtons) {
                val buttons = mutableListOf<DiscordRichPresence.RPCButton>()

                if (AppState.discordRpcButton1Visible) {
                    val label = AppState.discordRpcButton1Text
                        .replace("{song_name}", song.title)
                        .replace("{artist_name}", artistName)
                        .replace("{album_name}", albumName ?: "")
                    val url = AppState.discordRpcButton1Url
                        .replace("{video_id}", song.id)
                        .replace("{song_name}", song.title)
                        .replace("{artist_name}", artistName)
                        .replace("{album_name}", albumName ?: "")
                    if (url.isNotBlank()) {
                        buttons.add(DiscordRichPresence.RPCButton.of(label, url))
                    }
                }

                if (AppState.discordRpcButton2Visible) {
                    val label = AppState.discordRpcButton2Text
                        .replace("{song_name}", song.title)
                        .replace("{artist_name}", artistName)
                        .replace("{album_name}", albumName ?: "")
                    val url = AppState.discordRpcButton2Url
                        .replace("{video_id}", song.id)
                        .replace("{song_name}", song.title)
                        .replace("{artist_name}", artistName)
                        .replace("{album_name}", albumName ?: "")
                    if (url.isNotBlank()) {
                        buttons.add(DiscordRichPresence.RPCButton.of(label, url))
                    }
                }

                if (buttons.isNotEmpty()) {
                    builder.buttons(buttons)
                }
            }

            rpc.updatePresence(builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clear() {
        if (!initialized) return
        lastSongId = null
        lastIsPlaying = null
        lastPositionSeconds = 0
        lastDurationSeconds = 0
        try {
            rpc.updatePresence(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
