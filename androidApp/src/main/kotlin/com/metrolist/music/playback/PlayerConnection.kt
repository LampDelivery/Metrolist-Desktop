package com.metrolist.music.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.playback.MusicQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@UnstableApi
class PlayerConnection(
    private val context: Context,
    private val scope: CoroutineScope,
) : ServiceConnection {
    private var service: MusicService? = null
    private var binder: MusicService.MusicBinder? = null
    private var isBound = false

    val isConnected = MutableStateFlow(false)

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    val currentSong: StateFlow<SongItem?> = _currentSong

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    val progress: StateFlow<Float> = combine(currentPosition, duration) { pos, dur ->
        if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
    }.stateIn(scope, SharingStarted.Lazily, 0f)

    val playbackState = MutableStateFlow(PlaybackState.IDLE)

    // Queue state
    val queue = MusicQueue()
    val queueTitle = MutableStateFlow<String?>(null)
    val currentMediaItemIndex = MutableStateFlow(-1)

    val canSkipPrevious = MutableStateFlow(false)
    val canSkipNext = MutableStateFlow(true)

    val repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val shuffleModeEnabled = MutableStateFlow(false)

    val volume = MutableStateFlow(1f)

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(context, MusicService::class.java)
        context.startService(intent)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        binder = service as MusicService.MusicBinder
        this.service = binder?.getService()
        isConnected.value = true
        isBound = true

        setupPlayerListeners()
        startCollectingPlayerState()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
        binder = null
        isConnected.value = false
        isBound = false
    }

    private fun startCollectingPlayerState() {
        val player = service?.player ?: return
        
        scope.launch {
            player.isPlaying.collect { _isPlaying.value = it }
        }
        scope.launch {
            player.currentSong.collect { _currentSong.value = it }
        }
        scope.launch {
            player.currentPosition.collect { _currentPosition.value = it }
        }
        scope.launch {
            player.duration.collect { _duration.value = it }
        }
    }

    private fun setupPlayerListeners() {
        val player = service?.player?.exoPlayerInstance ?: return

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentMediaItemIndex.value = player.currentMediaItemIndex
                updateCanSkip()
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                currentMediaItemIndex.value = player.currentMediaItemIndex
                updateCanSkip()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                this@PlayerConnection.repeatMode.value = repeatMode
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                this@PlayerConnection.shuffleModeEnabled.value = shuffleModeEnabled
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                this@PlayerConnection.playbackState.value = when (playbackState) {
                    Player.STATE_IDLE -> PlaybackState.IDLE
                    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                    Player.STATE_READY -> PlaybackState.READY
                    Player.STATE_ENDED -> PlaybackState.ENDED
                    else -> PlaybackState.IDLE
                }
            }
        })
    }

    private fun updateCanSkip() {
        val player = service?.player?.exoPlayerInstance ?: return
        canSkipPrevious.value = player.hasPreviousMediaItem()
        canSkipNext.value = player.hasNextMediaItem() || repeatMode.value != Player.REPEAT_MODE_OFF
    }

    // Playback controls
    fun playSong(song: SongItem, url: String) {
        service?.playSong(song, url)
    }

    fun playQueue(songs: List<SongItem>, startIndex: Int = 0) {
        service?.playQueue(songs, startIndex)
    }

    fun togglePlayPause() {
        val player = service?.player ?: return
        if (player.isPlaying.value) {
            player.pause()
        } else {
            player.resume()
        }
    }

    fun seekTo(position: Long) {
        service?.player?.seekTo(position)
    }

    fun skipToNext() {
        service?.player?.skipToNext()
    }

    fun skipToPrevious() {
        service?.player?.skipToPrevious()
    }

    fun setRepeatMode(mode: Int) {
        service?.player?.setRepeatMode(mode)
    }

    fun cycleRepeatMode() {
        val nextMode = when (repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }
        setRepeatMode(nextMode)
    }

    fun toggleShuffle() {
        val player = service?.player ?: return
        player.setShuffleModeEnabled(!shuffleModeEnabled.value)
    }

    fun setVolume(volume: Float) {
        service?.player?.setVolume((volume * 100).toInt())
        this.volume.value = volume
    }

    fun release() {
        if (isBound) {
            context.unbindService(this)
            isBound = false
        }
    }

    enum class PlaybackState {
        IDLE,
        BUFFERING,
        READY,
        ENDED
    }
}

val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { null }
