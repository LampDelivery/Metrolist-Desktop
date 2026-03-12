package com.metrolist.desktop.playback

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Platform
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * JNA Interface for libmpv
 */
interface MpvLib : Library {
    fun mpv_create(): Pointer?
    fun mpv_initialize(handle: Pointer): Int
    fun mpv_command(handle: Pointer, args: Array<String?>): Int
    fun mpv_terminate_destroy(handle: Pointer)
    fun mpv_set_property_string(handle: Pointer, name: String, value: String): Int
    fun mpv_get_property_string(handle: Pointer, name: String): String?
    fun mpv_free(data: Pointer)
    
    companion object {
        val LIB_NAME = if (Platform.isWindows()) "libmpv-2" else "mpv"
        val INSTANCE: MpvLib = Native.load(LIB_NAME, MpvLib::class.java) as MpvLib
    }
}

class MpvPlayer {
    private var handle: Pointer? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    init {
        try {
            handle = MpvLib.INSTANCE.mpv_create()
            handle?.let {
                // Set some basic options
                MpvLib.INSTANCE.mpv_set_property_string(it, "vo", "null") // Audio only for now
                MpvLib.INSTANCE.mpv_set_property_string(it, "ytdl", "no") // We fetch URLs ourselves
                MpvLib.INSTANCE.mpv_initialize(it)
            }
        } catch (e: Exception) {
            println("Failed to initialize MPV: ${e.message}")
        }
    }

    fun play(url: String) {
        handle?.let {
            MpvLib.INSTANCE.mpv_command(it, arrayOf("loadfile", url, "replace"))
            _isPlaying.value = true
        }
    }

    fun pause() {
        handle?.let {
            MpvLib.INSTANCE.mpv_command(it, arrayOf("set", "pause", "yes"))
            _isPlaying.value = false
        }
    }

    fun resume() {
        handle?.let {
            MpvLib.INSTANCE.mpv_command(it, arrayOf("set", "pause", "no"))
            _isPlaying.value = true
        }
    }

    fun stop() {
        handle?.let {
            MpvLib.INSTANCE.mpv_command(it, arrayOf("stop"))
            _isPlaying.value = false
        }
    }

    fun setVolume(level: Int) {
        handle?.let {
            MpvLib.INSTANCE.mpv_command(it, arrayOf("set", "volume", level.toString()))
        }
    }

    fun release() {
        handle?.let {
            MpvLib.INSTANCE.mpv_terminate_destroy(it)
            handle = null
        }
        scope.cancel()
    }
}
