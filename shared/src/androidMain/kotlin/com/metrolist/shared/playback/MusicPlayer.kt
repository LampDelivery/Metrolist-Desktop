package com.metrolist.shared.playback

import com.metrolist.shared.model.SongItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class MusicPlayer actual constructor() {
    private val _currentSong = MutableStateFlow<SongItem?>(null)
    actual val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    actual val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    actual val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    actual val duration: StateFlow<Long> = _duration.asStateFlow()

    actual fun play(song: SongItem, url: String) {
        _currentSong.value = song
        _isPlaying.value = true
        // TODO: Implement ExoPlayer logic
    }

    actual fun pause() {
        _isPlaying.value = false
    }

    actual fun resume() {
        _isPlaying.value = true
    }

    actual fun stop() {
        _isPlaying.value = false
        _currentSong.value = null
    }

    actual fun seekTo(position: Long) {
        _currentPosition.value = position
    }

    actual fun setVolume(level: Int) {
        // TODO: Implement volume logic
    }

    actual fun setQueue(songs: List<SongItem>, startPlaying: Boolean) {
        // TODO: Implement queue logic
    }

    actual fun release() {
        stop()
    }
}
