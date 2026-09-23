package com.connectlens.app.core.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Snapshot of permission states for the two permissions ConnectLens uses.
 */
@Stable
data class AppPermissionState(
    val hasCallLog: Boolean,
    val hasContacts: Boolean
) {
    val hasBoth: Boolean get() = hasCallLog && hasContacts
    val hasEither: Boolean get() = hasCallLog || hasContacts
}

/**
 * Remembers the current permission state and provides a launcher to request
 * both permissions together.
 *
 * Usage:
 * ```
 * val permState = rememberAppPermissionState()
 * Button(onClick = { permState.requestPermissions() }) { ... }
 * ```
 */
@Composable
fun rememberAppPermissionState(
    onResult: (callLog: Boolean, contacts: Boolean) -> Unit = { _, _ -> }
): ManagedPermissionState {
    val context = LocalContext.current

    fun checkCallLog() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALL_LOG
    ) == PackageManager.PERMISSION_GRANTED

    fun checkContacts() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CONTACTS
    ) == PackageManager.PERMISSION_GRANTED

    var permState by remember {
        mutableStateOf(
            AppPermissionState(
                hasCallLog = checkCallLog(),
                hasContacts = checkContacts()
            )
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val callLog  = results[Manifest.permission.READ_CALL_LOG] ?: checkCallLog()
        val contacts = results[Manifest.permission.READ_CONTACTS] ?: checkContacts()
        permState = AppPermissionState(hasCallLog = callLog, hasContacts = contacts)
        onResult(callLog, contacts)
    }

    return remember(permState) {
        ManagedPermissionState(
            state = permState,
            refreshState = {
                permState = AppPermissionState(
                    hasCallLog  = checkCallLog(),
                    hasContacts = checkContacts()
                )
            },
            requestBoth = {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.READ_CONTACTS
                    )
                )
            },
            requestCallLog = {
                launcher.launch(arrayOf(Manifest.permission.READ_CALL_LOG))
            },
            requestContacts = {
                launcher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
            }
        )
    }
}

/** Combines [AppPermissionState] with actions to request individual permissions. */
@Stable
class ManagedPermissionState(
    val state: AppPermissionState,
    val refreshState: () -> Unit,
    val requestBoth: () -> Unit,
    val requestCallLog: () -> Unit,
    val requestContacts: () -> Unit
)
