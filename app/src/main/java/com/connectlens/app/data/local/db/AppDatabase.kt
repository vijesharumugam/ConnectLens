package com.connectlens.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.connectlens.app.data.local.entity.CallRecordEntity
import com.connectlens.app.data.local.entity.ContactCacheEntity

/**
 * ConnectLens Room database.
 *
 * Version history:
 *  1 – Initial schema (call_records, contact_cache).
 *
 * Migrations: version 1 is the first release. Future migrations should be
 * added via [androidx.room.migration.Migration] rather than using
 * fallbackToDestructiveMigration, which discards user data.
 *
 * exportSchema = false: schema JSON files are not needed for this MVP.
 * Set to true and add a schema export directory if you add CI migration tests.
 */
@Database(
    entities = [CallRecordEntity::class, ContactCacheEntity::class],
    version  = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callRecordDao(): CallRecordDao
    abstract fun contactCacheDao(): ContactCacheDao
}
