package com.connectlens.app.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.connectlens.app.core.common.TimeUtils
import com.connectlens.app.domain.model.ContactStats

/**
 * A row representing a contact and their call summary.
 * Used in the Dashboard "Top Contacts" list and the Contact List screen.
 */
@Composable
fun ContactRow(
    stats: ContactStats,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val description = "Contact: ${stats.displayName}, " +
            "${stats.totalCalls} calls, ${stats.formattedTotalDuration}"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            AvatarView(name = stats.displayName, size = 44.dp)

            // Name + last call
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text      = stats.displayName,
                    style     = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                stats.mostRecentCall?.let { instant ->
                    Text(
                        text  = TimeUtils.formatRelativeDate(instant),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats chips
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text      = "${stats.totalCalls} calls",
                    style     = MaterialTheme.typography.labelMedium,
                    color     = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = stats.formattedTotalDuration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    HorizontalDivider(
        modifier  = Modifier.padding(start = 72.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
