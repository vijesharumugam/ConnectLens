package com.connectlens.app.feature.callhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.CallType
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.repository.CallLogRepository
import com.connectlens.app.domain.usecase.GetCallHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed class CallHistoryUiState {
    object Loading : CallHistoryUiState()
    data class Success(val calls: List<CallRecord>, val hasPermission: Boolean) : CallHistoryUiState()
    data class Error(val message: String) : CallHistoryUiState()
}

@HiltViewModel
class CallHistoryViewModel @Inject constructor(
    private val getCallHistoryUseCase: GetCallHistoryUseCase,
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.LAST_30_DAYS)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterType = MutableStateFlow<CallType?>(null)
    val filterType: StateFlow<CallType?> = _filterType

    private val _sortNewest = MutableStateFlow(true)
    val sortNewest: StateFlow<Boolean> = _sortNewest

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CallHistoryUiState> = combine(
        _selectedTimeRange,
        _searchQuery,
        _filterType,
        _sortNewest
    ) { range, query, filter, sort ->
        FilterParams(range, query, filter, sort)
    }.flatMapLatest { params ->
        val hasPermission = callLogRepository.hasPermission()
        if (!hasPermission) {
            flowOf<CallHistoryUiState>(CallHistoryUiState.Success(emptyList(), false))
        } else {
            getCallHistoryUseCase(params.range, params.query, params.filter, params.sort)
                .map<List<CallRecord>, CallHistoryUiState> { calls -> CallHistoryUiState.Success(calls, true) }
        }
    }
    .catch { emit(CallHistoryUiState.Error(it.message ?: "Unknown error")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CallHistoryUiState.Loading)

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(type: CallType?) {
        _filterType.value = type
    }

    fun toggleSort() {
        _sortNewest.value = !_sortNewest.value
    }

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    private data class FilterParams(
        val range: TimeRange,
        val query: String,
        val filter: CallType?,
        val sort: Boolean
    )
}
