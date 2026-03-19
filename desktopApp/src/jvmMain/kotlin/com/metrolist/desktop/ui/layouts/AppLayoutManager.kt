package com.metrolist.desktop.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppLayout
import com.metrolist.desktop.state.AppState

/**
 * Main layout manager that renders the appropriate layout based on AppState.appLayout
 */
@Composable
fun AppLayoutManager(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    when (AppState.appLayout) {
        AppLayout.CANVAS -> CanvasLayout(colorScheme, content)
        AppLayout.STUDIO -> StudioLayout(colorScheme, content)
        AppLayout.STAGE -> StageLayout(colorScheme, content)
        AppLayout.FLOW -> FlowLayout(colorScheme, content)
    }
}

/**
 * Canvas Layout - Clean foundation with sidebar + main content + bottom player (default)
 */
@Composable
private fun CanvasLayout(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Standard top bar (48dp like current)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(colorScheme.surfaceContainerHigh)
        ) {
            // TopBar content will be placed here (existing TopBar component)
        }

        Row(modifier = Modifier.weight(1f)) {
            // Standard sidebar (240dp like current)
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest)
            ) {
                // Standard navigation sidebar
            }

            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
            ) {
                content()
            }
        }

        // Standard bottom player bar (72dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(colorScheme.surfaceContainerHigh)
        ) {
            // Player controls
        }
    }
}

/**
 * Studio Layout - Expanded workspace with wide sidebar, search at top, floating controls
 */
@Composable
private fun StudioLayout(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with floating action buttons (reduced height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Floating action buttons instead of standard top bar content
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = { /* Settings */ },
                    modifier = Modifier.size(32.dp),
                    containerColor = colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Expanded sidebar with search at top (320dp instead of 240dp)
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search bar at top of sidebar
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Search library...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Navigation items below
                // Library navigation will be placed here
            }

            // Main content (reduced due to wider sidebar)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
            ) {
                content()
            }
        }

        // Standard bottom player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(colorScheme.surfaceContainerHigh)
        ) {
            // Player controls
        }
    }
}

/**
 * Stage Layout - Performance focused with player in top bar, search in sidebar below controls
 */
@Composable
private fun StageLayout(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with integrated player controls (increased height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App title/navigation on left
                Text(
                    "Metrolist",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.onSurface
                )

                // Player controls on right side of top bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current track info
                    Column {
                        Text(
                            "Current Track",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                        Text(
                            "Artist Name",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    // Play controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Outlined.Search, // Replace with skip previous
                                contentDescription = "Previous"
                            )
                        }
                        FilledIconButton(onClick = { }) {
                            Icon(
                                Icons.Outlined.Search, // Replace with play/pause
                                contentDescription = "Play/Pause"
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Outlined.Search, // Replace with skip next
                                contentDescription = "Next"
                            )
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Sidebar with player controls section + search below
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Player control section in sidebar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Now Playing Controls",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurface
                        )
                        // Volume, shuffle, repeat controls here
                    }
                }

                // Search below player controls
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Search...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Library navigation
            }

            // Main content (more space since no bottom player)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
            ) {
                content()
            }
        }

        // Minimal bottom bar (since player is in top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(colorScheme.surfaceContainerLow)
        ) {
            // Just a progress indicator or minimal status
        }
    }
}

/**
 * Flow Layout - Streamlined experience with external player controls, clean sidebar
 */
@Composable
private fun FlowLayout(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with external player controls (similar to Stage but cleaner)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(colorScheme.surfaceContainerHigh)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Metrolist",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colorScheme.onSurface
                )

                // Streamlined player controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compact track info
                    Text(
                        "Current Track - Artist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface
                    )

                    // Minimal controls
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Previous",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        FilledIconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Play",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Next",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // Clean sidebar (no player controls, just search + library)
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainerLowest)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search at top
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Search library...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Clean library navigation (no player controls section)
            }

            // Streamlined content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surface)
            ) {
                content()
            }
        }

        // No bottom player bar (all controls are external in top bar)
    }
}