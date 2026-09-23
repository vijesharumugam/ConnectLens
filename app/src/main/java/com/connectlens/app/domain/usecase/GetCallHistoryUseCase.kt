package com.connectlens.app.domain.usecase

import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.CallType
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case that returns a filtered and sorted list of [CallRecord]s.
 *
 * Supports:
 * - Free-text search (contact name or phone number)
 * - Call type filter
 * - Sort order (newest / oldest first)
 */
class GetCallHistoryUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository
) {
    operator fun invoke(
        timeRange: TimeRange,
        searchQuery: String = "",
        filterType: CallType? = null,
        sortNewest: Boolean = true
    ): Flow<List<CallRecord>> =
        callLogRepository
            .getCallRecords(timeRange)
            .map { records ->
                var result = records

                // Apply text search
                if (searchQuery.isNotBlank()) {
                    val q = searchQuery.trim()
                    result = result.filter { record ->
                        record.contactName?.contains(q, ignoreCase = true) == true ||
                                record.number.contains(q, ignoreCase = true)
                    }
                }

                // Apply call-type filter
                if (filterType != null) {
                    result = result.filter { it.type == filterType }
                }

                // Sort
                if (sortNewest) {
                    result.sortedByDescending { it.timestamp }
                } else {
                    result.sortedBy { it.timestamp }
                }
            }
}
