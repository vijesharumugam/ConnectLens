package com.connectlens.app.feature.contacts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.ContactStats
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.usecase.GetCallHistoryUseCase
import com.connectlens.app.domain.usecase.GetContactStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed class ContactDetailUiState {
    object Loading : ContactDetailUiState()
    data class Success(val stats: ContactStats, val calls: List<CallRecord>) : ContactDetailUiState()
    data class Error(val message: String) : ContactDetailUiState()
    object NotFound : ContactDetailUiState()
}

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getContactStatsUseCase: GetContactStatsUseCase,
    private val getCallHistoryUseCase: GetCallHistoryUseCase
) : ViewModel() {

    private val contactId: Long? = savedStateHandle.get<String>("contactId")?.toLongOrNull()
    private val phoneNumber: String? = savedStateHandle.get<String>("phoneNumber")

    private val _selectedTimeRange = MutableStateFlow(TimeRange.LAST_30_DAYS)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ContactDetailUiState> = _selectedTimeRange
        .flatMapLatest { range ->
            combine(
                getContactStatsUseCase(range, contactId, phoneNumber),
                getCallHistoryUseCase(range, phoneNumber ?: "", null, true)
            ) { stats, calls ->
                if (stats == null) {
                    ContactDetailUiState.NotFound
                } else {
                    ContactDetailUiState.Success(stats, calls)
                }
            }
        }
        .catch { emit(ContactDetailUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ContactDetailUiState.Loading
        )

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }
}
