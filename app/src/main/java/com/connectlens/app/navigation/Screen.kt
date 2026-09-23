package com.connectlens.app.navigation

/**
 * Sealed class that defines every destination in ConnectLens.
 *
 * Route strings are kept simple (no query params) for predictable back-stack
 * behaviour. Contact detail uses path segments for contactId and an optional
 * phoneNumber query param.
 */
sealed class Screen(val route: String) {
    data object Onboarding    : Screen("onboarding")
    data object Dashboard     : Screen("dashboard")
    data object ContactList   : Screen("contacts")
    data object CallHistory   : Screen("call_history")
    data object Analytics     : Screen("analytics")
    data object Settings      : Screen("settings")

    /**
     * Contact detail: route template is "contacts/{contactId}?phone={phoneNumber}"
     *
     * [contactId]   – Android Contacts _ID as a string, or "null" when unknown.
     * [phoneNumber] – raw number string, or omitted when a contact ID is available.
     */
    data object ContactDetail : Screen("contacts/{contactId}?phone={phoneNumber}") {
        fun createRoute(contactId: Long?, phoneNumber: String?): String =
            "contacts/${contactId ?: "null"}?phone=${phoneNumber?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: ""}"
    }
}
