package com.connectlens.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectlens.app.domain.repository.CallLogRepository
import com.connectlens.app.domain.repository.ContactRepository
import com.connectlens.app.domain.model.DashboardStats
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val stats: DashboardStats,
        val hasCallPermission: Boolean,
        val hasContactPermission: Boolean
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
    object PermissionRequired : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.LAST_30_DAYS)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedTimeRange,
        _refreshTrigger.onStart { emit(Unit) }
    ) { timeRange, _ ->
        timeRange
    }.map { timeRange ->
        val hasCall = callLogRepository.hasPermission()
        val hasContact = contactRepository.hasPermission()
        
        if (!hasCall) {
            DashboardUiState.PermissionRequired
        } else {
            try {
                val stats = getDashboardStatsUseCase(timeRange).first()
                DashboardUiState.Success(stats, hasCall, hasContact)
            } catch (e: Exception) {
                DashboardUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }

    fun checkPermissions(): Pair<Boolean, Boolean> {
        val hasCall = callLogRepository.hasPermission()
        val hasContact = contactRepository.hasPermission()
        if (hasCall) refresh()
        return Pair(hasCall, hasContact)
    }
}
