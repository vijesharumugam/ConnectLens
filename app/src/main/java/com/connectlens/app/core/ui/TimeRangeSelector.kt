package com.connectlens.app.core.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.connectlens.app.domain.model.TimeRange

/**
 * A sleek, horizontally scrollable row of pill filter chips for selecting [TimeRange].
 */
@Composable
fun TimeRangeSelector(
    selected: TimeRange? = null,
    onSelect: ((TimeRange) -> Unit)? = null,
    selectedRange: TimeRange? = null,
    onRangeSelected: ((TimeRange) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeSelected = selected ?: selectedRange ?: TimeRange.LAST_30_DAYS
    val activeOnSelect = onSelect ?: onRangeSelected ?: {}

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .semantics { contentDescription = "Time range filter, selected: ${activeSelected.displayName}" },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = (range == activeSelected)
            FilterChip(
                selected = isSelected,
                onClick  = { activeOnSelect(range) },
                shape    = CircleShape,
                label    = {
                    Text(
                        range.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    containerColor         = MaterialTheme.colorScheme.surfaceContainerLow,
                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border   = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
