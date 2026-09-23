package com.connectlens.app.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectlens.app.domain.model.DashboardStats
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.repository.CallLogRepository
import com.connectlens.app.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val stats: DashboardStats, val insights: List<String>) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
    object PermissionRequired : AnalyticsUiState()
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val callLogRepository: CallLogRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.LAST_30_DAYS)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = _selectedTimeRange
        .flatMapLatest { range ->
            if (!callLogRepository.hasPermission()) {
                flowOf(AnalyticsUiState.PermissionRequired)
            } else {
                getDashboardStatsUseCase(range).map { stats ->
                    AnalyticsUiState.Success(stats, generateInsights(stats))
                }
            }
        }
        .catch { emit(AnalyticsUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState.Loading)

    fun selectTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    private fun generateInsights(stats: DashboardStats): List<String> {
        val insights = mutableListOf<String>()
        if (stats.totalCalls > 0) {
            insights.add("You made ${stats.totalCalls} calls in this period.")
            val maxDay = stats.callsByDay.maxByOrNull { it.value }
            if (maxDay != null) {
                insights.add("Your busiest day was ${maxDay.key} with ${maxDay.value} calls.")
            }
            val incomingPct = (stats.incomingCalls.toFloat() / stats.totalCalls * 100).toInt()
            insights.add("Incoming calls make up $incomingPct% of total.")
            if (stats.averageDurationSeconds > 0) {
                insights.add("Average call duration is ${stats.averageDurationSeconds / 60} minutes.")
            }
        }
        return insights
    }
}
