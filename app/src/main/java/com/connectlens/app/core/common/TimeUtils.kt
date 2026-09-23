package com.connectlens.app.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Utility functions for formatting timestamps and durations consistently
 * across all screens.
 *
 * All formatting uses the device default time zone.
 */
object TimeUtils {

    private val zone = ZoneId.systemDefault()

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(zone)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, h:mm a").withZone(zone)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a").withZone(zone)
    private val dateOnlyFormatter = DateTimeFormatter.ofPattern("d MMM yyyy").withZone(zone)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM").withZone(zone)

    /**
     * Formats an [Instant] for display in call history lists:
     * - Today: "3:45 PM"
     * - Within the last 7 days: "Mon, 3:45 PM"
     * - Older: "15 Jun 2024, 3:45 PM"
     */
    fun formatRelativeDate(instant: Instant): String {
        val now   = Instant.now()
        val today = LocalDate.now(zone)
        val date  = instant.atZone(zone).toLocalDate()

        return when {
            date == today -> "Today · " + timeFormatter.format(instant)
            ChronoUnit.DAYS.between(date, today) < 7 -> {
                val dayName = DateTimeFormatter.ofPattern("EEE").withZone(zone).format(instant)
                "$dayName · " + timeFormatter.format(instant)
            }
            else -> fullDateFormatter.format(instant)
        }
    }

    /** Simple time-only format: "3:45 PM". */
    fun formatTime(instant: Instant): String = timeFormatter.format(instant)

    /** "15 Jun 2024" */
    fun formatDate(instant: Instant): String = dateOnlyFormatter.format(instant)

    /** "15 Jun" (no year) */
    fun formatShortDate(instant: Instant): String = shortDateFormatter.format(instant)

    /** "15 Jun 2024, 3:45 PM" */
    fun formatDateTime(instant: Instant): String = fullDateFormatter.format(instant)

    /**
     * Formats a duration in seconds to a human-readable string.
     *
     * Examples:
     *  - 45      → "45s"
     *  - 90      → "1m 30s"
     *  - 3661    → "1h 1m"
     */
    fun formatDuration(seconds: Long): String {
        if (seconds <= 0L) return "0s"
        val hours   = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs    = seconds % 60
        return when {
            hours > 0   -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${secs}s"
            else        -> "${secs}s"
        }
    }

    /**
     * Formats duration as a compact string suitable for list items.
     * e.g. "1h 4m", "23m", "45s"
     */
    fun formatDurationCompact(seconds: Long): String = formatDuration(seconds)

    /** Returns the [LocalDate] for an [Instant] in device timezone. */
    fun toLocalDate(instant: Instant): LocalDate = instant.atZone(zone).toLocalDate()
}
