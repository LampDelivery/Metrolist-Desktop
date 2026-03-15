@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
package com.metrolist.desktop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
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
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.*
import androidx.compose.ui.zIndex
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javafx.application.Platform
import kotlinx.coroutines.launch
import com.metrolist.shared.model.*
import com.metrolist.shared.api.lastfm.LastFM
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.state.GlobalYouTubeRepository
import com.metrolist.desktop.state.NavItem
import com.metrolist.desktop.constants.*
import com.metrolist.desktop.ui.theme.*
import com.metrolist.desktop.ui.components.*
import com.metrolist.desktop.ui.screens.*
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun WindowScope.App(onClose: () -> Unit, onMinimize: () -> Unit, onMaximize: () -> Unit) {
    val animatedPrimary by animateColorAsState(targetValue = AppState.seedColor, animationSpec = tween(1000))
    val focusManager = LocalFocusManager.current
    
    MetrolistTheme(seedColor = animatedPrimary) {
        val colorScheme = MaterialTheme.colorScheme
        val windowCornerRadius = if (AppState.isMaximized) 0.dp else 12.dp
        val outlineColor = if (AppState.isWindowFocused && !AppState.isMaximized) colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
        
        var searchText by remember { mutableStateOf("") }
        var selectedNavIndex by remember { mutableIntStateOf(
            AppState.sidebarNavItems.filter { it.visible }
                .indexOfFirst { it.id == AppState.defaultOpenTab }
                .coerceAtLeast(0)
        ) }
        var isSidebarExpanded by remember { mutableStateOf(false) }

        val visibleItems = if (AppState.isEditingSidebar) AppState.sidebarNavItems else AppState.sidebarNavItems.filter { it.visible }
        val currentTitle = when (AppState.selectedLocalPlaylist) {
            "top50" -> "My Top 50"
            "downloaded" -> "Downloaded"
            "cached" -> "Cached"
            "uploaded" -> "Uploaded"
            else -> if (AppState.showIntegrations) "Integrations" else visibleItems.getOrNull(selectedNavIndex)?.label ?: "Metrolist"
        }

        val pageKey = when {
            AppState.showSignIn -> "signIn"
            AppState.showSettings -> "settings"
            AppState.showIntegrations -> "integrations"
            AppState.selectedLocalPlaylist != null -> "localPlaylist"
            AppState.selectedArtistId != null -> "artist"
            AppState.selectedPlaylistId != null -> "playlist"
            AppState.selectedAlbumId != null -> "album"
            searchText.isNotEmpty() || AppState.searchSummaryPage != null || AppState.searchResultPage != null -> "search"
            else -> visibleItems.getOrNull(selectedNavIndex)?.id ?: "home"
        }

        val topBarAlpha by animateFloatAsState(
            targetValue = if (AppState.isExpanded) 1f else (AppState.topBarScrollOffset / 300f).coerceIn(0f, 1f),
            animationSpec = tween(200, easing = LinearEasing),
            label = "topBarAlpha"
        )

        Surface(
            modifier = Modifier.fillMaxSize()
                .clip(RoundedCornerShape(windowCornerRadius))
                .border(if (AppState.isMaximized) 0.dp else 1.dp, outlineColor, RoundedCornerShape(windowCornerRadius)),
            color = colorScheme.background
        ) {
            Column(Modifier.fillMaxSize()) {

                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val expansionProgress by animateFloatAsState(
                        targetValue = if (isSidebarExpanded) 1f else 0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                    )
                    
                    val sidebarWidth = lerp(SideRailCollapsedWidth, SideRailWidth, expansionProgress)
                    
                    Surface(
                        modifier = Modifier.fillMaxHeight().width(sidebarWidth),
                        color = colorScheme.surfaceContainer
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(TopBarHeight),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(SideRailCollapsedWidth), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { 
                                        isSidebarExpanded = !isSidebarExpanded 
                                        if (!isSidebarExpanded) AppState.isEditingSidebar = false
                                        focusManager.clearFocus()
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Toggle Sidebar", tint = colorScheme.onSurfaceVariant)
                                    }
                                }
                                
                                Text(
                                    "Metrolist",
                                    modifier = Modifier.padding(start = 4.dp).alpha(expansionProgress),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1
                                )

                                if (isSidebarExpanded) {
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = { 
                                            AppState.isEditingSidebar = !AppState.isEditingSidebar 
                                            focusManager.clearFocus()
                                        },
                                        modifier = Modifier.padding(end = 8.dp).alpha(expansionProgress)
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

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                Text(
                                    "Your Library",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                                )

                                val editPlaylists = AppState.librarySections["Library"]
                                    ?.filterIsInstance<PlaylistItem>() ?: emptyList()
                                if (editPlaylists.isNotEmpty()) {
                                    val playlistListState = rememberLazyListState()
                                    val playlistReorderState = rememberReorderableLazyListState(playlistListState) { from, to ->
                                        val current = AppState.librarySections["Library"]
                                            ?.filterIsInstance<PlaylistItem>() ?: emptyList()
                                        val ids = AppState.getOrderedPlaylists(current).map { it.id }.toMutableList()
                                        val moved = ids.removeAt(from.index)
                                        ids.add(to.index, moved)
                                        AppState.savePlaylistOrder(ids)
                                    }
                                    LazyColumn(
                                        state = playlistListState,
                                        modifier = Modifier.weight(1f).fillMaxWidth()
                                    ) {
                                        items(AppState.getOrderedPlaylists(editPlaylists), key = { it.id }) { playlist ->
                                            ReorderableItem(playlistReorderState, key = playlist.id) {
                                                EditingPlaylistItem(
                                                    playlist = playlist,
                                                    isVisible = playlist.id !in AppState.hiddenPlaylistIds,
                                                    onToggleVisibility = { AppState.togglePlaylistVisibility(playlist.id) },
                                                    colorScheme = colorScheme,
                                                    modifier = Modifier.draggableHandle()
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    visibleItems.forEachIndexed { idx, navItem ->
                                        val icons = iconMap[navItem.id] ?: (Icons.Default.QuestionMark to Icons.Default.QuestionMark)
                                        SidebarNavItem(
                                            selected = selectedNavIndex == idx && !AppState.showSignIn && !AppState.showSettings && !AppState.showIntegrations && AppState.selectedArtistId == null && AppState.selectedPlaylistId == null && AppState.selectedAlbumId == null,
                                            onClick = {
                                                AppState.isExpanded = false // Collapse player on tab switch
                                                selectedNavIndex = idx
                                                AppState.showSignIn = false
                                                AppState.showSettings = false
                                                AppState.showIntegrations = false
                                                AppState.selectedLocalPlaylist = null
                                                AppState.selectedArtistId = null
                                                AppState.selectedPlaylistId = null
                                                AppState.selectedAlbumId = null
                                                searchText = ""
                                                AppState.searchSummaryPage = null
                                                AppState.searchResultPage = null
                                                focusManager.clearFocus()
                                            },
                                            icon = if (selectedNavIndex == idx && !AppState.showSettings && !AppState.showIntegrations) icons.second else icons.first,
                                            label = navItem.label,
                                            expansionProgress = expansionProgress,
                                            colorScheme = colorScheme
                                        )
                                    }
                                }
                            }
                            
                            if (isSidebarExpanded && !AppState.isEditingSidebar) {
                                Spacer(Modifier.height(8.dp))

                                // Library display state
                                var librarySortMode by remember { mutableStateOf("Recents") }
                                var libraryViewMode by remember { mutableStateOf("List") }
                                var showSortMenu by remember { mutableStateOf(false) }

                                val rawPlaylists = AppState.librarySections["Library"]
                                    ?.filterIsInstance<PlaylistItem>()
                                    ?.filter { it.id !in AppState.hiddenPlaylistIds }
                                    ?: emptyList()
                                val sortedPlaylists = when (librarySortMode) {
                                    "Alphabetical" -> rawPlaylists.sortedBy { it.title }
                                    "Creator"      -> rawPlaylists.sortedBy { it.author ?: "" }
                                    "Recently Added" -> rawPlaylists.reversed()
                                    else           -> AppState.getOrderedPlaylists(rawPlaylists)
                                }

                                // Header: "Your Library" + sort chip + view toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                                        .alpha(expansionProgress),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Your Library",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Sort chip
                                    Box {
                                        TextButton(
                                            onClick = { showSortMenu = true },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                librarySortMode,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                            Icon(
                                                Icons.Outlined.KeyboardArrowDown, null,
                                                modifier = Modifier.size(14.dp),
                                                tint = colorScheme.onSurfaceVariant
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showSortMenu,
                                            onDismissRequest = { showSortMenu = false }
                                        ) {
                                            listOf("Recents", "Recently Added", "Alphabetical", "Creator").forEach { mode ->
                                                DropdownMenuItem(
                                                    text = { Text(mode, style = MaterialTheme.typography.bodySmall) },
                                                    onClick = { librarySortMode = mode; showSortMenu = false },
                                                    leadingIcon = if (librarySortMode == mode) {
                                                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                                    } else null
                                                )
                                            }
                                        }
                                    }
                                    // View mode toggle
                                    IconButton(
                                        onClick = { libraryViewMode = if (libraryViewMode == "List") "Grid" else "List" },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (libraryViewMode == "Grid") Icons.AutoMirrored.Outlined.FormatListBulleted else Icons.Outlined.GridView,
                                            null,
                                            modifier = Modifier.size(17.dp),
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Playlist content
                                if (libraryViewMode == "Grid") {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier.weight(1f).alpha(expansionProgress).padding(horizontal = 6.dp),
                                        contentPadding = PaddingValues(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        gridItems(sortedPlaylists) { playlist ->
                                            SidebarPlaylistGridEntry(playlist, colorScheme) { focusManager.clearFocus() }
                                        }
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.weight(1f).alpha(expansionProgress)) {
                                        items(sortedPlaylists) { playlist ->
                                            SidebarPlaylistEntry(playlist, colorScheme, onClick = { focusManager.clearFocus() })
                                        }
                                    }
                                }
                            } else if (!AppState.isEditingSidebar) {
                                // Collapsed sidebar: show playlist thumbnails in a vertical stack
                                val rawPlaylists = AppState.getOrderedPlaylists(
                                    AppState.librarySections["Library"]
                                        ?.filterIsInstance<PlaylistItem>()
                                        ?.filter { it.id !in AppState.hiddenPlaylistIds }
                                        ?: emptyList()
                                )
                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(rawPlaylists) { playlist ->
                                        CollapsedPlaylistThumb(playlist, colorScheme) {
                                            focusManager.clearFocus()
                                        }
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // Main Content
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() }) {
                        LaunchedEffect(pageKey) {
                            AppState.topBarScrollOffset = if (pageKey in setOf("home", "artist")) 0 else Int.MAX_VALUE
                        }

                        // Screens that don't use a full-bleed header get 48dp top inset
                        val useTopPadding = pageKey !in setOf("home", "artist")
                        Box(modifier = Modifier.fillMaxSize()
                            .then(if (useTopPadding) Modifier.padding(top = 48.dp) else Modifier)
                        ) {
                            com.metrolist.desktop.ui.components.PageTransition(
                                pageKey = pageKey
                            ) { key ->
                                when (key) {
                                    "signIn" -> EmbeddedSignInView(
                                        onAuthDataExtracted = { cookie, visitorData, dataSyncId -> AppState.updateAuth(cookie, visitorData, dataSyncId) },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    "settings" -> SettingsScreen(colorScheme)
                                    "integrations" -> IntegrationsScreen(colorScheme)
                                    "artist" -> {
                                        val artistId = AppState.selectedArtistId
                                        if (artistId != null) {
                                            ArtistScreen(colorScheme)
                                        } else {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("No artist selected")
                                            }
                                        }
                                    }
                                    "playlist" -> {
                                        val playlistId = AppState.selectedPlaylistId
                                        if (playlistId != null) {
                                            PlaylistScreen(colorScheme)
                                        } else {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("No playlist selected")
                                            }
                                        }
                                    }
                                    "album" -> {
                                        val albumId = AppState.selectedAlbumId
                                        if (albumId != null) {
                                            AlbumScreen(albumId, colorScheme)
                                        } else {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("No album selected")
                                            }
                                        }
                                    }
                                    "search" -> SearchResultsList(colorScheme)
                                    "localPlaylist" -> LocalPlaylistScreen(AppState.selectedLocalPlaylist ?: "top50", colorScheme)
                                    "home" -> HomeScreen(colorScheme)
                                    "library" -> LibraryScreen(AppState.librarySections, colorScheme)
                                    "history" -> HistoryScreen(colorScheme)
                                    "stats" -> StatsScreen(colorScheme)
                                    "together" -> TogetherScreen(colorScheme)
                                    else -> HomeScreen(colorScheme)
                                }
                            }
                        }

                        // Topbar overlaid over content area only (not sidebar)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = AppState.isExpanded,
                            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(600, easing = EaseOutQuart)) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(600, easing = EaseInQuart)) + fadeOut()
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
                                ExpandedPlayerView()
                            }
                        }

                        // Always rendered last = always on top of everything including expanded player
                        CustomTitleBar(
                            title = currentTitle,
                            colorScheme = colorScheme,
                            backgroundAlpha = topBarAlpha,
                            searchText = searchText,
                            onSearchChange = { searchText = it },
                            onClose = onClose,
                            onMinimize = onMinimize,
                            onMaximize = onMaximize
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = AppState.currentTrack != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    StandardBottomPlayer(colorScheme)
                }

                AddToPlaylistDialog()
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
    expansionProgress: Float,
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
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(SideRailCollapsedWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
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
            }
            
            Text(
                text = label,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .graphicsLayer {
                        alpha = expansionProgress
                        translationX = (1f - expansionProgress) * -20f
                    },
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
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
fun EditingPlaylistItem(
    playlist: PlaylistItem,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    colorScheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            url = playlist.thumbnail ?: "",
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp))
        )
        Text(
            playlist.title,
            modifier = Modifier.padding(start = 10.dp).weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = if (isVisible) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.3f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Checkbox(
            checked = isVisible,
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
fun SidebarPlaylistEntry(playlist: PlaylistItem, colorScheme: ColorScheme, onClick: () -> Unit = {}) {
    val isLikedMusic = playlist.title.contains("Liked", ignoreCase = true)
    val hoverIS = remember { MutableInteractionSource() }
    val isHovered by hoverIS.collectIsHoveredAsState()
    val hoverAlpha by animateFloatAsState(if (isHovered) 1f else 0f, label = "playlistHover")
    val thumbShape = RoundedCornerShape(6.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isHovered) colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent)
            .hoverable(hoverIS)
            .clickable {
                AppState.isExpanded = false
                AppState.fetchPlaylistData(playlist.id, playlist.thumbnail)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with hover play overlay
        Box(modifier = Modifier.size(48.dp)) {
            AsyncImage(
                url = playlist.thumbnail ?: "",
                modifier = Modifier.size(48.dp),
                shape = thumbShape
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(hoverAlpha)
                    .clip(thumbShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow, null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                playlist.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLikedMusic) FontWeight.Bold else FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLikedMusic) {
                    Icon(
                        Icons.Default.PushPin, null,
                        modifier = Modifier.size(11.dp).rotate(45f),
                        tint = colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(3.dp))
                }
                Text(
                    text = "Playlist" + (playlist.author?.let { " • $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SidebarPlaylistGridEntry(playlist: PlaylistItem, colorScheme: ColorScheme, onClick: () -> Unit = {}) {
    val hoverIS = remember { MutableInteractionSource() }
    val isHovered by hoverIS.collectIsHoveredAsState()
    val hoverAlpha by animateFloatAsState(if (isHovered) 1f else 0f, label = "gridHover")
    val thumbShape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isHovered) colorScheme.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent)
            .hoverable(hoverIS)
            .clickable {
                AppState.isExpanded = false
                AppState.fetchPlaylistData(playlist.id, playlist.thumbnail)
                onClick()
            }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            AsyncImage(
                url = playlist.thumbnail ?: "",
                modifier = Modifier.fillMaxSize(),
                shape = thumbShape
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(hoverAlpha)
                    .clip(thumbShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.PlayArrow, null, tint = colorScheme.onPrimaryContainer, modifier = Modifier.size(15.dp))
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            playlist.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CollapsedPlaylistThumb(playlist: PlaylistItem, colorScheme: ColorScheme, onClick: () -> Unit = {}) {
    val hoverIS = remember { MutableInteractionSource() }
    val isHovered by hoverIS.collectIsHoveredAsState()
    val hoverAlpha by animateFloatAsState(if (isHovered) 1f else 0f, label = "collapsedThumbHover")
    val thumbShape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .fillMaxWidth()
            .hoverable(hoverIS)
            .clickable {
                AppState.isExpanded = false
                AppState.fetchPlaylistData(playlist.id, playlist.thumbnail)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            AsyncImage(
                url = playlist.thumbnail ?: "",
                modifier = Modifier.fillMaxSize(),
                shape = thumbShape
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(hoverAlpha)
                    .clip(thumbShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.PlayArrow, null, tint = colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                }
            }
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

private fun saveWindowState(state: WindowState) {
    AppState.prefs.put("WINDOW_PLACEMENT", state.placement.name)
    val position = state.position
    if (position is WindowPosition.Absolute) {
        AppState.prefs.putInt("WINDOW_X", position.x.value.toInt())
        AppState.prefs.putInt("WINDOW_Y", position.y.value.toInt())
    }
    AppState.prefs.putInt("WINDOW_WIDTH", state.size.width.value.toInt())
    AppState.prefs.putInt("WINDOW_HEIGHT", state.size.height.value.toInt())
    AppState.prefs.flush()
}

fun main() {
    Platform.startup {}
    
    LastFM.initialize(
        apiKey = BuildConfig.LASTFM_API_KEY,
        secret = BuildConfig.LASTFM_SECRET
    )
    
    LastFM.sessionKey = AppState.prefs.get("LASTFM_SESSION", null)
    AppState.loadSession()
    
    application {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screens = ge.screenDevices
        
        val savedX = AppState.prefs.getInt("WINDOW_X", 100)
        val savedY = AppState.prefs.getInt("WINDOW_Y", 100)
        val savedWidth = AppState.prefs.getInt("WINDOW_WIDTH", 1280)
        val savedHeight = AppState.prefs.getInt("WINDOW_HEIGHT", 800)
        
        var targetScreenBounds: Rectangle? = null
        for (screen in screens) {
            val bounds = screen.defaultConfiguration.bounds
            if (bounds.contains(savedX, savedY)) {
                targetScreenBounds = bounds
                break
            }
        }
        
        val finalX = targetScreenBounds?.let { savedX } ?: 100
        val finalY = targetScreenBounds?.let { savedY } ?: 100

        val windowPlacement = try {
            WindowPlacement.valueOf(AppState.prefs.get("WINDOW_PLACEMENT", WindowPlacement.Maximized.name))
        } catch (_: Exception) {
            WindowPlacement.Maximized
        }
        
        val windowState = rememberWindowState(
            placement = windowPlacement,
            position = WindowPosition(finalX.dp, finalY.dp),
            size = DpSize(savedWidth.dp, savedHeight.dp)
        )

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
                saveWindowState(windowState)
                AppState.player.release()
                exitApplication()
            },
            title = "Metrolist",
            state = windowState,
            undecorated = true,
            transparent = true,
            icon = dynamicIcon
        ) {
            LaunchedEffect(Unit) {
                try {
                    val geLocal = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    val gd = geLocal.defaultScreenDevice
                    val gc = gd.defaultConfiguration
                    val insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc)
                    val bounds = gc.bounds
                    val maxBounds = Rectangle(
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
                    saveWindowState(windowState)
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

            MiniplayerWindow()
        }
    }
}
