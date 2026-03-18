@file:Suppress("UNUSED_PARAMETER")
package com.metrolist.shared.playback

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

actual class MusicPlayer actual constructor() {
    private val mpvPlayer = MpvPlayer()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _currentSong = MutableStateFlow<SongItem?>(null)
    actual val currentSong: StateFlow<SongItem?> = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    actual val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    actual val duration: StateFlow<Long> = _duration.asStateFlow()

    actual val isPlaying: StateFlow<Boolean> = mpvPlayer.isPlaying

    private val _onEOF = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    actual val onEOF: SharedFlow<Unit> = _onEOF.asSharedFlow()

    init {
        // Position and Duration Polling
        scope.launch {
            while (isActive) {
                // Update position even if paused to catch seek changes
                val pos = mpvPlayer.getPosition() // returns seconds as Double
                _currentPosition.value = (pos * 1000.0).toLong()
                
                val dur = mpvPlayer.getDuration() // returns seconds as Double
                if (dur > 0) {
                    _duration.value = (dur * 1000.0).toLong()
                }
                
                delay(50) // Poll faster for smoother UI progress
            }
        }

        scope.launch {
            mpvPlayer.onEOF.collect {
                _onEOF.emit(Unit)
            }
        }
    }

    actual fun play(song: SongItem, url: String) {
        _currentSong.value = song
        mpvPlayer.play(url)
    }

    actual fun pause() {
        mpvPlayer.pause()
    }

    actual fun resume() {
        mpvPlayer.resume()
    }

    actual fun stop() {
        mpvPlayer.stop()
    }

    actual fun seekTo(position: Long) {
        mpvPlayer.seekTo(position / 1000L)
    }

    actual fun setVolume(level: Int) {
        mpvPlayer.setVolume(level)
    }

    actual fun setQueue(songs: List<SongItem>, startPlaying: Boolean) {
        // Required by expect declaration
    }

    actual fun release() {
        mpvPlayer.release()
        scope.cancel()
    }
}
