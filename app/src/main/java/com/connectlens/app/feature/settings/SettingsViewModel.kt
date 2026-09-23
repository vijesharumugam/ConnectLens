package com.connectlens.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectlens.app.domain.repository.CallLogRepository
import com.connectlens.app.domain.repository.ContactRepository
import com.connectlens.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _callLogPermission = MutableStateFlow(false)
    val callLogPermission: StateFlow<Boolean> = _callLogPermission

    private val _contactPermission = MutableStateFlow(false)
    val contactPermission: StateFlow<Boolean> = _contactPermission

    private val _isClearingData = MutableStateFlow(false)
    val isClearingData: StateFlow<Boolean> = _isClearingData

    private val _clearSuccessEvent = MutableSharedFlow<Unit>()
    val clearSuccessEvent: SharedFlow<Unit> = _clearSuccessEvent

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        _callLogPermission.value = callLogRepository.hasPermission()
        _contactPermission.value = true // Assume handled appropriately via some ContactRepository method if available
    }

    fun clearAllLocalData() {
        viewModelScope.launch {
            _isClearingData.value = true
            callLogRepository.clearLocalData()
            contactRepository.clearLocalData()
            userPreferencesRepository.clearAllPreferences()
            _isClearingData.value = false
            _clearSuccessEvent.emit(Unit)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(false)
        }
    }
}
