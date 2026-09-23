package com.connectlens.app.feature.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectlens.app.core.ui.ContactRow
import com.connectlens.app.core.ui.ErrorState
import com.connectlens.app.core.ui.LoadingState
import com.connectlens.app.core.ui.TimeRangeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = viewModel::updateSearch,
                            placeholder = { Text("Search contacts...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        Text("Contacts")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TimeRangeSelector(
                selected = selectedTimeRange,
                onSelect = viewModel::selectTimeRange,
                modifier = Modifier.fillMaxWidth()
            )

            ScrollableTabRow(
                selectedTabIndex = sortBy.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(selected = sortBy == SortBy.BY_CALLS, onClick = { viewModel.selectSort(SortBy.BY_CALLS) }, text = { Text("Most Calls") })
                Tab(selected = sortBy == SortBy.BY_DURATION, onClick = { viewModel.selectSort(SortBy.BY_DURATION) }, text = { Text("Most Duration") })
                Tab(selected = sortBy == SortBy.BY_RECENT, onClick = { viewModel.selectSort(SortBy.BY_RECENT) }, text = { Text("Recent") })
            }

            when (val state = uiState) {
                is ContactListUiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is ContactListUiState.Error -> ErrorState(message = state.message, onRetry = { }, modifier = Modifier.fillMaxSize())
                is ContactListUiState.Success -> {
                    if (state.contacts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("No contacts found.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                            items(state.contacts) { contactStats ->
                                ContactRow(
                                    stats = contactStats,
                                    onClick = { contactStats.contact?.id?.let { onNavigateToDetail(it) } },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
