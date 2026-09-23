package com.connectlens.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a cached call record.
 *
 * Privacy note: We store only what is necessary for analytics computation.
 * Phone numbers and contact names are stored to enable display and searching.
 * The original system call-log is never modified by ConnectLens.
 *
 * Indexed on [timestampMs] for efficient date-range queries.
 */
@Entity(
    tableName = "call_records",
    indices = [Index(value = ["timestampMs"])]
)
data class CallRecordEntity(
    @PrimaryKey
    val id: Long,

    /** Raw phone number from the call log. May be empty string. */
    val number: String,

    /** Cached contact display name (from CallLog.Calls.CACHED_NAME). Nullable. */
    val contactName: String?,

    /** Android Contacts _ID matched to this number. Nullable. */
    val contactId: Long?,

    /** String representation of [CallType] enum name. */
    val callType: String,

    /** Duration in seconds. 0 for missed/rejected calls. */
    val durationSeconds: Long,

    /** Call start time as epoch milliseconds. */
    val timestampMs: Long
)
