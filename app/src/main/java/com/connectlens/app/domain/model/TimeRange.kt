package com.connectlens.app.domain.model

import java.time.ZonedDateTime
import java.time.temporal.WeekFields

/**
 * Time filters available throughout the app.
 *
 * Each value converts to a [start, end] pair of [ZonedDateTime] using the
 * device's default time zone. All date arithmetic happens at the call site so
 * that the domain model remains free of side effects.
 */
enum class TimeRange(val displayName: String) {
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_90_DAYS("Last 90 days"),
    CURRENT_MONTH("This month"),
    PREVIOUS_MONTH("Last month"),
    ALL_TIME("All time");

    /**
     * Returns [Pair<start, end>] in the device default time zone.
     * The end is always the current moment.
     */
    fun toDateRange(): Pair<ZonedDateTime, ZonedDateTime> {
        val now = ZonedDateTime.now()
        val start = when (this) {
            LAST_7_DAYS    -> now.minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0)
            LAST_30_DAYS   -> now.minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0)
            LAST_90_DAYS   -> now.minusDays(90).withHour(0).withMinute(0).withSecond(0).withNano(0)
            CURRENT_MONTH  -> now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            PREVIOUS_MONTH -> now.minusMonths(1).withDayOfMonth(1)
                                 .withHour(0).withMinute(0).withSecond(0).withNano(0)
            ALL_TIME       -> ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, now.zone)
        }
        return Pair(start, now)
    }
}
