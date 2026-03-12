package com.metrolist.desktop.utils

import dev.firstdark.rpc.DiscordRpc
import dev.firstdark.rpc.enums.ActivityType
import dev.firstdark.rpc.handlers.RPCEventHandler
import dev.firstdark.rpc.models.DiscordRichPresence
import dev.firstdark.rpc.models.User
import com.metrolist.desktop.state.AppState
import com.metrolist.shared.model.SongItem

object DiscordRPCManager {
    private const val CLIENT_ID = "1481169442346893362"
    private val rpc = DiscordRpc()
    private var initialized = false
    
    private var lastSongId: String? = null
    private var lastIsPlaying: Boolean? = null
    private var lastUpdateMillis: Long = 0
    private var lastPositionSeconds: Long = 0
    private var lastDurationSeconds: Long = 0

    private val handler = object : RPCEventHandler() {
        override fun ready(user: User) {
            println("Discord RPC ready: ${user.username}")
            initialized = true
        }
    }

    fun init() {
        if (initialized) return
        try {
            rpc.init(CLIENT_ID, handler, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun update(song: SongItem?, isPlaying: Boolean, currentPositionMs: Long) {
        if (!initialized) init()
        
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
        
        // Logic from pear desktop
        val songChanged = song.id != lastSongId
        val playingChanged = isPlaying != lastIsPlaying
        val seeked = !songChanged && kotlin.math.abs(currentPositionSeconds - lastPositionSeconds) > 2
        val durationFetched = !songChanged && lastDurationSeconds == 0L && durationSeconds > 0L
        
        // updates (Discord limit is ~15s) unless state changed
        if (!songChanged && !playingChanged && !seeked && !durationFetched && (now - lastUpdateMillis < 15000)) {
            return
        }

        lastSongId = song.id
        lastIsPlaying = isPlaying
        lastUpdateMillis = now
        lastPositionSeconds = currentPositionSeconds
        lastDurationSeconds = durationSeconds

        try {
            val albumName = song.album?.name ?: "Metrolist"
            
            val builder = DiscordRichPresence.builder()
                .details(if (AppState.discordRpcUseDetails) song.title else song.artists.joinToString { it.name })
                .state(if (AppState.discordRpcUseDetails) song.artists.joinToString { it.name } else song.title)
                .largeImageKey(song.thumbnail ?: "icon")
                .largeImageText(albumName)
                .activityType(ActivityType.LISTENING)

            if (isPlaying) {
                val nowSeconds = now / 1000
                builder.startTimestamp(nowSeconds - currentPositionSeconds)
                if (durationSeconds > 0) {
                    builder.endTimestamp(nowSeconds - currentPositionSeconds + durationSeconds)
                }
            } else {
                builder.smallImageKey("pause")
                builder.smallImageText("Paused")
            }
                
            if (AppState.discordRpcShowButtons) {
                builder.button(DiscordRichPresence.RPCButton.of(AppState.discordRpcButton1Text, "https://music.youtube.com/watch?v=${song.id}"))
                builder.button(DiscordRichPresence.RPCButton.of(AppState.discordRpcButton2Text, "https://github.com/MetrolistGroup/Metrolist"))
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
