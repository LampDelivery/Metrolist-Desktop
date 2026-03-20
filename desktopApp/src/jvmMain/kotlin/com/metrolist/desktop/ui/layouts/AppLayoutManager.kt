package com.metrolist.desktop.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.metrolist.desktop.state.AppLayout
import com.metrolist.desktop.state.AppState

/**
 * Layout manager for Material 3 Expressive layouts
 * 
 * Note: Actual layout rendering is handled in Main.kt based on AppState.appLayout.
 * These composable functions are used for layout previews and mockups.
 * 
 * Express  - Clean foundation (default)
 * Comfortable - Expanded sidebar, no top bar, floating controls
 * Focused  - Player in top bar, search in sidebar
 * Dynamic  - Player in top bar, search in top left
 */
@Composable
fun AppLayoutManager(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    // Main layout rendering is handled in Main.kt
    // This just renders the content area
    Box(modifier = Modifier.fillMaxSize()) {
        content()
    }
}

// ============================================
// Layout Mockup Components (for settings previews)
// ============================================

@Composable
fun ExpressLayoutMockup(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(colorScheme.surfaceContainerHigh, RoundedCornerShape(2.dp))
        )
        
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Sidebar
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainer, RoundedCornerShape(2.dp))
            )
            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
        
        // Player bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(colorScheme.surfaceContainerHigh, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun ComfortableLayoutMockup(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Expanded sidebar with search at top
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search bar placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                )
                // Navigation area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(colorScheme.surfaceContainer, RoundedCornerShape(2.dp))
                )
            }
            // Content with floating buttons at top
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                )
                // Floating controls indicator
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(colorScheme.surfaceContainerHigh, RoundedCornerShape(1.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(colorScheme.surfaceContainerHigh, RoundedCornerShape(1.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(colorScheme.error, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
        
        // Player bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(colorScheme.surfaceContainerHigh, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun FocusedLayoutMockup(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top bar with player controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(colorScheme.surfaceContainerHigh, RoundedCornerShape(2.dp))
        )
        
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Expanded sidebar with search below player
            Column(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Player info placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                )
                // Search bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
                )
                // Navigation area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(colorScheme.surfaceContainer, RoundedCornerShape(2.dp))
                )
            }
            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun DynamicLayoutMockup(colorScheme: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Search in top left corner
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(16.dp)
                    .background(colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
            )
            // Player controls in top bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(colorScheme.surfaceContainerHigh, RoundedCornerShape(2.dp))
            )
        }
        
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Expanded sidebar
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceContainer, RoundedCornerShape(2.dp))
            )
            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    }
}
