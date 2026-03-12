@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
package com.metrolist.desktop.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowScope
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.state.GlobalYouTubeRepository
import com.metrolist.shared.api.innertube.models.AccountInfo

@Composable
fun WindowScope.CustomTitleBar(
    title: String,
    colorScheme: ColorScheme,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit
) {
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    val searchWidth by animateDpAsState(
        targetValue = if (isSearchFocused) 420.dp else 280.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    WindowDraggableArea {
        Surface(color = colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Title
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { 
                                AppState.showSettings = false 
                                AppState.showSignIn = false
                                AppState.showIntegrations = false
                                AppState.isExpanded = false
                            },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Middle side: Search Bar (more centered now)
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
                                .onFocusChanged { isSearchFocused = it.isFocused }
                                .onKeyEvent {
                                    if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                                        AppState.isExpanded = false
                                        AppState.addSearchQuery(searchText)
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
                        var showAuthDialog by remember { mutableStateOf(false) }
                        if (AppState.isSignedIn) {
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                AsyncImage(
                                    url = AppState.profilePicUrl ?: "", 
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable { expanded = true }, 
                                    shape = CircleShape
                                )
                                
                                DropdownMenu(
                                    expanded = expanded, 
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.width(300.dp).background(colorScheme.surfaceContainerHigh)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            url = AppState.profilePicUrl ?: "",
                                            modifier = Modifier.size(48.dp).clip(CircleShape),
                                            shape = CircleShape
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = GlobalYouTubeRepository.instance.lastAccountInfo?.name ?: "User",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (GlobalYouTubeRepository.instance.lastAccountInfo?.email != null) {
                                                Text(
                                                    text = GlobalYouTubeRepository.instance.lastAccountInfo?.email!!,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colorScheme.outlineVariant)
                                    
                                    DropdownMenuItem(
                                        text = { Text("Settings") }, 
                                        onClick = { 
                                            expanded = false
                                            AppState.showSettings = true
                                            AppState.showSignIn = false
                                            AppState.showIntegrations = false
                                            AppState.isExpanded = false
                                        }, 
                                        leadingIcon = { Icon(Icons.Outlined.Settings, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Integrations") }, 
                                        onClick = { 
                                            expanded = false
                                            AppState.showIntegrations = true
                                            AppState.showSettings = false
                                            AppState.showSignIn = false
                                            AppState.isExpanded = false
                                        }, 
                                        leadingIcon = { Icon(Icons.Outlined.Extension, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Authentication Data") },
                                        onClick = {
                                            expanded = false
                                            showAuthDialog = true
                                        },
                                        leadingIcon = { Icon(Icons.Outlined.VpnKey, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sign out") }, 
                                        onClick = { 
                                            expanded = false
                                            AppState.signOut() 
                                        },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null) } 
                                    )
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
                                            }) {
                                                Text("Save")
                                            }
                                        },
                                        dismissButton = {
                                            OutlinedButton(onClick = { showAuthDialog = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            Button(onClick = { 
                                AppState.showSignIn = true; 
                                AppState.showSettings = false; 
                                AppState.showIntegrations = false;
                                AppState.isExpanded = false
                            }, shape = CircleShape, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {
                                Text("Sign in", fontSize = 12.sp)
                            }
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
