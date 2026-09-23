package com.connectlens.app.domain.usecase

import com.connectlens.app.domain.model.ContactStats
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case that computes [ContactStats] for a specific contact or phone number.
 *
 * Either [contactId] or [phoneNumber] must be non-null.
 * Returns a Flow that emits null when no matching records are found.
 */
class GetContactStatsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val analyticsEngine: AnalyticsEngine
) {
    operator fun invoke(
        timeRange: TimeRange,
        contactId: Long?,
        phoneNumber: String?
    ): Flow<ContactStats?> =
        callLogRepository
            .getCallRecords(timeRange)
            .map { records ->
                analyticsEngine.computeContactStats(records, contactId, phoneNumber)
            }
}
