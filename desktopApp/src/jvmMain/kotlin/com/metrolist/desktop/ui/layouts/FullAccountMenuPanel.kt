package com.metrolist.desktop.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.metrolist.desktop.state.AppState
import com.metrolist.desktop.ui.components.AsyncImage
import com.metrolist.shared.state.GlobalYouTubeRepository

/**
 * Full account menu panel (replicating TopBar functionality)
 */
@Composable
fun FullAccountMenuPanel(
    colorScheme: ColorScheme,
    onDismiss: () -> Unit
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
                                    if (!AppState.isSignedIn) { /* Show auth dialog */ }
                                    else if (!showToken) showToken = true
                                    else { /* Show auth dialog */ }
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
                    }
                }
            }
        }
    }
}