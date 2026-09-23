package com.connectlens.app.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.connectlens.app.data.local.db.ContactCacheDao
import com.connectlens.app.data.local.entity.ContactCacheEntity
import com.connectlens.app.data.platform.ContactsReader
import com.connectlens.app.domain.model.Contact
import com.connectlens.app.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactsReader: ContactsReader,
    private val contactCacheDao: ContactCacheDao
) : ContactRepository {

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

    override fun getContacts(): Flow<List<Contact>> = flow {
        if (!hasPermission()) {
            emit(emptyList())
            return@flow
        }
        val contacts = contactsReader.readContacts()
        emit(contacts)
    }.flowOn(Dispatchers.IO)

    override suspend fun getContactByNumber(phoneNumber: String): Contact? {
        if (!hasPermission()) return null
        return withContext(Dispatchers.IO) {
            val normalised = normalise(phoneNumber)
            contactsReader.readContacts().firstOrNull { contact ->
                contact.phoneNumbers.any { normalise(it) == normalised }
            }
        }
    }

    override suspend fun refreshContacts() {
        if (!hasPermission()) return
        withContext(Dispatchers.IO) {
            val contacts = contactsReader.readContacts()
            val entities = contacts.map { it.toEntity() }
            contactCacheDao.deleteAll()
            contactCacheDao.insertAll(entities)
        }
    }

    override suspend fun clearLocalData() {
        withContext(Dispatchers.IO) {
            contactCacheDao.deleteAll()
        }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun Contact.toEntity() = ContactCacheEntity(
        id           = id,
        displayName  = displayName,
        phoneNumbers = JSONArray(phoneNumbers).toString(),
        lastUpdated  = Instant.now().toEpochMilli()
    )

    private fun ContactCacheEntity.toDomain(): Contact {
        val arr     = JSONArray(phoneNumbers)
        val numbers = (0 until arr.length()).map { arr.getString(it) }
        return Contact(id = id, displayName = displayName, phoneNumbers = numbers)
    }

    /**
     * Strips formatting from a phone number and keeps the last 10 digits.
     * This is a simple heuristic; production apps would use libphonenumber.
     */
    private fun normalise(number: String): String {
        val digits = number.replace(Regex("[^+0-9]"), "")
        return if (digits.length > 10) digits.takeLast(10) else digits
    }
}
