package com.metrolist.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.shared.model.SongItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class PlaybackViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {
    val playerConnection = PlayerConnection(context, viewModelScope)

    val isPlaying: StateFlow<Boolean> = playerConnection.isPlaying
    val currentSong: StateFlow<SongItem?> = playerConnection.currentSong
    val currentPosition: StateFlow<Long> = playerConnection.currentPosition
    val duration: StateFlow<Long> = playerConnection.duration
    val progress: StateFlow<Float> = playerConnection.progress

    val canSkipPrevious: StateFlow<Boolean> = playerConnection.canSkipPrevious
    val canSkipNext: StateFlow<Boolean> = playerConnection.canSkipNext

    val repeatMode: StateFlow<Int> = playerConnection.repeatMode
    val shuffleModeEnabled: StateFlow<Boolean> = playerConnection.shuffleModeEnabled

    val playbackState: StateFlow<PlayerConnection.PlaybackState> = playerConnection.playbackState

    val hasMedia: StateFlow<Boolean> = currentSong
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun togglePlayPause() = playerConnection.togglePlayPause()
    fun seekTo(position: Long) = playerConnection.seekTo(position)
    fun skipToNext() = playerConnection.skipToNext()
    fun skipToPrevious() = playerConnection.skipToPrevious()
    fun setRepeatMode(mode: Int) = playerConnection.setRepeatMode(mode)
    fun toggleShuffle() = playerConnection.toggleShuffle()

    fun cycleRepeatMode() {
        val nextMode = when (repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else -> Player.REPEAT_MODE_OFF
        }
        setRepeatMode(nextMode)
    }

    override fun onCleared() {
        super.onCleared()
        playerConnection.release()
    }
}
