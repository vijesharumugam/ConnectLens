package com.connectlens.app.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Top-level analytics summary displayed on the Dashboard.
 *
 * All fields are computed from actual permitted call records within [timeRange].
 * Never fabricated or estimated.
 */
data class DashboardStats(
    val totalCalls: Int,

    /** Aggregate connected duration in seconds across all calls in the range. */
    val totalDurationSeconds: Long,

    /** Average duration per call (including missed calls counted as 0 seconds). */
    val averageDurationSeconds: Long,

    /** Count of distinct contacts (or phone numbers) seen in the range. */
    val uniqueContactsCount: Int,

    val incomingCalls: Int,
    val outgoingCalls: Int,

    /** Missed call count; may be zero if data is unavailable. */
    val missedCalls: Int,

    /** Top contacts sorted by total duration. Limited to at most 10 entries. */
    val topContacts: List<ContactStats>,

    /** Calls per calendar date, used for bar charts. */
    val callsByDay: Map<LocalDate, Int>,

    /** Duration (seconds) per calendar date, used for talk-time charts. */
    val durationByDay: Map<LocalDate, Long>,

    /** Hourly breakdown (0..23 hours). */
    val hourlyDistribution: Map<Int, Int> = emptyMap(),

    /** Day of week breakdown (1..7). */
    val dayOfWeekDistribution: Map<Int, Int> = emptyMap(),

    /** Call duration buckets (e.g. "< 1m", "1-5m", "5-15m", "15-30m", "30m+"). */
    val durationBuckets: Map<String, Int> = emptyMap(),

    /** Human readable peak calling window (e.g. "2:00 PM - 5:00 PM"). */
    val peakWindow: String = "N/A",

    /** Communication style indicator (e.g. "62% Incoming / 38% Outgoing • Responsive"). */
    val communicationBalance: String = "Balanced",

    /** Missed call percentage (0..100). */
    val missedCallRate: Float = 0f,

    /** Generated smart insight bullet points. */
    val insights: List<String> = emptyList(),

    /** The time filter this summary was computed for. */
    val timeRange: TimeRange,

    /** Wall-clock time when this data was last read from the system. */
    val lastSyncTime: Instant?
) {
    /** e.g. "4h 12m" or "37m" */
    val formattedTotalDuration: String
        get() {
            val hours   = totalDurationSeconds / 3600
            val minutes = (totalDurationSeconds % 3600) / 60
            return when {
                hours > 0   -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else        -> "${totalDurationSeconds}s"
            }
        }

    /** e.g. "4m 30s" */
    val formattedAverageDuration: String
        get() {
            val minutes = averageDurationSeconds / 60
            val seconds = averageDurationSeconds % 60
            return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
        }

    companion object {
        /** Returns an empty stats object for a given range (no data / no permission). */
        fun empty(timeRange: TimeRange) = DashboardStats(
            totalCalls             = 0,
            totalDurationSeconds    = 0L,
            averageDurationSeconds   = 0L,
            uniqueContactsCount    = 0,
            incomingCalls          = 0,
            outgoingCalls          = 0,
            missedCalls            = 0,
            topContacts            = emptyList(),
            callsByDay             = emptyMap(),
            durationByDay          = emptyMap(),
            hourlyDistribution     = emptyMap(),
            dayOfWeekDistribution  = emptyMap(),
            durationBuckets        = emptyMap(),
            peakWindow             = "N/A",
            communicationBalance   = "No data",
            missedCallRate         = 0f,
            insights               = emptyList(),
            timeRange              = timeRange,
            lastSyncTime           = null
        )
    }
}
