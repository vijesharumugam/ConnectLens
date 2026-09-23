package com.connectlens.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val callLogPermission by viewModel.callLogPermission.collectAsStateWithLifecycle()
    val contactPermission by viewModel.contactPermission.collectAsStateWithLifecycle()
    val isClearingData by viewModel.isClearingData.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.clearSuccessEvent.collect {
            snackbarHostState.showSnackbar("Data cleared successfully")
        }
    }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showResetOnboardingDialog by remember { mutableStateOf(false) }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Local Analytics Cache") },
            text = { Text("Are you sure you want to clear all local data? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDataDialog = false
                    viewModel.clearAllLocalData()
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showResetOnboardingDialog = false },
            title = { Text("Reset Onboarding") },
            text = { Text("Are you sure you want to reset onboarding?") },
            confirmButton = {
                TextButton(onClick = {
                    showResetOnboardingDialog = false
                    viewModel.resetOnboarding()
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetOnboardingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Permissions", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("READ_CALL_LOG: ${if (callLogPermission) "Granted ✅" else "Denied ❌"}")
                        if (!callLogPermission) {
                            Button(onClick = { /* Open Settings */ }, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Open Settings")
                            }
                        }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("READ_CONTACTS: ${if (contactPermission) "Granted ✅" else "Denied ❌"}")
                    }
                }
            }

            item {
                Text("Data Management", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                ListItem(
                    headlineContent = { Text("Clear Local Analytics Cache") },
                    modifier = Modifier.clickable { showClearDataDialog = true }
                )
                ListItem(
                    headlineContent = { Text("Reset Onboarding") },
                    modifier = Modifier.clickable { showResetOnboardingDialog = true }
                )
                ListItem(
                    headlineContent = { Text("What data ConnectLens stores") }
                )
            }

            item {
                Text("Privacy Information", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Your call data never leaves your device", modifier = Modifier.padding(16.dp))
                }
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("ConnectLens cannot delete your phone system call history", modifier = Modifier.padding(16.dp))
                }
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("You can revoke permissions anytime in Android Settings", modifier = Modifier.padding(16.dp))
                }
            }

            item {
                Text("About", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                ListItem(
                    headlineContent = { Text("App Version") },
                    supportingContent = { Text("1.0.0") } // BuildConfig.VERSION_NAME placeholder
                )
                ListItem(
                    headlineContent = { Text("Privacy Policy") }
                )
            }
        }
        
        if (isClearingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
