package com.connectlens.app.data.platform

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.connectlens.app.domain.model.Contact
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads contact data from Android's Contacts ContentProvider.
 *
 * Requires READ_CONTACTS permission; the caller is responsible for verifying
 * this before invoking [readContacts].
 *
 * Strategy:
 * 1. Query [ContactsContract.Contacts] to get IDs and display names.
 * 2. Query [ContactsContract.CommonDataKinds.Phone] to get phone numbers.
 * 3. Join in memory (avoids a heavy JOIN query on the content provider).
 */
@Singleton
class ContactsReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ContactsReader"
    }

    /**
     * Returns all contacts with at least one phone number.
     * Returns an empty list on permission denial or ContentProvider error.
     */
    fun readContacts(): List<Contact> {
        return try {
            // Step 1: Read all contacts
            val contactMap = mutableMapOf<Long, Pair<String, MutableList<String>>>()
            val contactProjection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            )
            context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                contactProjection,
                null, null,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { cursor ->
                val idIdx   = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                while (cursor.moveToNext()) {
                    val id   = cursor.getLong(idIdx)
                    val name = cursor.getString(nameIdx) ?: "Unknown"
                    contactMap[id] = Pair(name, mutableListOf())
                }
            }

            // Step 2: Read all phone numbers
            val phoneProjection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                null, null, null
            )?.use { cursor ->
                val contactIdIdx  = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val normalizedIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
                val rawIdx        = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val contactId  = cursor.getLong(contactIdIdx)
                    // Prefer normalised (E.164) number; fall back to raw
                    val number     = cursor.getString(normalizedIdx)
                        ?: cursor.getString(rawIdx)
                        ?: continue
                    contactMap[contactId]?.second?.add(number)
                }
            }

            // Step 3: Build domain objects (only contacts with ≥ 1 phone number)
            contactMap
                .filter { (_, pair) -> pair.second.isNotEmpty() }
                .map { (id, pair) ->
                    Contact(
                        id           = id,
                        displayName  = pair.first,
                        phoneNumbers = pair.second.distinct()
                    )
                }
        } catch (e: SecurityException) {
            Log.w(TAG, "READ_CONTACTS permission not available during query")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error reading contacts", e)
            emptyList()
        }
    }
}
