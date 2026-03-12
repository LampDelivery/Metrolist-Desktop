package com.metrolist.shared.playback

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

interface MpvLib : Library {
    fun mpv_create(): Pointer?
    fun mpv_initialize(handle: Pointer): Int
    fun mpv_command(handle: Pointer, args: Array<String?>): Int
    fun mpv_terminate_destroy(handle: Pointer)
    fun mpv_set_property_string(handle: Pointer, name: String, value: String): Int
    fun mpv_get_property_string(handle: Pointer, name: String): String?
    fun mpv_free(data: Pointer)
    
    companion object {
        private var loadedLib: MpvLib? = null

        fun load(): MpvLib? {
            if (loadedLib != null) return loadedLib
            
            val userDir = System.getProperty("user.dir")
            // When running from the packaged app, the libraries are usually in the same dir or 'app' dir
            val appDir = System.getProperty("compose.application.resources.dir")?.let { File(it) }
            
            val searchPaths = mutableListOf<String>()
            appDir?.let { searchPaths.add(it.absolutePath) }
            searchPaths.add(userDir)
            searchPaths.add(File(userDir, "app").absolutePath)
            
            // Helpful for development
            val osName = System.getProperty("os.name").lowercase()
            if (osName.contains("win")) {
                // Common Windows search path for dev
                searchPaths.add("C:\\mpv")
            }

            val combinedPath = searchPaths.joinToString(File.pathSeparator)
            System.setProperty("jna.library.path", combinedPath)
            
            val names = when {
                osName.contains("win") -> listOf("libmpv-2", "mpv-2", "mpv")
                osName.contains("mac") -> listOf("libmpv.2", "libmpv")
                else -> listOf("libmpv", "mpv")
            }
            
            for (name in names) {
                try {
                    val lib = Native.load(name, MpvLib::class.java) as MpvLib
                    loadedLib = lib
                    println("Successfully loaded mpv library: $name from paths: $combinedPath")
                    return lib
                } catch (e: UnsatisfiedLinkError) {
                    // silent
                }
            }
            System.err.println("Failed to load mpv library. Paths searched: $combinedPath")
            return null
        }
    }
}

class MpvPlayer {
    private var handle: Pointer? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    init {
        try {
            val lib = MpvLib.load()
            if (lib != null) {
                handle = lib.mpv_create()
                handle?.let {
                    lib.mpv_set_property_string(it, "vo", "null")
                    lib.mpv_set_property_string(it, "ytdl", "no")
                    lib.mpv_initialize(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(url: String) {
        val lib = MpvLib.load() ?: return
        val h = handle ?: return
        lib.mpv_command(h, arrayOf("loadfile", url, "replace"))
        _isPlaying.value = true
    }

    fun pause() {
        val h = handle ?: return
        val lib = MpvLib.load() ?: return
        lib.mpv_command(h, arrayOf("set", "pause", "yes"))
        _isPlaying.value = false
    }

    fun resume() {
        val h = handle ?: return
        val lib = MpvLib.load() ?: return
        lib.mpv_command(h, arrayOf("set", "pause", "no"))
        _isPlaying.value = true
    }

    fun stop() {
        val h = handle ?: return
        val lib = MpvLib.load() ?: return
        lib.mpv_command(h, arrayOf("stop"))
        _isPlaying.value = false
    }

    fun seekTo(seconds: Long) {
        val h = handle ?: return
        val lib = MpvLib.load() ?: return
        lib.mpv_command(h, arrayOf("seek", seconds.toString(), "absolute"))
    }

    fun setVolume(level: Int) {
        val h = handle ?: return
        val lib = MpvLib.load() ?: return
        lib.mpv_command(h, arrayOf("set", "volume", level.toString()))
    }

    fun getPosition(): Double {
        val h = handle ?: return 0.0
        val lib = MpvLib.load() ?: return 0.0
        return lib.mpv_get_property_string(h, "time-pos")?.toDoubleOrNull() ?: 0.0
    }

    fun getDuration(): Double {
        val h = handle ?: return 0.0
        val lib = MpvLib.load() ?: return 0.0
        return lib.mpv_get_property_string(h, "duration")?.toDoubleOrNull() ?: 0.0
    }

    fun release() {
        handle?.let {
            MpvLib.load()?.mpv_terminate_destroy(it)
            handle = null
        }
        scope.cancel()
    }
}
