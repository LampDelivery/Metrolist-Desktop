package com.metrolist.desktop.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <E> ChipsRow(
    chips: List<Pair<E, String>>,
    currentValue: E?,
    onValueUpdate: (E) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Spacer(Modifier.width(32.dp))

        chips.forEach { (value, label) ->
            val isSelected = currentValue == value
            // Keep pills round even when selected (like Android app)
            val cornerRadius = 50.dp

            FilterChip(
                label = { Text(label) },
                selected = isSelected,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                onClick = { onValueUpdate(value) },
                shape = RoundedCornerShape(cornerRadius),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    selectedBorderWidth = 0.dp,
                    borderWidth = 1.dp,
                ),
            )

            Spacer(Modifier.width(8.dp))
        }

        Spacer(Modifier.width(24.dp))
    }
}
