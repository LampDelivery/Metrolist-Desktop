package com.metrolist.desktop.utils

import com.metrolist.desktop.state.AppState
import javax.swing.SwingUtilities
import java.awt.*

/**
 * Windows Media Transport Controls Manager
 * Simple, reliable approach for Windows media integration
 */
object WindowsMediaTransportControls {
    private var window: Window? = null
    private var isInitialized = false
    private val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)

    /**
     * Initialize Windows media transport controls
     */
    fun initialize(mainWindow: Window) {
        if (!isWindows) {
            println("Windows Media Transport Controls not supported on this platform")
            return
        }

        try {
            window = mainWindow

            // Set basic App User Model ID via system property (simpler approach)
            System.setProperty("java.awt.appname", "Metrolist")

            // Try to set App User Model ID via JNA if available
            try {
                setAppUserModelIdViaNative()
            } catch (e: Exception) {
                println("Could not set native App User Model ID: ${e.message}")
                // Continue with basic approach
            }

            isInitialized = true
            println("Windows Media Transport Controls initialized successfully")

            // Initial state update
            updateMediaState()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to initialize Windows Media Transport Controls: ${e.message}")
        }
    }

    /**
     * Set App User Model ID using native approach
     */
    private fun setAppUserModelIdViaNative() {
        // This is a simplified approach - in production you'd want to use JNA
        // For now, we'll rely on window title and system properties
        val appId = "com.metrolist.desktop"
        System.setProperty("windows.appusermodel.id", appId)
        println("Set App User Model ID (via system property): $appId")
    }

    /**
     * Update Windows media session with current track info
     */
    fun updateMediaState() {
        if (!isWindows || !isInitialized) return

        SwingUtilities.invokeLater {
            try {
                updateWindowTitle()
            } catch (e: Exception) {
                println("Error updating media state: ${e.message}")
            }
        }
    }

    /**
     * Update window title for Windows media detection
     */
    private fun updateWindowTitle() {
        window?.let { win ->
            if (win is Frame) {
                val currentTrack = AppState.currentTrack
                val isPlaying = AppState.isPlaying

                val title = when {
                    currentTrack != null && isPlaying -> {
                        val artist = currentTrack.artists.joinToString { it.name }
                        "${currentTrack.title} - $artist - Metrolist"
                    }
                    currentTrack != null -> {
                        val artist = currentTrack.artists.joinToString { it.name }
                        "⏸ ${currentTrack.title} - $artist - Metrolist"
                    }
                    else -> "Metrolist"
                }

                win.title = title

                // Also set system properties that media apps can read
                if (currentTrack != null) {
                    System.setProperty("media.title", currentTrack.title)
                    System.setProperty("media.artist", currentTrack.artists.joinToString { it.name })
                    System.setProperty("media.album", currentTrack.album?.name ?: "")
                    System.setProperty("media.status", if (isPlaying) "Playing" else "Paused")
                } else {
                    System.clearProperty("media.title")
                    System.clearProperty("media.artist")
                    System.clearProperty("media.album")
                    System.clearProperty("media.status")
                }
            }
        }
    }

    /**
     * Cleanup Windows media transport controls
     */
    fun cleanup() {
        try {
            // Clear system properties
            System.clearProperty("media.title")
            System.clearProperty("media.artist")
            System.clearProperty("media.album")
            System.clearProperty("media.status")

            // Reset window title
            SwingUtilities.invokeLater {
                window?.let { win ->
                    if (win is Frame) {
                        win.title = "Metrolist"
                    }
                }
            }

            isInitialized = false
            window = null
            println("Windows Media Transport Controls cleaned up")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}