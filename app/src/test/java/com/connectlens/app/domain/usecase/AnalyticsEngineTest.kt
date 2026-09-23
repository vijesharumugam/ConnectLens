package com.connectlens.app.domain.usecase

import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.CallType
import com.connectlens.app.domain.model.DashboardStats
import com.connectlens.app.domain.model.TimeRange
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for [AnalyticsEngine].
 *
 * All tests use deterministic inputs; no Android framework dependencies.
 * Verify the tests run with: ./gradlew test
 */
class AnalyticsEngineTest {

    private lateinit var engine: AnalyticsEngine

    // Fixed instants for deterministic date-grouping tests
    private val zone   = ZoneId.systemDefault()
    private val today  = LocalDate.now(zone)
    private val todayInstant    = today.atStartOfDay(zone).plusHours(10).toInstant()
    private val yesterdayInstant = today.minusDays(1).atStartOfDay(zone).plusHours(10).toInstant()

    @Before
    fun setUp() {
        engine = AnalyticsEngine()
    }

    // ── Empty dataset ─────────────────────────────────────────────────────────

    @Test
    fun `computeDashboardStats with empty list returns zero stats`() {
        val result = engine.computeDashboardStats(emptyList(), TimeRange.LAST_30_DAYS, null)
        assertEquals(0, result.totalCalls)
        assertEquals(0L, result.totalDurationSeconds)
        assertEquals(0, result.uniqueContactsCount)
        assertTrue(result.topContacts.isEmpty())
        assertTrue(result.callsByDay.isEmpty())
    }

    // ── Total call count ──────────────────────────────────────────────────────

    @Test
    fun `computeDashboardStats counts all records`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 60L),
            makeRecord(2L, CallType.OUTGOING, 120L),
            makeRecord(3L, CallType.MISSED,   0L)
        )
        val result = engine.computeDashboardStats(records, TimeRange.LAST_30_DAYS, null)
        assertEquals(3, result.totalCalls)
    }

    // ── Duration aggregation ──────────────────────────────────────────────────

    @Test
    fun `computeDashboardStats sums durations correctly`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 300L),
            makeRecord(2L, CallType.OUTGOING, 700L)
        )
        val result = engine.computeDashboardStats(records, TimeRange.LAST_30_DAYS, null)
        assertEquals(1000L, result.totalDurationSeconds)
    }

    @Test
    fun `computeDashboardStats average is total div count`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 100L),
            makeRecord(2L, CallType.OUTGOING, 200L),
            makeRecord(3L, CallType.MISSED,    0L)
        )
        val result = engine.computeDashboardStats(records, TimeRange.LAST_30_DAYS, null)
        // avg = 300 / 3 = 100
        assertEquals(100L, result.averageDurationSeconds)
    }

    // ── Call type breakdown ───────────────────────────────────────────────────

    @Test
    fun `computeDashboardStats correctly counts incoming outgoing missed`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 60L),
            makeRecord(2L, CallType.INCOMING, 90L),
            makeRecord(3L, CallType.OUTGOING, 120L),
            makeRecord(4L, CallType.MISSED,   0L)
        )
        val result = engine.computeDashboardStats(records, TimeRange.LAST_30_DAYS, null)
        assertEquals(2, result.incomingCalls)
        assertEquals(1, result.outgoingCalls)
        assertEquals(1, result.missedCalls)
    }

    // ── Unique contacts ───────────────────────────────────────────────────────

    @Test
    fun `unique contacts counts distinct groupKey values`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 60L,  contactId = 10L),
            makeRecord(2L, CallType.OUTGOING, 30L,  contactId = 10L), // same contact
            makeRecord(3L, CallType.INCOMING, 45L,  contactId = 20L),
            makeRecord(4L, CallType.MISSED,   0L,   number = "9876543210") // unknown
        )
        val result = engine.computeDashboardStats(records, TimeRange.LAST_30_DAYS, null)
        assertEquals(3, result.uniqueContactsCount) // contactId=10, contactId=20, number
    }

    // ── Grouping by day ───────────────────────────────────────────────────────

    @Test
    fun `groupByDay groups records into correct dates`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 60L, timestamp = todayInstant),
            makeRecord(2L, CallType.OUTGOING, 30L, timestamp = todayInstant),
            makeRecord(3L, CallType.MISSED,   0L,  timestamp = yesterdayInstant)
        )
        val grouped = engine.groupByDay(records)
        assertEquals(2, grouped[today] ?: 0)
        assertEquals(1, grouped[today.minusDays(1)] ?: 0)
    }

    // ── Top contacts ──────────────────────────────────────────────────────────

    @Test
    fun `computeTopContactStats sorts by total duration descending`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 300L, contactId = 1L, name = "Alice"),
            makeRecord(2L, CallType.OUTGOING, 900L, contactId = 2L, name = "Bob"),
            makeRecord(3L, CallType.INCOMING, 150L, contactId = 3L, name = "Charlie")
        )
        val top = engine.computeTopContactStats(records)
        assertEquals("Bob",     top[0].displayName)
        assertEquals("Alice",   top[1].displayName)
        assertEquals("Charlie", top[2].displayName)
    }

    @Test
    fun `computeTopContactStats groups multiple calls for same contact`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 100L, contactId = 5L, name = "Dana"),
            makeRecord(2L, CallType.OUTGOING, 200L, contactId = 5L, name = "Dana"),
            makeRecord(3L, CallType.MISSED,   0L,   contactId = 5L, name = "Dana")
        )
        val top = engine.computeTopContactStats(records)
        assertEquals(1, top.size) // All grouped under contactId=5
        assertEquals(3, top[0].totalCalls)
        assertEquals(300L, top[0].totalDurationSeconds)
        assertEquals(1, top[0].missedCalls)
    }

    // ── Contact stats ─────────────────────────────────────────────────────────

    @Test
    fun `computeContactStats returns null for empty input`() {
        val result = engine.computeContactStats(emptyList(), contactId = 1L, phoneNumber = null)
        assertNull(result)
    }

    @Test
    fun `computeContactStats returns null when no records match contactId`() {
        val records = listOf(makeRecord(1L, CallType.INCOMING, 60L, contactId = 999L))
        val result  = engine.computeContactStats(records, contactId = 1L, phoneNumber = null)
        assertNull(result)
    }

    @Test
    fun `computeContactStats longest call is max duration`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 100L, contactId = 1L),
            makeRecord(2L, CallType.OUTGOING, 500L, contactId = 1L),
            makeRecord(3L, CallType.OUTGOING, 250L, contactId = 1L)
        )
        val stats = engine.computeContactStats(records, contactId = 1L, phoneNumber = null)
        assertNotNull(stats)
        assertEquals(500L, stats!!.longestCallSeconds)
    }

    // ── Formatted helpers ─────────────────────────────────────────────────────

    @Test
    fun `DashboardStats formattedTotalDuration formats hours correctly`() {
        val stats = DashboardStats.empty(TimeRange.LAST_30_DAYS)
            .copy(totalDurationSeconds = 3_661L)
        assertEquals("1h 1m", stats.formattedTotalDuration)
    }

    @Test
    fun `DashboardStats formattedTotalDuration formats minutes only`() {
        val stats = DashboardStats.empty(TimeRange.LAST_30_DAYS)
            .copy(totalDurationSeconds = 300L)
        assertEquals("5m", stats.formattedTotalDuration)
    }

    // ── Hourly & Day of Week Grouping ─────────────────────────────────────────

    @Test
    fun `groupByHourOfDay maps records to 24 hour slots`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 60L, timestamp = today.atTime(14, 30).atZone(zone).toInstant()),
            makeRecord(2L, CallType.OUTGOING, 30L, timestamp = today.atTime(14, 45).atZone(zone).toInstant()),
            makeRecord(3L, CallType.MISSED,   0L,  timestamp = today.atTime(9, 15).atZone(zone).toInstant())
        )
        val hourly = engine.groupByHourOfDay(records)
        assertEquals(2, hourly[14])
        assertEquals(1, hourly[9])
        assertEquals(0, hourly[0])
    }

    @Test
    fun `computeDurationBuckets classifies calls correctly`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 30L),   // < 1m
            makeRecord(2L, CallType.OUTGOING, 120L),  // 1-5m
            makeRecord(3L, CallType.INCOMING, 600L),  // 5-15m
            makeRecord(4L, CallType.OUTGOING, 2000L), // 30m+
            makeRecord(5L, CallType.MISSED,    0L)    // excluded (0 duration)
        )
        val buckets = engine.computeDurationBuckets(records)
        assertEquals(1, buckets["< 1m"])
        assertEquals(1, buckets["1-5m"])
        assertEquals(1, buckets["5-15m"])
        assertEquals(1, buckets["30m+"])
    }

    @Test
    fun `computePeakCallWindow returns valid formatted time range`() {
        val records = listOf(
            makeRecord(1L, CallType.INCOMING, 60L, timestamp = today.atTime(14, 0).atZone(zone).toInstant()),
            makeRecord(2L, CallType.INCOMING, 60L, timestamp = today.atTime(15, 0).atZone(zone).toInstant()),
            makeRecord(3L, CallType.INCOMING, 60L, timestamp = today.atTime(16, 0).atZone(zone).toInstant())
        )
        val peak = engine.computePeakCallWindow(records)
        assertTrue("Peak window should contain AM/PM", peak.contains("AM") || peak.contains("PM"))
    }

    @Test
    fun `computeCommunicationBalance formats ratio and style`() {
        val balance = engine.computeCommunicationBalance(70, 30, 100)
        assertTrue(balance.contains("70% In"))
        assertTrue(balance.contains("30% Out"))
    }

    // ── Group by week / month ─────────────────────────────────────────────────

    @Test
    fun `groupByWeek produces non-empty result for multi-week data`() {
        val records = (0..14).map { dayOffset ->
            makeRecord(
                id        = dayOffset.toLong(),
                type      = CallType.INCOMING,
                duration  = 60L,
                timestamp = today.minusDays(dayOffset.toLong())
                    .atStartOfDay(zone).toInstant()
            )
        }
        val grouped = engine.groupByWeek(records)
        assertTrue("Expected at least 2 weeks", grouped.size >= 2)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun makeRecord(
        id: Long,
        type: CallType,
        duration: Long,
        contactId: Long? = null,
        number: String   = "+911234567890",
        name: String?    = null,
        timestamp: Instant = todayInstant
    ) = CallRecord(
        id              = id,
        number          = number,
        contactName     = name,
        contactId       = contactId,
        type            = type,
        durationSeconds = duration,
        timestamp       = timestamp
    )
}
