package com.connectlens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.connectlens.app.core.designsystem.ConnectLensTheme
import com.connectlens.app.data.repository.UserPreferencesRepository
import com.connectlens.app.navigation.ConnectLensNavGraph
import com.connectlens.app.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ConnectLensTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Collect onboarding state. Null = still loading from DataStore.
                    val onboardingCompleted by userPreferencesRepository
                        .isOnboardingCompleted
                        .collectAsState(initial = null)

                    when (val completed = onboardingCompleted) {
                        null -> {
                            // Brief loading while DataStore initialises
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {
                            ConnectLensNavGraph(
                                startDestination = if (completed) {
                                    Screen.Dashboard.route
                                } else {
                                    Screen.Onboarding.route
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
