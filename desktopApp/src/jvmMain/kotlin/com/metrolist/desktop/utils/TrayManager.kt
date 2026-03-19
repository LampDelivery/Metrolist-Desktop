package com.metrolist.desktop.utils

import com.metrolist.desktop.state.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.MouseInfo
import javax.swing.JPopupMenu
import javax.swing.JMenuItem
import javax.swing.BorderFactory
import javax.swing.JWindow
import javax.swing.PopupFactory
import javax.swing.plaf.ColorUIResource
import javax.swing.UIManager
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
    // Optional theme override (set from Compose side)
    private var useThemeColors: Boolean = false
    private var themeBgColor: Color? = null
    private var themeFgColor: Color? = null
    private var themeBorderColor: Color? = null
    private var popupInvoker: JWindow? = null

    /**
     * Allow Compose side to push theme color overrides into the tray manager.
     * `bgArgb`, `fgArgb`, and `borderArgb` are ARGB ints from Compose `toArgb()`.
     */
    fun setThemeColors(bgArgb: Int, fgArgb: Int, borderArgb: Int, dark: Boolean) {
        try {
            useThemeColors = true
            themeBgColor = Color(bgArgb, true)
            themeFgColor = Color(fgArgb, true)
            themeBorderColor = Color(borderArgb, true)
            isDarkTheme = dark
        } catch (_: Exception) {
            // ignore invalid colors
        }
    }

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

            // Prepare popup invoker for styled menus
            preparePopupInvoker()

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
            // Create new tray icon (no AWT PopupMenu) — we'll use a styled Swing popup
            trayIcon = TrayIcon(icon, "Metrolist", null).apply {
                isImageAutoSize = true
                addActionListener { _ ->
                    SwingUtilities.invokeLater { toggleWindowVisibility() }
                }
                addMouseListener(object : MouseAdapter() {
                    override fun mouseReleased(e: MouseEvent) {
                        if (e.isPopupTrigger) {
                            SwingUtilities.invokeLater { showStyledPopupMenu() }
                        }
                    }

                    override fun mousePressed(e: MouseEvent) {
                        if (e.isPopupTrigger) {
                            SwingUtilities.invokeLater { showStyledPopupMenu() }
                        }
                    }
                })
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
            g2d.color = Color.BLACK
            g2d.fillOval(indicatorX - 1, indicatorY - 1, indicatorSize + 2, indicatorSize + 2)
            g2d.color = Color(0x4CAF50)
            g2d.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize)
        } else {
            if (AppState.currentTrack != null) {
                g2d.color = Color.BLACK
                g2d.fillOval(indicatorX - 1, indicatorY - 1, indicatorSize + 2, indicatorSize + 2)
                g2d.color = Color.GRAY
                g2d.fillOval(indicatorX, indicatorY, indicatorSize, indicatorSize)
            }
        }

        g2d.dispose()
        return image
    }

    /**
     * Prepare an invisible JWindow to act as invoker for styled Swing popups
     */
    private fun preparePopupInvoker() {
        SwingUtilities.invokeLater {
            try {
                popupInvoker = JWindow()
                popupInvoker?.isAlwaysOnTop = true
                popupInvoker?.setSize(1, 1)
            } catch (e: Exception) {
                popupInvoker = null
            }
        }
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
     * Build and show a styled dark JPopupMenu at the current mouse location
     */
    private fun showStyledPopupMenu() {
        try {
            // Use dynamic colors based on detected theme or Compose-provided overrides
            val bgColor = when {
                useThemeColors && themeBgColor != null -> themeBgColor!!
                isDarkTheme -> Color(0x121212)
                else -> Color(0xFFFFFF)
            }
            val fgColor = when {
                useThemeColors && themeFgColor != null -> themeFgColor!!
                isDarkTheme -> Color(0xE0E0E0)
                else -> Color(0x212121)
            }
            val borderColor = when {
                useThemeColors && themeBorderColor != null -> themeBorderColor!!
                isDarkTheme -> Color(0x2C2C2C)
                else -> Color(0xDDDDDD)
            }

            UIManager.put("PopupMenu.background", ColorUIResource(bgColor))
            UIManager.put("MenuItem.background", ColorUIResource(bgColor))
            UIManager.put("MenuItem.foreground", ColorUIResource(fgColor))
            // Make separators match the theme
            UIManager.put("Separator.background", ColorUIResource(borderColor))
            UIManager.put("Separator.foreground", ColorUIResource(borderColor))

            // Build a heavyweight transparent JDialog (focusable) to avoid lightweight popup artifacts
            val radius = 14
            val menuWindow = javax.swing.JDialog().apply {
                isUndecorated = true
                isAlwaysOnTop = true
                // Transparent window so our rounded content defines corners
                background = Color(0, 0, 0, 0)
                // Make window focusable so focus loss events fire when user clicks other apps
                try { setFocusableWindowState(true) } catch (_: Exception) {}
                try { setType(Window.Type.POPUP) } catch (_: Exception) {}
            }

            // Content panel paints rounded background
            val content = object : javax.swing.JPanel() {
                init {
                    isOpaque = false
                    layout = javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS)
                    border = javax.swing.border.EmptyBorder(8, 10, 8, 10)
                }

                override fun paintComponent(g: java.awt.Graphics) {
                    val g2 = g.create() as java.awt.Graphics2D
                    try {
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                        g2.color = bgColor
                        g2.fillRoundRect(0, 0, width, height, radius, radius)
                    } finally {
                        g2.dispose()
                    }
                    super.paintComponent(g)
                }
            }

            // Selection background color
            val selectionBg = if (isDarkTheme || (useThemeColors && ((themeBgColor?.let { it.rgb and 0xFFFFFF }?.let { Color(it).red } ?: 0) < 128))) {
                bgColor.brighter()
            } else {
                bgColor.darker()
            }

            // Helper to add a labeled row with hover and click
            fun addRow(text: String, enabled: Boolean = true, action: (() -> Unit)? = null, fontStyle: Int = Font.PLAIN, fgOverride: Color? = null) {
                val label = javax.swing.JLabel(text).apply {
                    foreground = fgOverride ?: fgColor
                    font = Font("Segoe UI", fontStyle, 12)
                    isOpaque = false
                    alignmentX = java.awt.Component.LEFT_ALIGNMENT
                    border = javax.swing.border.EmptyBorder(6, 6, 6, 6)
                    cursor = if (enabled && action != null) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor()
                }
                if (enabled && action != null) {
                    label.addMouseListener(object : java.awt.event.MouseAdapter() {
                        override fun mouseEntered(e: java.awt.event.MouseEvent?) {
                            label.background = selectionBg
                            label.isOpaque = true
                            label.repaint()
                        }
                        override fun mouseExited(e: java.awt.event.MouseEvent?) {
                            label.isOpaque = false
                            label.repaint()
                        }
                        override fun mousePressed(e: java.awt.event.MouseEvent?) {
                            try { action() } catch (_: Exception) {}
                            // hide
                            SwingUtilities.invokeLater {
                                try { menuWindow.isVisible = false } catch (_: Exception) {}
                            }
                        }
                    })
                }
                content.add(label)
            }

            // Current track info
            AppState.currentTrack?.let { track ->
                val title = javax.swing.JLabel(track.title).apply {
                    foreground = fgColor
                    font = Font("Segoe UI", Font.BOLD, 12)
                    isOpaque = false
                    alignmentX = java.awt.Component.LEFT_ALIGNMENT
                    border = javax.swing.border.EmptyBorder(6, 6, 6, 6)
                }
                content.add(title)
                val artist = javax.swing.JLabel("by ${track.artists.joinToString { it.name }}").apply {
                    foreground = if (isDarkTheme) Color(0xBDBDBD) else Color(0x666666)
                    font = Font("Segoe UI", Font.PLAIN, 11)
                    isOpaque = false
                    alignmentX = java.awt.Component.LEFT_ALIGNMENT
                    border = javax.swing.border.EmptyBorder(4, 6, 4, 6)
                }
                content.add(artist)
                // separator
                val sep = javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL).apply {
                    foreground = borderColor
                    background = borderColor
                    isOpaque = true
                    preferredSize = java.awt.Dimension(1, 1)
                    maximumSize = java.awt.Dimension(Int.MAX_VALUE, 2)
                    alignmentX = java.awt.Component.LEFT_ALIGNMENT
                }
                content.add(sep)
            }

            // Playback controls
            addRow(if (AppState.isPlaying) "Pause" else "Play", action = { AppState.togglePlay() })
            addRow("Next Track", action = { AppState.skipNext() })
            addRow("Previous Track", action = { AppState.skipPrevious() })

            // separator
            content.add(javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL).apply {
                foreground = borderColor; background = borderColor; isOpaque = true; preferredSize = java.awt.Dimension(1, 1); maximumSize = java.awt.Dimension(Int.MAX_VALUE, 2); alignmentX = java.awt.Component.LEFT_ALIGNMENT
            })

            // Window controls
            val windowLabel = if (window?.isVisible == true) "Hide Window" else "Show Window"
            addRow(windowLabel, action = { toggleWindowVisibility() })
            addRow("Show Miniplayer", action = { showMiniplayer() })

            // separator
            content.add(javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL).apply {
                foreground = borderColor; background = borderColor; isOpaque = true; preferredSize = java.awt.Dimension(1, 1); maximumSize = java.awt.Dimension(Int.MAX_VALUE, 2); alignmentX = java.awt.Component.LEFT_ALIGNMENT
            })

            // App controls
            addRow("Settings", action = { showSettings() })
            content.add(javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL).apply {
                foreground = borderColor; background = borderColor; isOpaque = true; preferredSize = java.awt.Dimension(1, 1); maximumSize = java.awt.Dimension(Int.MAX_VALUE, 2); alignmentX = java.awt.Component.LEFT_ALIGNMENT
            })
            addRow("Quit Metrolist", action = { quitApplication() })

            menuWindow.contentPane.add(content)

            // Show window centered around mouse pointer
            val p = MouseInfo.getPointerInfo().location
            menuWindow.pack()
            val mw = menuWindow.preferredSize.width
            val mh = menuWindow.preferredSize.height
            val locX = p.x.toInt() - mw / 2
            val locY = p.y.toInt() - mh / 2
            menuWindow.location = Point(locX, locY)
            // Show the window and request focus so WindowFocusListener receives focus loss when user clicks elsewhere
            try {
                menuWindow.isVisible = true
                menuWindow.toFront()
                menuWindow.requestFocus()
                content.isFocusable = true
                content.requestFocusInWindow()
            } catch (_: Exception) {}

            // Add global mouse listener to dismiss the menu when clicking outside
            var awtListener: java.awt.event.AWTEventListener? = null
            awtListener = object : java.awt.event.AWTEventListener {
                override fun eventDispatched(ev: java.awt.AWTEvent) {
                    try {
                        if (ev is java.awt.event.MouseEvent && ev.id == java.awt.event.MouseEvent.MOUSE_PRESSED) {
                            val ex = ev.xOnScreen
                            val ey = ev.yOnScreen
                            val bounds = java.awt.Rectangle(menuWindow.x, menuWindow.y, menuWindow.width, menuWindow.height)
                            if (!bounds.contains(ex, ey)) {
                                SwingUtilities.invokeLater {
                                    try { menuWindow.isVisible = false } catch (_: Exception) {}
                                    try { java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener) } catch (_: Exception) {}
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
            try {
                java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(awtListener, java.awt.AWTEvent.MOUSE_EVENT_MASK)
            } catch (_: Exception) {}

            // Also hide when the menu window loses focus (robust fallback)
            menuWindow.addWindowFocusListener(object : java.awt.event.WindowFocusListener {
                override fun windowGainedFocus(e: java.awt.event.WindowEvent?) {}
                override fun windowLostFocus(e: java.awt.event.WindowEvent?) {
                    SwingUtilities.invokeLater {
                        try { menuWindow.isVisible = false } catch (_: Exception) {}
                        try { if (awtListener != null) java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener) } catch (_: Exception) {}
                    }
                }
            })

            // (focus already requested above)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createStyledMenuItem(text: String, fg: Color, bg: Color, action: () -> Unit): JMenuItem {
        return JMenuItem(text).apply {
            font = Font("Segoe UI", Font.PLAIN, 12)
            foreground = fg
            background = bg
            isOpaque = true
            addActionListener { action() }
        }
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