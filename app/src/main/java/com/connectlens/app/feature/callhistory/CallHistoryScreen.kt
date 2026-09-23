package com.connectlens.app.feature.callhistory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectlens.app.core.ui.*
import com.connectlens.app.domain.model.CallType
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onNavigateBack: () -> Unit,
    onRequestPermission: () -> Unit,
    viewModel: CallHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val sortNewest by viewModel.sortNewest.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = viewModel::updateSearch,
                            placeholder = { Text("Search calls...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Call History")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.updateSearch("")
                    }) {
                        Icon(
                            if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is CallHistoryUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is CallHistoryUiState.Error -> ErrorState(message = state.message, onRetry = {}, modifier = Modifier.fillMaxSize())
                is CallHistoryUiState.Success -> {
                    if (!state.hasPermission) {
                        Card(
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Permission Required", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("We need permission to read your call logs to show history.")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onRequestPermission) {
                                    Text("Grant Permission")
                                }
                            }
                        }
                    } else {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = viewModel::toggleSort) {
                                    Text(if (sortNewest) "Sort: Newest First" else "Sort: Oldest First")
                                }
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                FilterChip(
                                    selected = filterType == null,
                                    onClick = { viewModel.setFilter(null) },
                                    label = { Text("All") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = filterType == CallType.INCOMING,
                                    onClick = { viewModel.setFilter(CallType.INCOMING) },
                                    label = { Text("Incoming") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = filterType == CallType.OUTGOING,
                                    onClick = { viewModel.setFilter(CallType.OUTGOING) },
                                    label = { Text("Outgoing") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = filterType == CallType.MISSED,
                                    onClick = { viewModel.setFilter(CallType.MISSED) },
                                    label = { Text("Missed") }
                                )
                            }
                            
                            TimeRangeSelector(
                                selectedRange = selectedTimeRange,
                                onRangeSelected = viewModel::selectTimeRange,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            
                            if (state.calls.isEmpty()) {
                                EmptyState(message = "No calls found", modifier = Modifier.fillMaxSize())
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    val groupedCalls = state.calls.groupBy { 
                                        it.timestamp.atZone(ZoneId.systemDefault()).toLocalDate() 
                                    }
                                    
                                    groupedCalls.forEach { (date, callsForDate) ->
                                        item {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = date.toString(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                        items(callsForDate) { call ->
                                            CallHistoryRow(callRecord = call)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
