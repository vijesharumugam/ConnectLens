package com.connectlens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching a resolved contact.
 *
 * Phone numbers are stored as a JSON array string to avoid a separate
 * junction table for this MVP. The [lastUpdated] field is used to determine
 * cache freshness.
 *
 * Privacy note: Only display name and normalised phone numbers are stored.
 * No profile photos or sensitive contact fields are cached.
 */
@Entity(tableName = "contact_cache")
data class ContactCacheEntity(
    @PrimaryKey
    val id: Long,

    val displayName: String,

    /**
     * JSON array of phone number strings, e.g. ["+919876543210", "9876543210"].
     * Serialised/deserialised using org.json.JSONArray (no extra dependency).
     */
    val phoneNumbers: String,

    /** Epoch milliseconds when this entry was written. */
    val lastUpdated: Long
)
