package com.connectlens.app.domain.repository

import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing and managing call-log data.
 *
 * The implementation reads from Android's CallLog ContentProvider
 * (requires READ_CALL_LOG permission) and may cache results in Room.
 *
 * All operations are non-blocking and run on IO dispatchers inside the impl.
 */
interface CallLogRepository {
    /**
     * Returns a Flow of [CallRecord] lists filtered to [timeRange].
     * Emits an empty list immediately if the permission is not granted.
     */
    fun getCallRecords(timeRange: TimeRange): Flow<List<CallRecord>>

    /**
     * Re-reads all available call records from the system and updates the
     * local Room cache. No-op if permission is not granted.
     */
    suspend fun refreshCallRecords()

    /**
     * Deletes all call records cached by ConnectLens from Room.
     * Does NOT delete records from the Android system call log.
     */
    suspend fun clearLocalData()

    /** Returns true if READ_CALL_LOG permission is currently granted. */
    fun hasPermission(): Boolean
}
