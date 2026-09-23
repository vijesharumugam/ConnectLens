package com.connectlens.app.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Aggregated analytics for a single contact (or an unmatched phone number).
 *
 * Computed by [AnalyticsEngine] from a filtered list of [CallRecord]s.
 * All numeric fields are non-negative; missing data is represented by 0 / null.
 */
data class ContactStats(
    /** The matched Contact from the Contacts ContentProvider, if available. */
    val contact: Contact?,

    /** Display name to show in the UI. Falls back to phoneNumber or "Unknown". */
    val displayName: String,

    /** Primary phone number for this group (raw, may be masked in UI). */
    val phoneNumber: String?,

    val totalCalls: Int,
    val incomingCalls: Int,
    val outgoingCalls: Int,

    /**
     * Missed calls count.
     * Note: Android's call log includes missed calls; however, this value
     * may be zero if the source data does not classify them separately.
     */
    val missedCalls: Int,

    /** Total connected duration across all call types, in seconds. */
    val totalDurationSeconds: Long,

    /** Average duration per call (including missed, which count as 0). */
    val averageDurationSeconds: Long,

    /** Duration of the single longest connected call. */
    val longestCallSeconds: Long,

    /** Timestamp of the most recent call in the selected range. */
    val mostRecentCall: Instant?,

    /** Timestamp of the earliest call in the selected range. */
    val firstCall: Instant?,

    /** Number of calls per calendar day. Useful for trend mini-charts. */
    val callsByDay: Map<LocalDate, Int>
) {
    /** Human-readable total duration (e.g. "2h 34m" or "45s"). */
    val formattedTotalDuration: String
        get() {
            val hours   = totalDurationSeconds / 3600
            val minutes = (totalDurationSeconds % 3600) / 60
            val seconds = totalDurationSeconds % 60
            return when {
                hours > 0   -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else        -> "${seconds}s"
            }
        }

    /** Human-readable average duration. */
    val formattedAverageDuration: String
        get() {
            val minutes = averageDurationSeconds / 60
            val seconds = averageDurationSeconds % 60
            return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
        }

    /** Human-readable longest call duration. */
    val formattedLongestCall: String
        get() {
            val minutes = longestCallSeconds / 60
            val seconds = longestCallSeconds % 60
            return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
        }
}
