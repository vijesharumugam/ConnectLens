package com.connectlens.app.core.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.connectlens.app.domain.model.TimeRange

/**
 * A horizontally scrollable row of filter chips, one per [TimeRange] value.
 * The selected chip is filled; others are outlined.
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
            .padding(horizontal = 16.dp)
            .semantics { contentDescription = "Time range filter, selected: ${activeSelected.displayName}" },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = (range == activeSelected),
                onClick  = { activeOnSelect(range) },
                label    = { Text(range.displayName, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}
