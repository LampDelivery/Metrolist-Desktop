@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
package com.metrolist.desktop.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterNone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Upgrade
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.WindowScope
import com.metrolist.desktop.state.AppState
import com.metrolist.shared.state.GlobalYouTubeRepository

@Composable
fun WindowScope.CustomTitleBar(
    title: String,
    colorScheme: ColorScheme,
    backgroundAlpha: Float = 1f,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    isSidebarExpanded: Boolean = true,
    onToggleSidebar: () -> Unit = {},
    isEditingSidebar: Boolean = false,
    onToggleEdit: () -> Unit = {}
) {
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val searchWidth by animateDpAsState(
        targetValue = if (isSearchFocused) 420.dp else 280.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    WindowDraggableArea {
        val frosted = AppState.fullGradientBackground
        val bgColor = if (frosted) Color.White.copy(alpha = 0.10f) else colorScheme.surfaceContainer.copy(alpha = backgroundAlpha)
        Surface(
            color = bgColor,
            modifier = Modifier.fillMaxWidth().height(48.dp)
                .then(if (frosted) Modifier.blur(32.dp) else Modifier)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Menu button + Edit button (only when sidebar expanded)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Toggle sidebar menu button
                    IconButton(
                        onClick = onToggleSidebar,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Toggle Sidebar",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Edit sidebar button (only show when expanded)
                    if (isSidebarExpanded) {
                        IconButton(
                            onClick = onToggleEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (isEditingSidebar) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditingSidebar) "Done editing" else "Edit sidebar",
                                tint = if (isEditingSidebar) colorScheme.primary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Middle: Search Bar
                Box(Modifier.weight(2f), contentAlignment = Alignment.Center) {
                    Box {
                        BasicTextField(
                            value = searchText, 
                            onValueChange = onSearchChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurface),
                            modifier = Modifier
                                .width(searchWidth)
                                .height(32.dp)
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .onFocusChanged {
                                    isSearchFocused = it.isFocused
                                    onSearchFocusChange(it.isFocused)
                                }
                                .onKeyEvent {
                                    if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                                        AppState.isExpanded = false
                                        AppState.addSearchQuery(searchText)
                                        AppState.search(searchText)
                                        focusManager.clearFocus()
                                        false
                                    } else false
                                },
                            singleLine = true,
                            cursorBrush = SolidColor(colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                AppState.isExpanded = false
                                AppState.addSearchQuery(searchText)
                                AppState.search(searchText)
                                focusManager.clearFocus()
                            }),
                            decorationBox = { innerTextField ->
                                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp), tint = colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                        if (searchText.isEmpty()) Text("Search music...", color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                                        innerTextField()
                                    }
                                    if (searchText.isNotEmpty()) {
                                        Icon(
                                            Icons.Default.Close, 
                                            null, 
                                            modifier = Modifier.size(16.dp).clickable { onSearchChange("") }, 
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = isSearchFocused && AppState.searchHistory.isNotEmpty(),
                            onDismissRequest = { isSearchFocused = false }, 
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                            modifier = Modifier.width(searchWidth).background(colorScheme.surfaceContainerHigh)
                        ) {
                            AppState.searchHistory.forEach { query ->
                                DropdownMenuItem(
                                    text = { Text(query, style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        AppState.isExpanded = false
                                        onSearchChange(query)
                                        AppState.addSearchQuery(query)
                                        AppState.search(query)
                                        focusManager.clearFocus()
                                    },
                                    leadingIcon = { Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        IconButton(onClick = { AppState.removeSearchQuery(query) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Right side: Profile & Window Controls
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        var showAccountPanel by remember { mutableStateOf(false) }
                        var showAuthDialog by remember { mutableStateOf(false) }

                        Box {
                            if (AppState.isSignedIn) {
                                AsyncImage(
                                    url = AppState.profilePicUrl ?: "",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { showAccountPanel = !showAccountPanel },
                                    shape = CircleShape
                                )
                            } else {
                                IconButton(
                                    onClick = { showAccountPanel = !showAccountPanel },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AccountCircle,
                                        null,
                                        tint = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            if (showAccountPanel) {
                                AccountMenuPanel(
                                    colorScheme = colorScheme,
                                    onDismiss = { showAccountPanel = false },
                                    onShowAuthDialog = { showAccountPanel = false; showAuthDialog = true }
                                )
                            }
                        }

                        if (showAuthDialog) {
                            var cookieText by remember { mutableStateOf(AppState.prefs.get("COOKIES", "")) }
                            var visitorDataText by remember { mutableStateOf(AppState.prefs.get("VISITOR_DATA", "")) }
                            var dataSyncIdText by remember { mutableStateOf(AppState.prefs.get("DATASYNC_ID", "")) }
                            var errorText by remember { mutableStateOf("") }
                            AlertDialog(
                                onDismissRequest = { showAuthDialog = false },
                                title = { Text("Edit Authentication Data") },
                                text = {
                                    Column {
                                        OutlinedTextField(
                                            value = cookieText,
                                            onValueChange = { cookieText = it },
                                            label = { Text("Cookie (must contain SAPISID)") },
                                            singleLine = false,
                                            modifier = Modifier.fillMaxWidth().height(80.dp)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = visitorDataText,
                                            onValueChange = { visitorDataText = it },
                                            label = { Text("Visitor Data") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = dataSyncIdText,
                                            onValueChange = { dataSyncIdText = it },
                                            label = { Text("DataSync ID") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (errorText.isNotEmpty()) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(errorText, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        if (!cookieText.contains("SAPISID")) {
                                            errorText = "Cookie must contain SAPISID."
                                            return@Button
                                        }
                                        AppState.updateAuth(cookieText, visitorDataText, dataSyncIdText)
                                        showAuthDialog = false
                                    }) { Text("Save") }
                                },
                                dismissButton = {
                                    OutlinedButton(onClick = { showAuthDialog = false }) { Text("Cancel") }
                                }
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        WindowControlButton(Icons.Default.HorizontalRule, onMinimize, colorScheme.onSurfaceVariant)
                        WindowControlButton(if (AppState.isMaximized) Icons.Default.FilterNone else Icons.Default.CropSquare, onMaximize, colorScheme.onSurfaceVariant)
                        WindowControlButton(Icons.Default.Close, onClose, colorScheme.error, isClose = true)
                    }
                }
            }
        }
    }
}

// ── Account Menu Panel ────────────────────────────────────────────────────

@Composable
private fun AccountMenuPanel(
    colorScheme: ColorScheme,
    onDismiss: () -> Unit,
    onShowAuthDialog: () -> Unit
) {
    var showToken by remember { mutableStateOf(false) }

    Popup(
        alignment = Alignment.BottomEnd,
        offset = IntOffset(0, 4),
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.PopupProperties(focusable = false)
    ) {
        Card(
            modifier = Modifier
                .width(360.dp)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Metrolist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                    }
                }

                // Account card
                Surface(shape = RoundedCornerShape(12.dp), color = colorScheme.surfaceContainerHighest) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (AppState.isSignedIn && AppState.profilePicUrl != null) {
                            AsyncImage(
                                url = AppState.profilePicUrl ?: "",
                                modifier = Modifier.size(46.dp).clip(CircleShape),
                                shape = CircleShape
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(46.dp).background(colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.AccountCircle, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            if (AppState.isSignedIn) {
                                Text(
                                    text = GlobalYouTubeRepository.instance.lastAccountInfo?.name ?: "User",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val email = GlobalYouTubeRepository.instance.lastAccountInfo?.email
                                if (email != null) {
                                    Text(text = email, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            } else {
                                Text("Not signed in", style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                            }
                        }
                        if (AppState.isSignedIn) {
                            OutlinedButton(
                                onClick = { onDismiss(); AppState.signOut() },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text("Log out", style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            FilledTonalButton(
                                onClick = {
                                    onDismiss()
                                    AppState.showSignIn = true
                                    AppState.showSettings = false
                                    AppState.showIntegrations = false
                                    AppState.isExpanded = false
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text("Sign in", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Token + toggles card
                Surface(shape = RoundedCornerShape(12.dp), color = colorScheme.surfaceContainer) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!AppState.isSignedIn) onShowAuthDialog()
                                    else if (!showToken) showToken = true
                                    else onShowAuthDialog()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.VpnKey, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Text(
                                text = when {
                                    !AppState.isSignedIn -> "Login with token"
                                    showToken -> "Tap again to copy or edit"
                                    else -> "Tap to show token"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val enabledAlpha = if (AppState.isSignedIn) 1f else 0.4f
                            Icon(Icons.Outlined.Cached, null, tint = colorScheme.onSurfaceVariant.copy(alpha = enabledAlpha), modifier = Modifier.size(20.dp))
                            Text("More content", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = colorScheme.onSurface.copy(alpha = enabledAlpha))
                            Switch(
                                checked = AppState.useLoginForBrowse,
                                onCheckedChange = { if (AppState.isSignedIn) AppState.toggleUseLoginForBrowse(it) },
                                enabled = AppState.isSignedIn,
                                thumbContent = {
                                    Icon(
                                        imageVector = if (AppState.useLoginForBrowse) Icons.Filled.Check else Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        }
                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val enabledAlpha = if (AppState.isSignedIn) 1f else 0.4f
                            Icon(Icons.Outlined.Sync, null, tint = colorScheme.onSurfaceVariant.copy(alpha = enabledAlpha), modifier = Modifier.size(20.dp))
                            Text("Auto sync with account", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = colorScheme.onSurface.copy(alpha = enabledAlpha))
                            Switch(
                                checked = AppState.ytmSync,
                                onCheckedChange = { if (AppState.isSignedIn) AppState.toggleYtmSync(it) },
                                enabled = AppState.isSignedIn,
                                thumbContent = {
                                    Icon(
                                        imageVector = if (AppState.ytmSync) Icons.Filled.Check else Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        }
                    }
                }

                // Navigation card
                Surface(shape = RoundedCornerShape(12.dp), color = colorScheme.surfaceContainer) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onDismiss(); AppState.showIntegrations = true; AppState.showSettings = false; AppState.showSignIn = false; AppState.isExpanded = false }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.Extension, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Text("Integrations", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { onDismiss(); AppState.showSettings = true; AppState.showSignIn = false; AppState.showIntegrations = false; AppState.isExpanded = false }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.Settings, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Text("Settings", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        val pendingUpdate = AppState.availableUpdate
                        if (pendingUpdate != null) {
                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        try { java.awt.Desktop.getDesktop().browse(java.net.URI("https://github.com/LampDelivery/Metrolist-Desktop/releases/latest")) } catch (_: Exception) {}
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Outlined.Upgrade, null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
                                Text("v$pendingUpdate available", style = MaterialTheme.typography.bodyMedium, color = colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                Icon(Icons.AutoMirrored.Outlined.NavigateNext, null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WindowControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color,
    isClose: Boolean = false
) {
    var isHovered by remember { mutableStateOf(false) }
    val backgroundColor = if (isHovered) {
        if (isClose) Color.Red.copy(alpha = 0.8f) else Color.LightGray.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }
    val contentColor = if (isHovered && isClose) Color.White else color

    Box(
        modifier = Modifier
            .size(46.dp, 32.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            modifier = Modifier.size(16.dp), 
            tint = contentColor
        )
    }
}
