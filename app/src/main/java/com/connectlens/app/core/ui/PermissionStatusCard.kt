package com.connectlens.app.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A card showing the granted/denied status of a single Android permission.
 * Shows a green check or yellow warning icon plus a description and optional CTA.
 */
@Composable
fun PermissionStatusCard(
    title: String,
    description: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val containerColor = if (isGranted)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.errorContainer

    val contentColor = if (isGranted)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onErrorContainer

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = if (isGranted) "Granted" else "Not granted",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.75f)
                )
            }

            if (!isGranted && actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel, color = contentColor)
                }
            }
        }
    }
}
