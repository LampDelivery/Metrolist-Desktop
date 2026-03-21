package com.metrolist.music.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.metrolist.android.MainActivity
import com.metrolist.music.R
import com.metrolist.shared.model.SongItem
import com.metrolist.shared.state.GlobalYouTubeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class MusicService : Service() {
    private val binder = MusicBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    lateinit var player: ExoMusicPlayer
        private set
    private var mediaSession: MediaSession? = null
    private val notificationId = 1
    private val channelId = "music_playback"

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoMusicPlayer()
        player.initialize(this)
        createNotificationChannel()
        setupMediaSession()
        startPlaybackObserver()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> player.resume()
            ACTION_PAUSE -> player.pause()
            ACTION_NEXT -> player.skipToNext()
            ACTION_PREVIOUS -> player.skipToPrevious()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows currently playing music"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMediaSession() {
        val exoPlayer = player.exoPlayerInstance ?: return
        
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(object : MediaSession.Callback {
                // Handle media button events
            })
            .build()
    }

    private fun startPlaybackObserver() {
        serviceScope.launch {
            player.isPlaying.collectLatest { isPlaying ->
                if (isPlaying) {
                    startForeground(notificationId, buildNotification())
                } else {
                    stopForeground(STOP_FOREGROUND_DETACH)
                    updateNotification()
                }
            }
        }

        serviceScope.launch {
            player.currentSong.collectLatest { song ->
                song?.let {
                    updateNotification()
                }
            }
        }
    }

    private fun buildNotification(): Notification {
        val song = player.currentSong.value
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (player.isPlaying.value) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pause",
                createServiceActionIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "Play",
                createServiceActionIntent(ACTION_PLAY)
            )
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(song?.title ?: "Unknown")
            .setContentText(song?.artists?.joinToString(", ") { it.name } ?: "Unknown Artist")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .setOngoing(player.isPlaying.value)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_skip_previous,
                    "Previous",
                    createServiceActionIntent(ACTION_PREVIOUS)
                )
            )
            .addAction(playPauseAction)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_skip_next,
                    "Next",
                    createServiceActionIntent(ACTION_NEXT)
                )
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(song?.artists?.joinToString(", ") { it.name } ?: "Unknown Artist")
            )
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, buildNotification())
    }

    private fun createServiceActionIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        mediaSession?.release()
        player.release()
    }

    // Public methods for playback control
    fun playSong(song: SongItem, url: String) {
        if (url.isBlank()) {
            // Fetch URL if not provided
            serviceScope.launch {
                val streamUrl = fetchStreamUrl(song.id)
                if (streamUrl.isNotBlank()) {
                    player.play(song, streamUrl)
                }
            }
        } else {
            player.play(song, url)
        }
    }

    fun playQueue(songs: List<SongItem>, startIndex: Int = 0) {
        serviceScope.launch {
            val mediaItems = mutableListOf<MediaItem>()
            val validSongs = mutableListOf<SongItem>()
            
            // Fetch URLs for all songs
            songs.forEach { song ->
                val streamUrl = fetchStreamUrl(song.id)
                if (streamUrl.isNotBlank()) {
                    validSongs.add(song)
                    mediaItems.add(
                        MediaItem.Builder()
                            .setMediaId(song.id)
                            .setUri(streamUrl)
                            .setMediaMetadata(
                                androidx.media3.common.MediaMetadata.Builder()
                                    .setTitle(song.title)
                                    .setArtist(song.artists.joinToString(", ") { it.name })
                                    .setAlbumTitle(song.album?.name)
                                    .setArtworkUri(song.thumbnail?.let { android.net.Uri.parse(it) })
                                    .build()
                            )
                            .build()
                    )
                }
            }
            
            if (mediaItems.isNotEmpty()) {
                val validStartIndex = startIndex.coerceIn(0, mediaItems.size - 1)
                player.playQueueItems(mediaItems, validSongs, validStartIndex)
            }
        }
    }

    private suspend fun fetchStreamUrl(videoId: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            // Use the YouTubeRepository to get the stream URL
            val response = GlobalYouTubeRepository.instance.getStreamUrl(videoId)
            response ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    companion object {
        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
    }
}
