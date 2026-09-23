package com.connectlens.app.feature.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectlens.app.core.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToContact: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ConnectLens", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Local Analytics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    if (uiState is DashboardUiState.Success) {
                        val state = uiState as DashboardUiState.Success
                        if (state.hasCallPermission && state.hasContactPermission) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Private") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TimeRangeSelector(
                selected = selectedTimeRange,
                onSelect = viewModel::selectTimeRange,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }
                is DashboardUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is DashboardUiState.PermissionRequired -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Call Log Permission Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "ConnectLens analyzes your call logs 100% locally on your device without sending any data over the internet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALL_LOG,
                                    Manifest.permission.READ_CONTACTS
                                )
                            )
                        }) {
                            Text("Grant Permission")
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // KPI Summary Grid
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryCard(
                                        title = "Total Calls",
                                        value = state.stats.totalCalls.toString(),
                                        subtitle = "${state.stats.incomingCalls} in • ${state.stats.outgoingCalls} out",
                                        icon = Icons.Default.Call,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryCard(
                                        title = "Talk Time",
                                        value = state.stats.formattedTotalDuration,
                                        subtitle = "Avg ${state.stats.formattedAverageDuration}/call",
                                        icon = Icons.Default.AccessTime,
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryCard(
                                        title = "Peak Activity",
                                        value = state.stats.peakWindow,
                                        subtitle = "Busiest window",
                                        icon = Icons.Default.Schedule,
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryCard(
                                        title = "Unique Contacts",
                                        value = state.stats.uniqueContactsCount.toString(),
                                        subtitle = "Interacted",
                                        icon = Icons.Default.People,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Call Activity Interactive Canvas Chart
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Daily Call Activity",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Tap any bar to inspect daily call total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    AnalyticsBarChart(
                                        data = state.stats.callsByDay,
                                        label = "Calls",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Donut Distribution Card
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Call Type Breakdown",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    CallTypeDonutChart(
                                        incoming = state.stats.incomingCalls,
                                        outgoing = state.stats.outgoingCalls,
                                        missed = state.stats.missedCalls,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Top Contacts Header
                        item {
                            Text(
                                "Top Contacts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.stats.topContacts.isEmpty()) {
                            item {
                                Text(
                                    "No contact records available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(state.stats.topContacts) { contactStats ->
                                ContactRow(
                                    stats = contactStats,
                                    onClick = { contactStats.contact?.id?.let { onNavigateToContact(it) } },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
