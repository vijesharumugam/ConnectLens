package com.connectlens.app.domain.usecase

import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.CallType
import com.connectlens.app.domain.model.ContactStats
import com.connectlens.app.domain.model.DashboardStats
import com.connectlens.app.domain.model.TimeRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure analytics engine with no Android framework dependencies.
 *
 * All functions are deterministic and side-effect-free, making them trivially
 * unit-testable with known input data.
 *
 * Timezone strategy: all date grouping uses [ZoneId.systemDefault()] so that
 * "calls per day" aligns with the device owner's calendar, not UTC.
 *
 * Overflow safety: durations are aggregated with Long arithmetic.
 * The theoretical maximum (all 86,400 seconds per day × 365 days) is
 * well within Long range.
 */
@Singleton
class AnalyticsEngine @Inject constructor() {

    // ── Dashboard ─────────────────────────────────────────────────────────────

    fun computeDashboardStats(
        records: List<CallRecord>,
        timeRange: TimeRange,
        lastSyncTime: Instant?
    ): DashboardStats {
        if (records.isEmpty()) return DashboardStats.empty(timeRange).copy(lastSyncTime = lastSyncTime)

        val totalCalls  = records.size
        val totalDuration = records.sumOf { it.durationSeconds }
        val avgDuration   = totalDuration / totalCalls.toLong()

        val uniqueContacts = records.map { it.groupKey }.toSet().size

        val incoming = records.count { it.type == CallType.INCOMING }
        val outgoing = records.count { it.type == CallType.OUTGOING }
        val missed   = records.count { it.type == CallType.MISSED }

        val hourlyDist  = groupByHourOfDay(records)
        val dowDist     = groupByDayOfWeek(records)
        val buckets     = computeDurationBuckets(records)
        val peakWin     = computePeakCallWindow(records)
        val balance     = computeCommunicationBalance(incoming, outgoing, totalCalls)
        val missedRate  = if (totalCalls > 0) (missed.toFloat() / totalCalls) * 100f else 0f
        val smartInsights = generateSmartInsights(records, incoming, outgoing, missed, totalCalls, totalDuration, peakWin)

        return DashboardStats(
            totalCalls             = totalCalls,
            totalDurationSeconds    = totalDuration,
            averageDurationSeconds   = avgDuration,
            uniqueContactsCount    = uniqueContacts,
            incomingCalls          = incoming,
            outgoingCalls          = outgoing,
            missedCalls            = missed,
            topContacts            = computeTopContactStats(records).take(10),
            callsByDay             = groupByDay(records),
            durationByDay          = groupDurationByDay(records),
            hourlyDistribution     = hourlyDist,
            dayOfWeekDistribution  = dowDist,
            durationBuckets        = buckets,
            peakWindow             = peakWin,
            communicationBalance   = balance,
            missedCallRate         = missedRate,
            insights               = smartInsights,
            timeRange              = timeRange,
            lastSyncTime           = lastSyncTime
        )
    }

    // ── Per-contact ───────────────────────────────────────────────────────────

    /**
     * Returns [ContactStats] for the group identified by [contactId] or
     * [phoneNumber]. Returns null if no matching records are found.
     */
    fun computeContactStats(
        records: List<CallRecord>,
        contactId: Long?,
        phoneNumber: String?
    ): ContactStats? {
        val filtered = when {
            contactId != null    -> records.filter { it.contactId == contactId }
            phoneNumber != null  -> records.filter { it.number == phoneNumber }
            else                 -> return null
        }
        if (filtered.isEmpty()) return null

        val first       = filtered.first()
        val displayName = first.contactName ?: phoneNumber ?: "Unknown"
        return buildContactStats(filtered, displayName, phoneNumber ?: first.number)
    }

    /**
     * Returns stats for every distinct contact/number group in [records],
     * sorted descending by total call duration.
     */
    fun computeTopContactStats(records: List<CallRecord>): List<ContactStats> =
        records
            .groupBy { it.groupKey }
            .values
            .map { contactRecords ->
                val first       = contactRecords.first()
                val displayName = first.contactName ?: first.number.ifBlank { "Unknown" }
                buildContactStats(contactRecords, displayName, first.number)
            }
            .sortedByDescending { it.totalDurationSeconds }

    // ── Grouping helpers ──────────────────────────────────────────────────────

    /** Groups call records by calendar day in device default timezone. */
    fun groupByDay(records: List<CallRecord>): Map<LocalDate, Int> {
        val zone = ZoneId.systemDefault()
        return records
            .groupBy { it.timestamp.atZone(zone).toLocalDate() }
            .mapValues { (_, v) -> v.size }
    }

    /** Groups total call duration by calendar day. */
    fun groupDurationByDay(records: List<CallRecord>): Map<LocalDate, Long> {
        val zone = ZoneId.systemDefault()
        return records
            .groupBy { it.timestamp.atZone(zone).toLocalDate() }
            .mapValues { (_, v) -> v.sumOf { it.durationSeconds } }
    }

    /** Groups call count by ISO week string (e.g. "2024-W03"). */
    fun groupByWeek(records: List<CallRecord>): Map<String, Int> {
        val zone = ZoneId.systemDefault()
        val woy  = WeekFields.ISO.weekOfWeekBasedYear()
        return records
            .groupBy {
                val d = it.timestamp.atZone(zone).toLocalDate()
                "${d.get(WeekFields.ISO.weekBasedYear())}-W${d.get(woy).toString().padStart(2, '0')}"
            }
            .mapValues { (_, v) -> v.size }
    }

    /** Groups call count by month string (e.g. "2024-03"). */
    fun groupByMonth(records: List<CallRecord>): Map<String, Int> {
        val zone = ZoneId.systemDefault()
        return records
            .groupBy {
                val d = it.timestamp.atZone(zone).toLocalDate()
                "${d.year}-${d.monthValue.toString().padStart(2, '0')}"
            }
            .mapValues { (_, v) -> v.size }
    }

    /** Groups call count by 24 hour of day (0..23). */
    fun groupByHourOfDay(records: List<CallRecord>): Map<Int, Int> {
        val zone = ZoneId.systemDefault()
        val hourMap = (0..23).associateWith { 0 }.toMutableMap()
        records.forEach { record ->
            val hour = record.timestamp.atZone(zone).hour
            hourMap[hour] = (hourMap[hour] ?: 0) + 1
        }
        return hourMap
    }

    /** Groups call count by Day of Week (1 = Monday, 7 = Sunday). */
    fun groupByDayOfWeek(records: List<CallRecord>): Map<Int, Int> {
        val zone = ZoneId.systemDefault()
        val dowMap = (1..7).associateWith { 0 }.toMutableMap()
        records.forEach { record ->
            val dow = record.timestamp.atZone(zone).dayOfWeek.value
            dowMap[dow] = (dowMap[dow] ?: 0) + 1
        }
        return dowMap
    }

    /** Groups call count by duration bucket ranges. */
    fun computeDurationBuckets(records: List<CallRecord>): Map<String, Int> {
        val connected = records.filter { it.durationSeconds > 0 }
        return linkedMapOf(
            "< 1m"    to connected.count { it.durationSeconds in 1..59 },
            "1-5m"   to connected.count { it.durationSeconds in 60..299 },
            "5-15m"  to connected.count { it.durationSeconds in 300..899 },
            "15-30m" to connected.count { it.durationSeconds in 900..1799 },
            "30m+"   to connected.count { it.durationSeconds >= 1800 }
        )
    }

    /** Calculates busiest 3-hour window in 24-hour cycle. */
    fun computePeakCallWindow(records: List<CallRecord>): String {
        if (records.isEmpty()) return "N/A"
        val hourMap = groupByHourOfDay(records)
        var maxCount = -1
        var bestStartHour = 9

        for (h in 0..23) {
            val count = (0..2).sumOf { offset -> hourMap[(h + offset) % 24] ?: 0 }
            if (count > maxCount) {
                maxCount = count
                bestStartHour = h
            }
        }

        fun formatHour(h: Int): String {
            val mod = h % 12
            val hourNum = if (mod == 0) 12 else mod
            val ampm = if (h < 12) "AM" else "PM"
            return "$hourNum $ampm"
        }

        val endHour = (bestStartHour + 3) % 24
        return "${formatHour(bestStartHour)} - ${formatHour(endHour)}"
    }

    /** Computes communication balance score and style description. */
    fun computeCommunicationBalance(incoming: Int, outgoing: Int, total: Int): String {
        if (total == 0) return "No Calls Recorded"
        val inPct = ((incoming.toFloat() / total) * 100).toInt()
        val outPct = 100 - inPct
        val style = when {
            inPct >= 70 -> "High Responsiveness"
            outPct >= 70 -> "Active Initiator"
            else -> "Balanced Communicator"
        }
        return "$inPct% In / $outPct% Out • $style"
    }

    /** Generates automated smart insight cards based on calling patterns. */
    fun generateSmartInsights(
        records: List<CallRecord>,
        incoming: Int,
        outgoing: Int,
        missed: Int,
        total: Int,
        totalDuration: Long,
        peakWindow: String
    ): List<String> {
        if (records.isEmpty()) return listOf("No call history available for this time range.")
        val list = mutableListOf<String>()

        // 1. Volume & Duration insight
        val avgMin = if (total > 0) (totalDuration / total) / 60 else 0
        list.add("You handled $total total calls with an average call duration of $avgMin mins.")

        // 2. Peak time insight
        list.add("Your peak calling window is $peakWindow.")

        // 3. Missed call alert
        if (missed > 0) {
            val missedPct = ((missed.toFloat() / total) * 100).toInt()
            if (missedPct > 25) {
                list.add("High missed call rate ($missedPct% of calls). Consider reviewing check-in frequency.")
            } else {
                list.add("Missed call rate is healthy at $missedPct%.")
            }
        } else {
            list.add("Zero missed calls recorded in this time period!")
        }

        // 4. Longest conversation
        val longest = records.maxByOrNull { it.durationSeconds }
        if (longest != null && longest.durationSeconds > 300) {
            val mins = longest.durationSeconds / 60
            val name = longest.contactName ?: longest.number
            list.add("Longest conversation: $mins mins with $name.")
        }

        return list
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun buildContactStats(
        records: List<CallRecord>,
        displayName: String,
        phoneNumber: String?
    ): ContactStats {
        val totalDuration = records.sumOf { it.durationSeconds }
        val avgDuration   = if (records.isNotEmpty()) totalDuration / records.size else 0L
        return ContactStats(
            contact                = null,   // enriched by ContactRepository if needed
            displayName            = displayName,
            phoneNumber            = phoneNumber,
            totalCalls             = records.size,
            incomingCalls          = records.count { it.type == CallType.INCOMING },
            outgoingCalls          = records.count { it.type == CallType.OUTGOING },
            missedCalls            = records.count { it.type == CallType.MISSED },
            totalDurationSeconds   = totalDuration,
            averageDurationSeconds = avgDuration,
            longestCallSeconds     = records.maxOfOrNull { it.durationSeconds } ?: 0L,
            mostRecentCall         = records.maxByOrNull { it.timestamp }?.timestamp,
            firstCall              = records.minByOrNull { it.timestamp }?.timestamp,
            callsByDay             = groupByDay(records)
        )
    }
}
