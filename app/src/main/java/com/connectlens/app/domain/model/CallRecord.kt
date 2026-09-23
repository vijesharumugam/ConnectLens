package com.connectlens.app.domain.model

import java.time.Instant

/**
 * Represents the type of a phone call as reported by the Android call-log.
 * UNKNOWN is used for any value not recognised by the current API level.
 */
enum class CallType(val displayName: String) {
    INCOMING("Incoming"),
    OUTGOING("Outgoing"),
    MISSED("Missed"),
    REJECTED("Rejected"),
    BLOCKED("Blocked"),
    UNKNOWN("Unknown")
}

/**
 * A single call record sourced from Android's CallLog ContentProvider.
 *
 * @param id              System-assigned call-log row ID.
 * @param number          Raw phone number string. May be empty if withheld.
 * @param contactName     Display name resolved from the Contacts ContentProvider,
 *                        or from the call-log cached name. Null if unresolved.
 * @param contactId       Android Contacts _ID for the matched contact, if any.
 * @param type            Mapped call type.
 * @param durationSeconds Duration in whole seconds. Zero for missed calls.
 * @param timestamp       When the call started (epoch instant, device timezone is
 *                        applied at display time via [TimeUtils]).
 */
data class CallRecord(
    val id: Long,
    val number: String,
    val contactName: String?,
    val contactId: Long?,
    val type: CallType,
    val durationSeconds: Long,
    val timestamp: Instant
) {
    /** True when a contact name or ID was matched to this call. */
    val isKnownContact: Boolean get() = contactName != null || contactId != null

    /** Stable key for grouping: contactId if known, otherwise the phone number. */
    val groupKey: String get() = contactId?.toString() ?: number.ifBlank { "unknown_$id" }
}
