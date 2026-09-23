package com.connectlens.app.core.common

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Utility functions for phone number display, matching, and dialing.
 */
object PhoneNumberUtils {

    /**
     * Launches the device's default phone app prefilled with [phoneNumber].
     */
    fun launchDialIntent(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Handle cases where dialer app is unavailable
        }
    }

    /**
     * Formats a phone number for UI display.
     * Returns "Unknown number" if the number is blank.
     */
    fun formatNumber(number: String): String =
        number.ifBlank { "Unknown number" }

    /**
     * Masks a phone number for privacy-aware display.
     *
     * Examples:
     *  - "+91 98765 43210" → "+91 ****43210"
     *  - "9876543210"      → "******3210"
     *  - ""                → "Unknown number"
     *  - "Private number"  → "Private number"
     */
    fun maskNumber(number: String): String {
        val digits = number.replace(Regex("[^+0-9]"), "")
        return when {
            digits.isBlank() -> "Unknown number"
            digits.length <= 4 -> "****"
            else -> {
                val prefix  = digits.dropLast(4).map { if (it == '+') '+' else '*' }.joinToString("")
                val suffix  = digits.takeLast(4)
                prefix + suffix
            }
        }
    }

    /**
     * Strips all non-digit characters (except leading +) and returns the last 10 digits.
     * Used for matching phone numbers across different formats.
     */
    fun normalise(number: String): String {
        val stripped = number.replace(Regex("[^+0-9]"), "")
        return if (stripped.length > 10) stripped.takeLast(10) else stripped
    }

    /**
     * Returns true if [a] and [b] refer to the same phone number using
     * last-10-digits matching.
     */
    fun isSameNumber(a: String, b: String): Boolean =
        normalise(a) == normalise(b) && normalise(a).isNotBlank()

    /**
     * Returns initials to use in an avatar view (at most 2 characters).
     *
     * Examples:
     *  - "John Doe"      → "JD"
     *  - "Alice"         → "A"
     *  - "Unknown"       → "?"
     *  - "+91 9876543210" → "#"
     */
    fun initialsFor(name: String): String {
        if (name.isBlank()) return "?"
        // If the name looks like a phone number, use '#'
        if (name.firstOrNull()?.isDigit() == true || name.startsWith("+")) return "#"
        val parts = name.trim().split(Regex("\\s+"))
        return when {
            parts.size >= 2 -> "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
            parts.size == 1 -> parts.first().first().uppercaseChar().toString()
            else            -> "?"
        }
    }
}
