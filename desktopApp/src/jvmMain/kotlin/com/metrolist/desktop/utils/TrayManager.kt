package com.metrolist.desktop.utils

import com.metrolist.desktop.state.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.math.roundToInt

/**
 * Modern system tray manager for Metrolist Desktop
 * Uses the actual app icon with subtle state indicators
 */
object TrayManager {
    private var systemTray: SystemTray? = null
    private var trayIcon: TrayIcon? = null
    private var window: Window? = null

    // Cache for app icons with state indicators
    private var cachedAppIcon: Image? = null
    private var cachedPlayingIcon: Image? = null
    private var cachedPausedIcon: Image? = null
    private var isDarkTheme: Boolean = false

    /**
     * Initialize the modern system tray
     */
    fun initialize(mainWindow: Window) {
        if (!SystemTray.isSupported()) {
            println("System tray is not supported")
            return
        }

        try {
            window = mainWindow
            systemTray = SystemTray.getSystemTray()

            // Detect system theme
            detectSystemTheme()

            // Load and cache app icon
            loadAppIcon()

            // Create initial tray icon
            createTrayIcon()

            println("Modern system tray initialized successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to initialize system tray: ${e.message}")
        }
    }

    /**
     * Detect system dark/light theme
     */
    private fun detectSystemTheme() {
        isDarkTheme = try {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> {
                    // Check Windows registry for theme setting
                    val result = Runtime.getRuntime()
                        .exec("reg query HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize /v AppsUseLightTheme")
                        .inputStream.bufferedReader().readText()

                    result.contains("0x0") // Dark theme = 0, Light theme = 1
                }
                osName.contains("mac") -> {
                    // Check macOS appearance
                    val result = Runtime.getRuntime()
                        .exec("defaults read -g AppleInterfaceStyle")
                        .inputStream.bufferedReader().readText()

                    result.trim().equals("Dark", ignoreCase = true)
                }
                else -> {
                    // Linux/other - Default to light theme
                    false
                }
            }
        } catch (e: Exception) {
            false // Default to light theme
        }
    }

    /**
     * Load the app icon from resources
     */
    private fun loadAppIcon() {
        try {
            val iconResource = javaClass.getResourceAsStream("/logo.png")
            if (iconResource != null) {
                cachedAppIcon = ImageIO.read(iconResource)
                println("Loaded app icon successfully")
            } else {
                println("Could not find logo.png in resources, using fallback")
                cachedAppIcon = createFallbackIcon()
            }
        } catch (e: Exception) {
            println("Failed to load app icon: ${e.message}")
            cachedAppIcon = createFallbackIcon()
        }
    }

    /**
     * Create fallback icon if logo.png is not available
     */
    private fun createFallbackIcon(): BufferedImage {
        val size = 32
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Simple fallback icon - a circle with "M"
        g2d.color = Color(0x6200EA)
        g2d.fillOval(4, 4, size - 8, size - 8)

        g2d.color = Color.WHITE
        g2d.font = Font(Font.SANS_SERIF, Font.BOLD, 18)
        val fm = g2d.fontMetrics
        val textWidth = fm.stringWidth("M")
        val textHeight = fm.height
        g2d.drawString("M", (size - textWidth) / 2, (size + textHeight / 2) / 2)

        g2d.dispose()
        return image
    }

    /**
     * Create tray icon with state indicator
     */
    private fun createTrayIcon() {
        val traySize = systemTray?.trayIconSize ?: Dimension(16, 16)
        val scaleFactor = getDisplayScaleFactor()
        val iconSize = (traySize.width * scaleFactor).roundToInt()

        // Create state-aware icons if not cached
        if (cachedPlayingIcon == null || cachedPausedIcon == null) {
            cachedPlayingIcon = createStateIcon(iconSize, isPlaying = true)
            cachedPausedIcon = createStateIcon(iconSize, isPlaying = false)
        }

        // Use appropriate icon based on current state
        val icon = if (AppState.isPlaying) cachedPlayingIcon else cachedPausedIcon

        if (trayIcon == null) {
            // Create new tray icon
            trayIcon = TrayIcon(icon, "Metrolist", createCleanPopupMenu()).apply {
                isImageAutoSize = true
                addActionListener { event ->
                    SwingUtilities.invokeLater { toggleWindowVisibility() }
                }
            }
            systemTray?.add(trayIcon)
        } else {
            // Update existing tray icon
            trayIcon?.image = icon
        }

        updateTooltip()
    }

    /**
     * Create state icon with subtle indicators on the app icon
     */
    private fun createStateIcon(size: Int, isPlaying: Boolean): BufferedImage {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

        // Draw the app icon as base
        if (cachedAppIcon != null) {
            g2d.drawImage(cachedAppIcon, 0, 0, size, size, null)
        }

        // Add subtle state indicator in bottom-right corner
        val indicatorSize = (size * 0.3f).roundToInt()
        val indicatorX = size - indicatorSize
        val indicatorY = size - indicatorSize

        if (isPlaying) {
            // Small green dot for playing
            g2d.color = Color.BLACK.brighter()
            g2d.fillOval(indicatorX - 1, indicatorY - 1, indicatorSize + 2, indicatorSize + 2)
            g2d.color = Color(0x4CAF50) // Material green
            g2d.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize)
        } else {
            // Small gray dot for paused (optional, can be removed for cleaner look)
            if (AppState.currentTrack != null) {
                g2d.color = Color.BLACK.brighter()
                g2d.fillOval(indicatorX - 1, indicatorY - 1, indicatorSize + 2, indicatorSize + 2)
                g2d.color = Color.GRAY
                g2d.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize)
            }
        }

        g2d.dispose()
        return image
    }

    /**
     * Get display scale factor for high-DPI displays
     */
    private fun getDisplayScaleFactor(): Float {
        return try {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val device = ge.defaultScreenDevice
            val config = device.defaultConfiguration
            config.defaultTransform.scaleX.toFloat()
        } catch (e: Exception) {
            1.0f
        }
    }

    /**
     * Create clean popup menu without Unicode issues
     */
    private fun createCleanPopupMenu(): PopupMenu {
        val popup = PopupMenu()

        // Current track info (if playing) - using simple text
        AppState.currentTrack?.let { track ->
            val trackInfo = MenuItem(track.title).apply {
                isEnabled = false
                font = Font("Segoe UI", Font.BOLD, 12)
            }
            popup.add(trackInfo)

            val artistInfo = MenuItem("by ${track.artists.joinToString { it.name }}").apply {
                isEnabled = false
                font = Font("Segoe UI", Font.PLAIN, 11)
            }
            popup.add(artistInfo)
            popup.addSeparator()
        }

        // Playback controls - using simple text instead of Unicode
        val playPauseLabel = if (AppState.isPlaying) "Pause" else "Play"
        popup.add(createCleanMenuItem(playPauseLabel, Font.BOLD) {
            AppState.togglePlay()
        })

        popup.add(createCleanMenuItem("Next Track") {
            AppState.skipNext()
        })

        popup.add(createCleanMenuItem("Previous Track") {
            AppState.skipPrevious()
        })

        popup.addSeparator()

        // Window controls
        val windowLabel = if (window?.isVisible == true) "Hide Window" else "Show Window"
        popup.add(createCleanMenuItem(windowLabel) {
            toggleWindowVisibility()
        })

        popup.add(createCleanMenuItem("Show Miniplayer") {
            showMiniplayer()
        })

        popup.addSeparator()

        // App controls
        popup.add(createCleanMenuItem("Settings") {
            showSettings()
        })

        popup.addSeparator()

        popup.add(createCleanMenuItem("Quit Metrolist") {
            quitApplication()
        })

        return popup
    }

    /**
     * Create a clean menu item with consistent styling
     */
    private fun createCleanMenuItem(text: String, fontStyle: Int = Font.PLAIN, action: () -> Unit): MenuItem {
        return MenuItem(text).apply {
            font = Font("Segoe UI", fontStyle, 12)
            addActionListener { action() }
        }
    }

    /**
     * Update tray icon when playback state changes
     */
    fun updateTrayIcon() {
        SwingUtilities.invokeLater {
            createTrayIcon()
        }
    }

    /**
     * Update tooltip with current track info
     */
    private fun updateTooltip() {
        trayIcon?.let { icon ->
            val tooltip = if (AppState.currentTrack == null) {
                "Metrolist"
            } else {
                val track = AppState.currentTrack!!
                val state = if (AppState.isPlaying) "Playing" else "Paused"
                "$state: ${track.title}\nby ${track.artists.joinToString { it.name }}\n\nMetrolist"
            }
            icon.toolTip = tooltip
        }
    }

    /**
     * Toggle main window visibility
     */
    private fun toggleWindowVisibility() {
        window?.let { win ->
            GlobalScope.launch(Dispatchers.Main) {
                if (win.isVisible) {
                    win.isVisible = false
                } else {
                    win.isVisible = true
                    win.toFront()
                    win.requestFocus()
                }
            }
        }
    }

    /**
     * Show miniplayer
     */
    private fun showMiniplayer() {
        GlobalScope.launch(Dispatchers.Main) {
            AppState.showMiniplayer = true
        }
    }

    /**
     * Show settings
     */
    private fun showSettings() {
        GlobalScope.launch(Dispatchers.Main) {
            AppState.showSettings = true
            window?.let { win ->
                if (!win.isVisible) {
                    win.isVisible = true
                    win.toFront()
                }
            }
        }
    }

    /**
     * Quit application
     */
    private fun quitApplication() {
        SwingUtilities.invokeLater {
            System.exit(0)
        }
    }

    /**
     * Clean up system tray resources
     */
    fun cleanup() {
        try {
            trayIcon?.let { icon ->
                systemTray?.remove(icon)
            }
            trayIcon = null
            systemTray = null
            cachedAppIcon = null
            cachedPlayingIcon = null
            cachedPausedIcon = null
            println("System tray cleaned up")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}