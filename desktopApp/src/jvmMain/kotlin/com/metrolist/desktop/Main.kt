@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
package com.metrolist.desktop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.ui.zIndex
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javafx.application.Platform
import kotlinx.coroutines.launch
import com.metrolist.shared.model.*
import com.metrolist.shared.api.lastfm.LastFM
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.state.NavItem
import com.metrolist.desktop.constants.*
import com.metrolist.desktop.ui.theme.*
import com.metrolist.desktop.ui.components.*
import com.metrolist.desktop.ui.screens.*
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun WindowScope.App(onClose: () -> Unit, onMinimize: () -> Unit, onMaximize: () -> Unit) {
    val animatedPrimary by animateColorAsState(targetValue = AppState.seedColor, animationSpec = tween(1000))
    
    MetrolistTheme(seedColor = animatedPrimary) {
        val colorScheme = MaterialTheme.colorScheme
        val windowCornerRadius = if (AppState.isMaximized) 0.dp else 12.dp
        val outlineColor = if (AppState.isWindowFocused && !AppState.isMaximized) colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
        
        var searchText by remember { mutableStateOf("") }
        var selectedNavIndex by remember { mutableIntStateOf(0) }
        var searchTracks by remember { mutableStateOf<List<YTItem>>(emptyList()) }
        var isSidebarExpanded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val visibleItems = if (AppState.isEditingSidebar) AppState.sidebarNavItems else AppState.sidebarNavItems.filter { it.visible }

        // Only use the label of the currently selected tab for the title
        val currentTitle = if (AppState.showIntegrations) "Integrations" else visibleItems.getOrNull(selectedNavIndex)?.label ?: "Metrolist"

        Surface(
            modifier = Modifier.fillMaxSize()
                .clip(RoundedCornerShape(windowCornerRadius))
                .border(if (AppState.isMaximized) 0.dp else 1.dp, outlineColor, RoundedCornerShape(windowCornerRadius)),
            color = colorScheme.background
        ) {
            Column(Modifier.fillMaxSize()) {
                // Top Bar 
                Box(Modifier.fillMaxWidth().zIndex(1f)) {
                    CustomTitleBar(
                        title = currentTitle,
                        colorScheme = colorScheme,
                        searchText = searchText,
                        onSearchChange = { 
                            searchText = it
                            if (it.length > 2) {
                                scope.launch { 
                                    try { 
                                        searchTracks = AppState.repository.search(it)
                                    } catch (_: Exception) {} 
                                }
                            } 
                        },
                        onClose = onClose,
                        onMinimize = onMinimize,
                        onMaximize = onMaximize
                    )
                }

                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Left Sidebar 
                    val sidebarWidth by animateDpAsState(
                        targetValue = if (isSidebarExpanded) SideRailWidth else SideRailCollapsedWidth,
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    )
                    
                    Surface(
                        modifier = Modifier.fillMaxHeight().width(sidebarWidth),
                        color = colorScheme.surfaceContainer
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { 
                                    isSidebarExpanded = !isSidebarExpanded 
                                    if (!isSidebarExpanded) AppState.isEditingSidebar = false
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar", tint = colorScheme.onSurfaceVariant)
                                }
                                
                                AnimatedVisibility(
                                    visible = isSidebarExpanded,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    Text(
                                        "Metrolist",
                                        modifier = Modifier.padding(start = 8.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                }

                                if (isSidebarExpanded) {
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = { AppState.isEditingSidebar = !AppState.isEditingSidebar },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Icon(
                                            if (AppState.isEditingSidebar) Icons.Default.Check else Icons.Default.Edit, 
                                            null, 
                                            tint = if (AppState.isEditingSidebar) colorScheme.primary else colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Sidebar Navigation Items
                            val iconMap = mapOf(
                                "home" to (Icons.Outlined.Home to Icons.Default.Home),
                                "library" to (Icons.Outlined.LibraryMusic to Icons.Default.LibraryMusic),
                                "history" to (Icons.Outlined.History to Icons.Default.History),
                                "stats" to (MetrolistStatsIcon to MetrolistStatsIcon),
                                "together" to (MetrolistTogetherIcon to MetrolistTogetherIcon)
                            )
                            
                            if (AppState.isEditingSidebar) {
                                val lazyListState = rememberLazyListState()
                                val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                                    val newList = AppState.sidebarNavItems.toMutableList()
                                    val item = newList.removeAt(from.index)
                                    newList.add(to.index, item)
                                    AppState.saveSidebarNavItems(newList)
                                }

                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(AppState.sidebarNavItems, key = { it.id }) { navItem ->
                                        ReorderableItem(reorderableState, key = navItem.id) {
                                            val icons = iconMap[navItem.id] ?: (Icons.Default.QuestionMark to Icons.Default.QuestionMark)
                                            EditingSidebarNavItem(
                                                navItem = navItem,
                                                icon = icons.first,
                                                onToggleVisibility = { 
                                                    val newList = AppState.sidebarNavItems.map { 
                                                        if (it.id == navItem.id) it.copy(visible = !it.visible) else it 
                                                    }
                                                    AppState.saveSidebarNavItems(newList)
                                                },
                                                colorScheme = colorScheme,
                                                modifier = Modifier.draggableHandle()
                                            )
                                        }
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    visibleItems.forEachIndexed { idx, navItem ->
                                        val icons = iconMap[navItem.id] ?: (Icons.Default.QuestionMark to Icons.Default.QuestionMark)
                                        SidebarNavItem(
                                            selected = selectedNavIndex == idx && !AppState.showSignIn && !AppState.showSettings && !AppState.showIntegrations && AppState.selectedArtistId == null && AppState.selectedPlaylistId == null,
                                            onClick = { 
                                                selectedNavIndex = idx
                                                AppState.showSignIn = false
                                                AppState.showSettings = false
                                                AppState.showIntegrations = false
                                                AppState.selectedArtistId = null
                                                AppState.selectedPlaylistId = null
                                                searchText = ""
                                            },
                                            icon = if (selectedNavIndex == idx && !AppState.showSettings && !AppState.showIntegrations) icons.second else icons.first,
                                            label = navItem.label,
                                            isExpanded = isSidebarExpanded,
                                            colorScheme = colorScheme
                                        )
                                    }
                                }
                            }
                            
                            if (isSidebarExpanded && !AppState.isEditingSidebar) {
                                Spacer(Modifier.height(16.dp))
                                
                                // New Playlist Button
                                Surface(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clickable { },
                                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp), tint = colorScheme.onSurface)
                                        Spacer(Modifier.width(12.dp))
                                        Text("New playlist", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    val playlistItems = AppState.librarySections["Library"]?.filterIsInstance<PlaylistItem>() ?: emptyList()
                                    items(playlistItems) { playlist ->
                                        SidebarPlaylistEntry(playlist, colorScheme)
                                    }
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                            
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // Main Content!!
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
                            if (AppState.showSignIn) {
                                EmbeddedSignInView(
                                    onAuthDataExtracted = { cookie, visitorData, dataSyncId -> 
                                        AppState.updateAuth(cookie, visitorData, dataSyncId) 
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else if (AppState.showSettings) SettingsScreen(colorScheme)
                            else if (AppState.showIntegrations) IntegrationsScreen(colorScheme)
                            else if (AppState.selectedArtistId != null) {
                                ArtistScreen(AppState.selectedArtistId!!, colorScheme)
                            }
                            else if (AppState.selectedPlaylistId != null) {
                                PlaylistScreen(AppState.selectedPlaylistId!!, colorScheme)
                            }
                            else if (searchText.isNotEmpty()) SearchResultsList(searchTracks, colorScheme)
                            else {
                                val currentNavItem = visibleItems.getOrNull(selectedNavIndex)
                                when (currentNavItem?.id) {
                                    "home" -> HomeScreen(AppState.homeSections, colorScheme)
                                    "library" -> LibraryScreen(AppState.librarySections, colorScheme)
                                    "history" -> HistoryScreen(colorScheme)
                                    "stats" -> StatsScreen(colorScheme)
                                    "together" -> TogetherScreen(colorScheme)
                                    else -> HomeScreen(AppState.homeSections, colorScheme)
                                }
                            }
                        }
                        
                        androidx.compose.animation.AnimatedVisibility(
                            visible = AppState.isExpanded,
                            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(600, easing = EaseOutQuart)) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(600, easing = EaseInQuart)) + fadeOut()
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
                                ExpandedPlayerView(colorScheme)
                            }
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = AppState.currentTrack != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    StandardBottomPlayer(colorScheme)
                }
            }
        }
    }
}

@Composable
fun EditingSidebarNavItem(
    navItem: NavItem,
    icon: ImageVector,
    onToggleVisibility: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, 
            null, 
            tint = if (navItem.visible) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            navItem.label,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
            style = MaterialTheme.typography.labelLarge,
            color = if (navItem.visible) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.3f),
            maxLines = 1
        )
        
        Checkbox(
            checked = navItem.visible,
            onCheckedChange = { onToggleVisibility() },
            colors = CheckboxDefaults.colors(checkedColor = colorScheme.primary)
        )

        Icon(
            Icons.Default.DragIndicator, 
            null, 
            tint = colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = modifier.size(24.dp).padding(start = 4.dp)
        )
    }
}

@Composable
fun SidebarPlaylistEntry(playlist: PlaylistItem, colorScheme: ColorScheme) {
    val isLikedMusic = playlist.title.contains("Liked", ignoreCase = true)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isLikedMusic) colorScheme.surfaceVariant else Color.Transparent)
            .clickable { AppState.fetchPlaylistData(playlist.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playlist.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isLikedMusic) FontWeight.Bold else FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLikedMusic) {
                        Icon(Icons.Default.PushPin, null, modifier = Modifier.size(12.dp).rotate(45f), tint = colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        if (isLikedMusic) "Auto playlist" else playlist.author ?: "Lamp",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isLikedMusic) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun SidebarNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    isExpanded: Boolean,
    colorScheme: ColorScheme
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (selected) colorScheme.secondaryContainer else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = label,
                    tint = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun HistoryScreen(colorScheme: ColorScheme) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            Text("History - Coming Soon", color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun StatsScreen(colorScheme: ColorScheme) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(MetrolistStatsIcon, null, modifier = Modifier.size(64.dp), tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            Text("Stats - Coming Soon", color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun TogetherScreen(colorScheme: ColorScheme) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(MetrolistTogetherIcon, null, modifier = Modifier.size(64.dp), tint = colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            Text("Together - Coming Soon", color = colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium)
        }
    }
}

fun main() {
    Platform.startup {}
    
    LastFM.initialize(
        apiKey = BuildConfig.LASTFM_API_KEY,
        secret = BuildConfig.LASTFM_SECRET
    )
    
    // Load saved session
    LastFM.sessionKey = AppState.prefs.get("LASTFM_SESSION", null)
    
    AppState.loadSession()
    
    application {
        // Load window state
        val windowPlacement = try {
            WindowPlacement.valueOf(AppState.prefs.get("WINDOW_PLACEMENT", WindowPlacement.Maximized.name))
        } catch (_: Exception) {
            WindowPlacement.Maximized
        }
        
        val windowState = rememberWindowState(
            placement = windowPlacement,
            position = WindowPosition(
                AppState.prefs.getInt("WINDOW_X", 100).dp,
                AppState.prefs.getInt("WINDOW_Y", 100).dp
            ),
            size = DpSize(
                AppState.prefs.getInt("WINDOW_WIDTH", 1280).dp,
                AppState.prefs.getInt("WINDOW_HEIGHT", 800).dp
            )
        )

        // Static icon: Nice gradient flowy background (static) + white logo
        val logoPainter = painterResource("logo.svg")
        val animatedPrimary by animateColorAsState(targetValue = AppState.seedColor, animationSpec = tween(1000))
        
        val dynamicIcon = remember(animatedPrimary) {
            object : Painter() {
                override val intrinsicSize: Size = Size(256f, 256f)
                override fun DrawScope.onDraw() {
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                animatedPrimary.copy(alpha = 0.8f),
                                animatedPrimary,
                                animatedPrimary.copy(alpha = 0.6f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        radius = size.minDimension / 2,
                        center = center
                    )
                    
                    drawCircle(
                        color = animatedPrimary,
                        radius = size.minDimension / 2.5f,
                        center = center
                    )
                    
                    with(logoPainter) {
                        val logoSize = size * 0.55f
                        val offset = Offset((size.width - logoSize.width) / 2f, (size.height - logoSize.height) / 2f)
                        translate(offset.x, offset.y) {
                            draw(logoSize, colorFilter = ColorFilter.tint(Color.White))
                        }
                    }
                }
            }
        }

        Window(
            onCloseRequest = {
                AppState.prefs.put("WINDOW_PLACEMENT", windowState.placement.name)
                AppState.prefs.putInt("WINDOW_X", windowState.position.let { if (it is WindowPosition.Absolute) it.x.value.toInt() else 100 })
                AppState.prefs.putInt("WINDOW_Y", windowState.position.let { if (it is WindowPosition.Absolute) it.y.value.toInt() else 100 })
                AppState.prefs.putInt("WINDOW_WIDTH", windowState.size.width.value.toInt())
                AppState.prefs.putInt("WINDOW_HEIGHT", windowState.size.height.value.toInt())
                AppState.prefs.flush()
                
                // Properly release player resources
                AppState.player.release()
                
                exitApplication()
            },
            title = "Metrolist",
            state = windowState,
            undecorated = true,
            transparent = true,
            icon = dynamicIcon
        ) {
            // Fix maximized issues
            LaunchedEffect(Unit) {
                try {
                    val ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    val gd = ge.defaultScreenDevice
                    val gc = gd.defaultConfiguration
                    val insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc)
                    val bounds = gc.bounds
                    val maxBounds = java.awt.Rectangle(
                        bounds.x + insets.left,
                        bounds.y + insets.top,
                        bounds.width - (insets.left + insets.right),
                        bounds.height - (insets.top + insets.bottom)
                    )
                    window.maximizedBounds = maxBounds
                } catch (_: Exception) {}
            }

            DisposableEffect(window) {
                val listener = object : WindowAdapter() {
                    override fun windowActivated(e: WindowEvent?) { AppState.isWindowFocused = true }
                    override fun windowDeactivated(e: WindowEvent?) { AppState.isWindowFocused = false }
                }
                window.addWindowListener(listener)
                onDispose { window.removeWindowListener(listener) }
            }
            LaunchedEffect(windowState.placement) {
                AppState.isMaximized = windowState.placement == WindowPlacement.Maximized
            }
            App(
                onClose = {
                    AppState.prefs.put("WINDOW_PLACEMENT", windowState.placement.name)
                    AppState.prefs.putInt("WINDOW_X", windowState.position.let { if (it is WindowPosition.Absolute) it.x.value.toInt() else 100 })
                    AppState.prefs.putInt("WINDOW_Y", windowState.position.let { if (it is WindowPosition.Absolute) it.y.value.toInt() else 100 })
                    AppState.prefs.putInt("WINDOW_WIDTH", windowState.size.width.value.toInt())
                    AppState.prefs.putInt("WINDOW_HEIGHT", windowState.size.height.value.toInt())
                    AppState.prefs.flush()
                    
                    // Properly release player resources
                    AppState.player.release()
                    
                    exitApplication()
                },
                onMinimize = { windowState.isMinimized = true },
                onMaximize = {
                    windowState.placement = if (windowState.placement == WindowPlacement.Maximized) { 
                        WindowPlacement.Floating 
                    } else { 
                        WindowPlacement.Maximized 
                    }
                    AppState.isMaximized = windowState.placement == WindowPlacement.Maximized
                }
            )
        }
    }
}
