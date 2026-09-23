package com.connectlens.app.feature.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectlens.app.core.common.PhoneNumberUtils
import com.connectlens.app.core.common.TimeUtils
import com.connectlens.app.core.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: Long?,
    phoneNumber: String?,
    onNavigateBack: () -> Unit,
    viewModel: ContactDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = if (uiState is ContactDetailUiState.Success) {
                        (uiState as ContactDetailUiState.Success).stats.displayName
                    } else {
                        "Contact Details"
                    }
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is ContactDetailUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is ContactDetailUiState.Error -> ErrorState(message = state.message, onRetry = {}, modifier = Modifier.fillMaxSize())
                is ContactDetailUiState.NotFound -> EmptyState(message = "Contact not found", modifier = Modifier.fillMaxSize())
                is ContactDetailUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AvatarView(
                                    name = state.stats.displayName,
                                    size = 72.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = state.stats.displayName,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                if (state.stats.phoneNumber != null) {
                                    Text(
                                        text = PhoneNumberUtils.maskNumber(state.stats.phoneNumber),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            TimeRangeSelector(
                                selectedRange = selectedTimeRange,
                                onRangeSelected = viewModel::selectTimeRange,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryCard(title = "Total Calls", value = state.stats.totalCalls.toString(), modifier = Modifier.weight(1f))
                                    SummaryCard(title = "Avg Duration", value = TimeUtils.formatDuration(state.stats.averageDurationSeconds), modifier = Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryCard(title = "Incoming", value = state.stats.incomingCalls.toString(), modifier = Modifier.weight(1f))
                                    SummaryCard(title = "Outgoing", value = state.stats.outgoingCalls.toString(), modifier = Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryCard(title = "Missed", value = state.stats.missedCalls.toString(), modifier = Modifier.weight(1f))
                                    SummaryCard(title = "Longest Call", value = TimeUtils.formatDuration(state.stats.longestCallSeconds), modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        item {
                            Text("Call Activity", style = MaterialTheme.typography.titleLarge)
                            AnalyticsBarChart(data = state.stats.callsByDay, modifier = Modifier.height(200.dp))
                        }

                        item {
                            Text("Call Types", style = MaterialTheme.typography.titleLarge)
                            CallTypeDonutChart(
                                incoming = state.stats.incomingCalls,
                                outgoing = state.stats.outgoingCalls,
                                missed = state.stats.missedCalls,
                                modifier = Modifier.height(200.dp)
                            )
                        }

                        item {
                            Text("Call History", style = MaterialTheme.typography.titleLarge)
                        }

                        if (state.calls.isEmpty()) {
                            item { EmptyState(message = "No calls found for this period", modifier = Modifier.fillMaxWidth()) }
                        } else {
                            items(state.calls) { call ->
                                CallHistoryRow(callRecord = call)
                            }
                        }
                    }
                }
            }
        }
    }
}
