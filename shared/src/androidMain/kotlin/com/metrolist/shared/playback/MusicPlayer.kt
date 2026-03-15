package com.metrolist.shared.playback

import com.metrolist.shared.model.SongItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _onEOF = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    actual val onEOF: SharedFlow<Unit> = _onEOF.asSharedFlow()

    actual fun play(song: SongItem, url: String) {
        _currentSong.value = song
        _isPlaying.value = true
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
    }

    actual fun setQueue(songs: List<SongItem>, startPlaying: Boolean) {
    }

    actual fun release() {
        stop()
    }
}
