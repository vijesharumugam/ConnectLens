package com.connectlens.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.connectlens.app.data.local.entity.ContactCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactCacheDao {

    /** Returns all cached contacts ordered alphabetically. */
    @Query("SELECT * FROM contact_cache ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<ContactCacheEntity>>

    /** Returns a single contact by its Android Contacts _ID, or null. */
    @Query("SELECT * FROM contact_cache WHERE id = :contactId LIMIT 1")
    suspend fun getById(contactId: Long): ContactCacheEntity?

    /** Upserts contacts; duplicate IDs replace existing rows. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactCacheEntity>)

    /** Deletes every cached contact. */
    @Query("DELETE FROM contact_cache")
    suspend fun deleteAll()

    /** Row count. */
    @Query("SELECT COUNT(*) FROM contact_cache")
    suspend fun count(): Int
}
