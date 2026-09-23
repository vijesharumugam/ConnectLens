package com.connectlens.app.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.connectlens.app.core.common.PhoneNumberUtils
import com.connectlens.app.core.common.TimeUtils
import com.connectlens.app.core.designsystem.ChartIncoming
import com.connectlens.app.core.designsystem.ChartMissed
import com.connectlens.app.core.designsystem.ChartOutgoing
import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.CallType

/**
 * A single row in the Call History list.
 * Shows contact/number, call type indicator, timestamp, and duration.
 */
@Composable
fun CallHistoryRow(
    record: CallRecord? = null,
    callRecord: CallRecord? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val targetRecord = record ?: callRecord ?: return
    val (icon, iconTint, typeLabel) = callTypeVisuals(targetRecord.type)
    val hasContactName = !targetRecord.contactName.isNullOrBlank()
    val rawNumber = targetRecord.number.ifBlank { null }
    val displayName = if (hasContactName) targetRecord.contactName!! else (rawNumber ?: "Unknown number")

    val subtitleText = buildString {
        if (hasContactName && rawNumber != null) {
            append(rawNumber)
            append(" • ")
        }
        append(TimeUtils.formatRelativeDate(targetRecord.timestamp))
    }

    val description = "$typeLabel call ${if (hasContactName) "with ${targetRecord.contactName} (${rawNumber ?: ""})" else "with ${rawNumber ?: "Unknown"}"}, " +
            "duration ${TimeUtils.formatDuration(targetRecord.durationSeconds)}"

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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Call type icon in a small badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = typeLabel,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Name, phone number, and timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text      = displayName,
                    style     = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                Text(
                    text  = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Duration (only for non-missed calls)
            Column(horizontalAlignment = Alignment.End) {
                if (targetRecord.durationSeconds > 0) {
                    Text(
                        text  = TimeUtils.formatDurationCompact(targetRecord.durationSeconds),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text  = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = iconTint.copy(alpha = 0.8f)
                    )
                }
            }

            if (!rawNumber.isNullOrBlank()) {
                IconButton(
                    onClick = { PhoneNumberUtils.launchDialIntent(context, rawNumber) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call $displayName",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    HorizontalDivider(
        modifier  = Modifier.padding(start = 68.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

private data class CallTypeVisuals(
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val label: String
)

private fun callTypeVisuals(type: CallType): CallTypeVisuals = when (type) {
    CallType.INCOMING -> CallTypeVisuals(Icons.Default.CallReceived, ChartIncoming, "Incoming")
    CallType.OUTGOING -> CallTypeVisuals(Icons.Default.CallMade, ChartOutgoing, "Outgoing")
    CallType.MISSED   -> CallTypeVisuals(Icons.Default.CallMissed, ChartMissed, "Missed")
    CallType.REJECTED -> CallTypeVisuals(Icons.Default.CallEnd, ChartMissed, "Rejected")
    CallType.BLOCKED  -> CallTypeVisuals(Icons.Default.Block, androidx.compose.ui.graphics.Color.Gray, "Blocked")
    CallType.UNKNOWN  -> CallTypeVisuals(Icons.Default.Call, androidx.compose.ui.graphics.Color.Gray, "Unknown")
}
