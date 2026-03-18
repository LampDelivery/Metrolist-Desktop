package com.metrolist.desktop.utils

import com.metrolist.desktop.state.AppState
import com.metrolist.shared.api.lastfm.LastFM
import com.metrolist.shared.model.SongItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class ScrobbleManager(
    private val scope: CoroutineScope,
    var minSongDuration: Int = 30,
    var scrobbleDelayPercent: Float = 0.5f,
    var scrobbleDelaySeconds: Int = 180
) {
    private var scrobbleJob: Job? = null
    private var scrobbleRemainingMillis: Long = 0L
    private var scrobbleTimerStartedAt: Long = 0L
    private var songStartedAt: Long = 0L
    private var songStarted = false
    var useNowPlaying = true

    fun onSongStart(song: SongItem?) {
        if (song == null) return
        songStartedAt = System.currentTimeMillis()
        songStarted = true
        startScrobbleTimer(song)
        if (useNowPlaying && AppState.prefs.getBoolean("LASTFM_ENABLED", true) && LastFM.sessionKey != null) {
            updateNowPlaying(song)
        }
    }

    fun onSongResume(song: SongItem) {
        resumeScrobbleTimer(song)
    }

    fun onSongPause() {
        pauseScrobbleTimer()
    }

    fun onSongStop() {
        stopScrobbleTimer()
        songStarted = false
    }

    private fun startScrobbleTimer(song: SongItem) {
        scrobbleJob?.cancel()
        val duration = song.duration ?: 0L

        if (duration <= minSongDuration) return

        val threshold = duration * 1000L * scrobbleDelayPercent
        scrobbleRemainingMillis = min(threshold.toLong(), scrobbleDelaySeconds * 1000L)

        if (scrobbleRemainingMillis <= 0) {
            scrobbleSong(song)
            return
        }
        scrobbleTimerStartedAt = System.currentTimeMillis()
        scrobbleJob = scope.launch {
            delay(scrobbleRemainingMillis)
            scrobbleSong(song)
            scrobbleJob = null
        }
    }

    private fun pauseScrobbleTimer() {
        scrobbleJob?.cancel()
        if (scrobbleTimerStartedAt != 0L) {
            val elapsed = System.currentTimeMillis() - scrobbleTimerStartedAt
            scrobbleRemainingMillis -= elapsed
            if (scrobbleRemainingMillis < 0) scrobbleRemainingMillis = 0
            scrobbleTimerStartedAt = 0L
        }
    }

    private fun resumeScrobbleTimer(song: SongItem) {
        if (scrobbleRemainingMillis <= 0) return
        scrobbleJob?.cancel()
        scrobbleTimerStartedAt = System.currentTimeMillis()
        scrobbleJob = scope.launch {
            delay(scrobbleRemainingMillis)
            scrobbleSong(song)
            scrobbleJob = null
        }
    }

    private fun stopScrobbleTimer() {
        scrobbleJob?.cancel()
        scrobbleJob = null
        scrobbleRemainingMillis = 0
    }

    private fun scrobbleSong(song: SongItem) {
        if (!AppState.prefs.getBoolean("LASTFM_ENABLED", true) || LastFM.sessionKey == null) return
        
        scope.launch(Dispatchers.IO) {
            LastFM.scrobble(
                httpClient = AppState.client,
                artist = song.artists.joinToString { it.name },
                track = song.title,
                timestamp = songStartedAt,
                album = song.album?.name ?: song.title,
            )
        }
    }

    private fun updateNowPlaying(song: SongItem) {
        if (!AppState.prefs.getBoolean("LASTFM_ENABLED", true) || LastFM.sessionKey == null) return

        scope.launch(Dispatchers.IO) {
            LastFM.updateNowPlaying(
                httpClient = AppState.client,
                artist = song.artists.joinToString { it.name },
                track = song.title,
                album = song.album?.name ?: song.title
            )
        }
    }

    fun onPlayerStateChanged(isPlaying: Boolean, song: SongItem?) {
        if (song == null) {
            onSongStop()
            return
        }
        if (isPlaying) {
            if (!songStarted) {
                onSongStart(song)
            } else {
                onSongResume(song)
            }
        } else {
            onSongPause()
        }
    }
}
