@file:OptIn(InternalCoroutinesApi::class)
@file:Suppress("FunctionName")
package com.metrolist.shared.playback

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

interface MpvLib : Library {
    fun mpv_create(): Pointer?
    fun mpv_initialize(handle: Pointer): Int
    fun mpv_command(handle: Pointer, args: Array<String?>): Int
    fun mpv_terminate_destroy(handle: Pointer)
    fun mpv_set_property_string(handle: Pointer, name: String, value: String): Int
    fun mpv_get_property_string(handle: Pointer, name: String): Pointer?
    fun mpv_free(data: Pointer)
    
    companion object {
        private var loadedLib: MpvLib? = null

        fun load(): MpvLib? {
            val currentLib = loadedLib
            if (currentLib != null) return currentLib
            
            val userDir = System.getProperty("user.dir")
            val appDir = System.getProperty("compose.application.resources.dir")?.let { File(it) }
            
            val searchPaths = mutableListOf<String>()
            
            appDir?.let { searchPaths.add(it.absolutePath) }
            searchPaths.add(File(userDir, "external").absolutePath)
            searchPaths.add(userDir)
            searchPaths.add(File(userDir, "app").absolutePath)
            
            val osName = System.getProperty("os.name").lowercase()
            if (osName.contains("win")) {
                searchPaths.add("C:\\mpv")
            } else if (osName.contains("linux")) {
                searchPaths.add("/usr/lib/x86_64-linux-gnu")
                searchPaths.add("/usr/local/lib")
            }

            val combinedPath = searchPaths.joinToString(File.pathSeparator)
            System.setProperty("jna.library.path", combinedPath)
            
            val names = when {
                osName.contains("win") -> listOf("libmpv-2", "mpv-2", "mpv")
                osName.contains("mac") -> listOf("libmpv.2", "libmpv", "mpv")
                osName.contains("linux") -> listOf("mpv", "mpv.so.2", "mpv.so.1")
                else -> listOf("mpv")
            }
            
            for (name in names) {
                try {
                    val lib = Native.load(name, MpvLib::class.java) as MpvLib
                    loadedLib = lib
                    return lib
                } catch (_: UnsatisfiedLinkError) {
                    // silent
                }
            }
            return null
        }
    }
}

class MpvPlayer {
    private var handle: Pointer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _onEOF = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onEOF = _onEOF.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        try {
            val lib = MpvLib.load()
            if (lib != null) {
                handle = lib.mpv_create()
                handle?.let {
                    lib.mpv_set_property_string(it, "vo", "null")
                    lib.mpv_set_property_string(it, "ytdl", "no")
                    lib.mpv_set_property_string(it, "keep-open", "no")
                    lib.mpv_initialize(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // State Polling
        scope.launch {
            while (isActive) {
                val idle = getPropertyString("idle-active") == "yes"
                val paused = getPropertyString("pause") == "yes"
                
                val wasPlaying = _isPlaying.value
                val currentlyPlaying = !idle && !paused
                
                if (wasPlaying && idle) {
                    _onEOF.tryEmit(Unit)
                }
                
                _isPlaying.value = currentlyPlaying
                delay(500)
            }
        }
    }

    private fun getPropertyString(name: String): String? {
        val h = handle ?: return null
        val lib = MpvLib.load() ?: return null
        val ptr = lib.mpv_get_property_string(h, name) ?: return null
        return try {
            ptr.getString(0)
        } finally {
            lib.mpv_free(ptr)
        }
    }

    fun play(url: String) {
        println("[MpvPlayer] play() called with url: $url")
        val lib = MpvLib.load()
        if (lib == null) {
            println("[MpvPlayer] ERROR: MpvLib not loaded")
            return
        }
        val h = handle
        if (h == null) {
            println("[MpvPlayer] ERROR: handle is null")
            return
        }
        val result = lib.mpv_command(h, arrayOf("loadfile", url, "replace"))
        if (result != 0) {
            println("[MpvPlayer] ERROR: mpv_command failed with code $result")
        }
        _isPlaying.value = true
    }

    fun pause() {
        println("[MpvPlayer] pause() called")
        val h = handle
        if (h == null) {
            println("[MpvPlayer] ERROR: handle is null")
            return
        }
        val lib = MpvLib.load()
        if (lib == null) {
            println("[MpvPlayer] ERROR: MpvLib not loaded")
            return
        }
        lib.mpv_command(h, arrayOf("set", "pause", "yes"))
        _isPlaying.value = false
    }

    fun resume() {
        println("[MpvPlayer] resume() called")
        val h = handle
        if (h == null) {
            println("[MpvPlayer] ERROR: handle is null")
            return
        }
        val lib = MpvLib.load()
        if (lib == null) {
            println("[MpvPlayer] ERROR: MpvLib not loaded")
            return
        }
        lib.mpv_command(h, arrayOf("set", "pause", "no"))
        _isPlaying.value = true
    }

    fun stop() {
        println("[MpvPlayer] stop() called")
        val h = handle
        if (h == null) {
            println("[MpvPlayer] ERROR: handle is null")
            return
        }
        val lib = MpvLib.load()
        if (lib == null) {
            println("[MpvPlayer] ERROR: MpvLib not loaded")
            return
        }
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

    fun getPosition(): Double = getPropertyString("time-pos")?.toDoubleOrNull() ?: 0.0

    fun getDuration(): Double = getPropertyString("duration")?.toDoubleOrNull() ?: 0.0

    fun release() {
        scope.cancel()
        handle?.let {
            MpvLib.load()?.mpv_terminate_destroy(it)
            handle = null
        }
    }
}
