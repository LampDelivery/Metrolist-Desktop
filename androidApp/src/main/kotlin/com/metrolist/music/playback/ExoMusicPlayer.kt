package com.metrolist.music.playback

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.metrolist.shared.model.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class ExoMusicPlayer {
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _onEOF = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onEOF: SharedFlow<Unit> = _onEOF.asSharedFlow()

    private var currentUrl: String? = null
    
    // Store song mapping for queue playback
    private var songQueue: List<SongItem> = emptyList()

    fun initialize(context: Context) {
        if (exoPlayer != null) return

        val okHttpDataSourceFactory = OkHttpDataSource.Factory(
            okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        )

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setMediaSourceFactory(DefaultMediaSourceFactory(okHttpDataSourceFactory))
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_ENDED -> {
                                _onEOF.tryEmit(Unit)
                            }
                            Player.STATE_READY -> {
                                _duration.value = duration.coerceAtLeast(0L)
                            }
                            else -> {}
                        }
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        _currentPosition.value = newPosition.positionMs.coerceAtLeast(0L)
                    }
                })
            }

        // Start position polling
        scope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.duration > 0) {
                        _duration.value = player.duration.coerceAtLeast(0L)
                    }
                    _currentPosition.value = player.currentPosition.coerceAtLeast(0L)
                }
                delay(100)
            }
        }
    }

    fun play(song: SongItem, url: String) {
        currentUrl = url
        _currentSong.value = song
        songQueue = listOf(song)
        
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(url)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artists.joinToString(", ") { it.name })
                        .setAlbumTitle(song.album?.name)
                        .setArtworkUri(song.thumbnail?.let { android.net.Uri.parse(it) })
                        .build()
                )
                .build()
            
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }
    
    fun playSongs(songs: List<SongItem>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        songQueue = songs
        _currentSong.value = songs.getOrNull(startIndex)
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun stop() {
        exoPlayer?.stop()
        _currentSong.value = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position.coerceAtLeast(0L))
        _currentPosition.value = position.coerceAtLeast(0L)
    }

    fun setVolume(level: Int) {
        exoPlayer?.volume = level / 100f
    }

    fun setQueue(songs: List<SongItem>, startPlaying: Boolean) {
        // Queue management is handled by MusicQueue class
    }

    fun release() {
        scope.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }

    // Additional methods for queue support
    fun playQueueItems(items: List<MediaItem>, songs: List<SongItem>, startIndex: Int = 0, startPositionMs: Long = 0) {
        songQueue = songs
        _currentSong.value = songs.getOrNull(startIndex)
        
        exoPlayer?.let { player ->
            player.setMediaItems(items, startIndex, startPositionMs)
            player.prepare()
            player.play()
            
            // Add listener to update current song when transitioning
            player.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val index = player.currentMediaItemIndex
                    _currentSong.value = songQueue.getOrNull(index)
                }
            })
        }
    }

    fun addToQueue(item: MediaItem) {
        exoPlayer?.addMediaItem(item)
    }

    fun addToQueue(items: List<MediaItem>) {
        exoPlayer?.addMediaItems(items)
    }

    fun clearQueue() {
        exoPlayer?.clearMediaItems()
    }

    fun skipToNext() {
        exoPlayer?.seekToNext()
    }

    fun skipToPrevious() {
        exoPlayer?.seekToPrevious()
    }

    fun skipToQueueItem(index: Int) {
        exoPlayer?.seekToDefaultPosition(index)
    }

    val currentMediaItemIndex: Int
        get() = exoPlayer?.currentMediaItemIndex ?: -1

    val mediaItemCount: Int
        get() = exoPlayer?.mediaItemCount ?: 0

    fun getMediaItemAt(index: Int): MediaItem? {
        return try {
            exoPlayer?.getMediaItemAt(index)
        } catch (_: Exception) {
            null
        }
    }

    fun setRepeatMode(repeatMode: Int) {
        exoPlayer?.repeatMode = repeatMode
    }

    fun setShuffleModeEnabled(enabled: Boolean) {
        exoPlayer?.shuffleModeEnabled = enabled
    }

    val exoPlayerInstance: ExoPlayer?
        get() = exoPlayer
}
