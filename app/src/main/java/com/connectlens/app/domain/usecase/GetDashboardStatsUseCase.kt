package com.connectlens.app.domain.usecase

import com.connectlens.app.domain.model.DashboardStats
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Use case that provides a [DashboardStats] Flow for a given [TimeRange].
 *
 * Combines call-log data with the [AnalyticsEngine] to produce aggregated
 * statistics. The result updates automatically if the underlying data changes.
 */
class GetDashboardStatsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val analyticsEngine: AnalyticsEngine
) {
    operator fun invoke(timeRange: TimeRange): Flow<DashboardStats> =
        callLogRepository
            .getCallRecords(timeRange)
            .map { records ->
                analyticsEngine.computeDashboardStats(
                    records      = records,
                    timeRange    = timeRange,
                    lastSyncTime = Instant.now()
                )
            }

    fun hasCallLogPermission(): Boolean = callLogRepository.hasPermission()
}
