package com.connectlens.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.connectlens.app.data.local.entity.CallRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordDao {

    /**
     * Returns call records within the epoch-millisecond range [startMs, endMs],
     * newest first.
     */
    @Query(
        """
        SELECT * FROM call_records
        WHERE timestampMs >= :startMs AND timestampMs <= :endMs
        ORDER BY timestampMs DESC
        """
    )
    fun getCallRecords(startMs: Long, endMs: Long): Flow<List<CallRecordEntity>>

    @Query("SELECT * FROM call_records ORDER BY timestampMs DESC")
    suspend fun getAllCallRecords(): List<CallRecordEntity>

    @Query("SELECT * FROM call_records WHERE timestampMs >= :startMs AND timestampMs <= :endMs ORDER BY timestampMs DESC")
    suspend fun getCallRecordsInRange(startMs: Long, endMs: Long): List<CallRecordEntity>

    /** Upserts records; duplicate IDs replace existing rows. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<CallRecordEntity>)

    /** Deletes every cached call record. */
    @Query("DELETE FROM call_records")
    suspend fun deleteAll()

    /** Row count; useful for deciding whether a refresh is needed. */
    @Query("SELECT COUNT(*) FROM call_records")
    suspend fun count(): Int

    /** Returns the most recent [timestampMs], or null if the table is empty. */
    @Query("SELECT MAX(timestampMs) FROM call_records")
    suspend fun getLatestTimestampMs(): Long?
}

