package com.metrolist.shared.playback

import com.metrolist.shared.model.SongItem
import kotlinx.coroutines.flow.StateFlow

expect class MusicPlayer() {
    fun play(song: SongItem, url: String)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun setVolume(level: Int)
    fun setQueue(songs: List<SongItem>, startPlaying: Boolean = false)

    val currentSong: StateFlow<SongItem?>
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
}
