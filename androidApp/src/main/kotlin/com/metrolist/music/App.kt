/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.allowHardware
import coil3.request.crossfade
import com.metrolist.music.di.ApplicationScope
import com.metrolist.music.utils.CrashHandler
import com.metrolist.music.utils.dataStore
import com.metrolist.shared.api.innertube.models.YouTubeLocale
import com.metrolist.shared.constants.*
import com.metrolist.shared.state.GlobalYouTube
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), SingletonImageLoader.Factory {
    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        // Install crash handler first
        CrashHandler.install(this)

        Timber.plant(Timber.DebugTree())

        applicationScope.launch {
            initializeSettings()
            observeSettingsChanges()
        }
    }

    private suspend fun initializeSettings() {
        val settings = dataStore.data.first()
        val locale = Locale.getDefault()

        // Set initial YouTube state using the shared GlobalYouTube object
        GlobalYouTube.updateLocale(
            YouTubeLocale(
                gl = settings[ContentCountryKey] ?: locale.country.ifBlank { "US" },
                hl = settings[ContentLanguageKey] ?: locale.language.ifBlank { "en" }
            )
        )

        val channel = NotificationChannel(
            "updates",
            "Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun observeSettingsChanges() {
        applicationScope.launch(Dispatchers.IO) {
            dataStore.data
                .map { it[InnerTubeCookieKey] }
                .distinctUntilChanged()
                .collect { cookie ->
                    GlobalYouTube.updateCookie(cookie)
                }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(this)
            .apply {
                crossfade(true)
                allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(context, 0.25)
                        .build()
                }
                diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("coil"))
                        .maxSizeBytes(512 * 1024 * 1024L)
                        .build()
                }
            }.build()
    }
}
