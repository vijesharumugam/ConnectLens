package com.connectlens.app.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectlens.app.domain.repository.CallLogRepository
import com.connectlens.app.domain.repository.ContactRepository
import com.connectlens.app.domain.model.ContactStats
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SortBy {
    BY_CALLS, BY_DURATION, BY_RECENT
}

sealed class ContactListUiState {
    object Loading : ContactListUiState()
    data class Success(val contacts: List<ContactStats>, val hasPermission: Boolean) : ContactListUiState()
    data class Error(val message: String) : ContactListUiState()
}

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.ALL_TIME)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.BY_CALLS)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    val uiState: StateFlow<ContactListUiState> = combine(
        _selectedTimeRange,
        _searchQuery,
        _sortBy
    ) { timeRange, query, sort ->
        Triple(timeRange, query, sort)
    }.flatMapLatest { (timeRange, query, sort) ->
        flow {
            emit(ContactListUiState.Loading)
            val hasCall = callLogRepository.hasPermission()
            val hasContact = contactRepository.hasPermission()
            
            if (!hasCall) {
                emit(ContactListUiState.Success(emptyList(), false))
            } else {
                try {
                    val stats = getDashboardStatsUseCase(timeRange).first()
                    var list = stats.topContacts
                    
                    if (query.isNotBlank()) {
                        list = list.filter {
                            it.displayName.contains(query, ignoreCase = true) ||
                            (it.phoneNumber?.contains(query) == true)
                        }
                    }
                    
                    list = when (sort) {
                        SortBy.BY_CALLS -> list.sortedByDescending { it.totalCalls }
                        SortBy.BY_DURATION -> list.sortedByDescending { it.totalDurationSeconds }
                        SortBy.BY_RECENT -> list.sortedByDescending { it.mostRecentCall }
                    }
                    
                    emit(ContactListUiState.Success(list, true))
                } catch (e: Exception) {
                    emit(ContactListUiState.Error(e.message ?: "Error"))
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactListUiState.Loading)

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun selectSort(sort: SortBy) {
        _sortBy.value = sort
    }

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }
}
