package com.connectlens.app.domain.model

/**
 * A contact resolved from Android's Contacts ContentProvider.
 *
 * @param id           Android Contacts _ID.
 * @param displayName  Primary display name.
 * @param phoneNumbers All phone numbers associated with this contact
 *                     (normalised where available, raw otherwise).
 */
data class Contact(
    val id: Long,
    val displayName: String,
    val phoneNumbers: List<String>
)
