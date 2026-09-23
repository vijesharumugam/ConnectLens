package com.connectlens.app.data.platform

import android.content.Context
import android.provider.CallLog
import android.util.Log
import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.CallType
import com.connectlens.app.domain.model.TimeRange
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads call records from Android's [CallLog.Calls] ContentProvider.
 *
 * This class requires READ_CALL_LOG permission at the call site.
 * The caller is responsible for verifying permission before invoking [readCallLog].
 *
 * Privacy note:
 * - Only reads; never writes to the call log.
 * - No data is transmitted off-device.
 * - Phone numbers are not logged.
 */
@Singleton
class CallLogReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CallLogReader"
    }

    /**
     * Reads call records within [timeRange] from the system call log.
     *
     * Returns an empty list (not an exception) if:
     * - Permission was revoked between the check and this call.
     * - The ContentProvider is unavailable.
     * - The device has no call history in the range.
     */
    fun readCallLog(timeRange: TimeRange): List<CallRecord> {
        val (start, end) = timeRange.toDateRange()
        val startMs = start.toInstant().toEpochMilli()
        val endMs   = end.toInstant().toEpochMilli()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.CACHED_LOOKUP_URI
        )

        val selection     = "${CallLog.Calls.DATE} >= ? AND ${CallLog.Calls.DATE} <= ?"
        val selectionArgs = arrayOf(startMs.toString(), endMs.toString())
        val sortOrder     = "${CallLog.Calls.DATE} DESC"

        return try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val records = mutableListOf<CallRecord>()

                val idIdx       = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
                val numberIdx   = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val nameIdx     = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                val typeIdx     = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val durationIdx = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                val dateIdx     = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)

                while (cursor.moveToNext()) {
                    val id       = cursor.getLong(idIdx)
                    val number   = cursor.getString(numberIdx) ?: ""
                    val name     = cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }
                    val typeInt  = cursor.getInt(typeIdx)
                    val duration = cursor.getLong(durationIdx).coerceAtLeast(0L)
                    val dateLong = cursor.getLong(dateIdx)

                    records.add(
                        CallRecord(
                            id              = id,
                            number          = number,
                            contactName     = name,
                            contactId       = null, // resolved later via ContactsReader if needed
                            type            = mapCallType(typeInt),
                            durationSeconds = duration,
                            timestamp       = Instant.ofEpochMilli(dateLong)
                        )
                    )
                }
                records
            } ?: emptyList()
        } catch (e: SecurityException) {
            // Permission revoked between check and query
            Log.w(TAG, "READ_CALL_LOG permission not available during query")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error reading call log", e)
            emptyList()
        }
    }

    private fun mapCallType(androidType: Int): CallType = when (androidType) {
        CallLog.Calls.INCOMING_TYPE  -> CallType.INCOMING
        CallLog.Calls.OUTGOING_TYPE  -> CallType.OUTGOING
        CallLog.Calls.MISSED_TYPE    -> CallType.MISSED
        CallLog.Calls.REJECTED_TYPE  -> CallType.REJECTED
        CallLog.Calls.BLOCKED_TYPE   -> CallType.BLOCKED
        else                         -> CallType.UNKNOWN
    }
}
