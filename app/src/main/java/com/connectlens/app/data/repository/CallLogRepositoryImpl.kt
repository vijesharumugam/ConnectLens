package com.connectlens.app.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.connectlens.app.data.local.db.CallRecordDao
import com.connectlens.app.data.local.entity.CallRecordEntity
import com.connectlens.app.data.platform.CallLogReader
import com.connectlens.app.domain.model.CallRecord
import com.connectlens.app.domain.model.CallType
import com.connectlens.app.domain.model.TimeRange
import com.connectlens.app.domain.repository.CallLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [CallLogRepository].
 *
 * Data flow:
 * - On each [getCallRecords] call, reads directly from the system ContentProvider
 *   (always fresh, no stale cache issue).
 * - [refreshCallRecords] syncs ALL_TIME records to Room for potential offline use.
 * - Room is used only for [refreshCallRecords] persistence, not as the primary source.
 *
 * This approach avoids double-counting: we don't cache during normal reads,
 * only during explicit refresh.
 */
@Singleton
class CallLogRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callLogReader: CallLogReader,
    private val callRecordDao: CallRecordDao
) : CallLogRepository {

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

    override fun getCallRecords(timeRange: TimeRange): Flow<List<CallRecord>> = flow {
        if (!hasPermission()) {
            emit(emptyList())
            return@flow
        }

        // 1. Fetch system call log records for the requested range (or ALL_TIME to maximize sync)
        val systemRecords = callLogReader.readCallLog(TimeRange.ALL_TIME)

        // 2. Persist newly fetched system records to Room database
        if (systemRecords.isNotEmpty()) {
            runCatching {
                callRecordDao.insertAll(systemRecords.map { it.toEntity() })
            }
        }

        // 3. Read cached entities from Room database
        val dbEntities = callRecordDao.getAllCallRecords()
        val dbRecords = dbEntities.map { it.toDomain() }

        // 4. Combine and deduplicate system + DB records
        val combinedMap = LinkedHashMap<String, CallRecord>()
        (systemRecords + dbRecords).forEach { record ->
            val key = "${record.number}_${record.timestamp.toEpochMilli()}_${record.durationSeconds}_${record.type}"
            if (!combinedMap.containsKey(key)) {
                combinedMap[key] = record
            }
        }

        val allRecords = combinedMap.values.sortedByDescending { it.timestamp }

        // 5. Apply time range filtering
        val (start, end) = timeRange.toDateRange()
        val startMs = start.toInstant().toEpochMilli()
        val endMs   = end.toInstant().toEpochMilli()

        val filteredRecords = if (timeRange == TimeRange.ALL_TIME) {
            allRecords
        } else {
            allRecords.filter { record ->
                val ts = record.timestamp.toEpochMilli()
                ts in startMs..endMs
            }
        }

        emit(filteredRecords)
    }.flowOn(Dispatchers.IO)

    override suspend fun refreshCallRecords() {
        if (!hasPermission()) return
        withContext(Dispatchers.IO) {
            val records  = callLogReader.readCallLog(TimeRange.ALL_TIME)
            val entities = records.map { it.toEntity() }
            callRecordDao.insertAll(entities)
        }
    }

    override suspend fun clearLocalData() {
        withContext(Dispatchers.IO) {
            callRecordDao.deleteAll()
        }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun CallRecord.toEntity() = CallRecordEntity(
        id              = id,
        number          = number,
        contactName     = contactName,
        contactId       = contactId,
        callType        = type.name,
        durationSeconds = durationSeconds,
        timestampMs     = timestamp.toEpochMilli()
    )

    private fun CallRecordEntity.toDomain() = CallRecord(
        id              = id,
        number          = number,
        contactName     = contactName,
        contactId       = contactId,
        type            = runCatching { CallType.valueOf(callType) }.getOrDefault(CallType.UNKNOWN),
        durationSeconds = durationSeconds,
        timestamp       = Instant.ofEpochMilli(timestampMs)
    )
}
