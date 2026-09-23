package com.connectlens.app.domain.repository

import com.connectlens.app.domain.model.Contact
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing contact data.
 *
 * The implementation reads from Android's Contacts ContentProvider
 * (requires READ_CONTACTS permission) and may cache results in Room.
 */
interface ContactRepository {
    /**
     * Returns a Flow of all contacts available to the app.
     * Emits an empty list immediately if the permission is not granted.
     */
    fun getContacts(): Flow<List<Contact>>

    /**
     * Attempts to find a contact whose phone numbers match [phoneNumber].
     * Returns null if no match is found or if the permission is not granted.
     */
    suspend fun getContactByNumber(phoneNumber: String): Contact?

    /**
     * Re-reads all contacts from the system and refreshes the local cache.
     * No-op if permission is not granted.
     */
    suspend fun refreshContacts()

    /**
     * Deletes all contact data cached by ConnectLens from Room.
     * Does NOT modify the Android system contacts.
     */
    suspend fun clearLocalData()

    /** Returns true if READ_CONTACTS permission is currently granted. */
    fun hasPermission(): Boolean
}
