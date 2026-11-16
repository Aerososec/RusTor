package com.rustor.feature.onboarding.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.UserPreferencesException
import com.rustor.core.domain.repository.UserPreferencesRepository
import com.rustor.feature.onboarding.ui.OnboardingIntent
import com.rustor.feature.onboarding.ui.OnboardingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _uiState =
        MutableStateFlow<OnboardingUiState>(OnboardingUiState.Ready)

    val uiState = _uiState.asStateFlow()

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.ContinueClicked -> {
                continueClicked()
            }
        }
    }

    private fun continueClicked() {
        if (_uiState.value == OnboardingUiState.Saving) return

        _uiState.value = OnboardingUiState.Saving

        viewModelScope.launch {
            try {
                repository.completeOnboarding()
            } catch (error: UserPreferencesException) {
                Log.w(TAG, "Unable to complete onboarding", error)
                _uiState.value = OnboardingUiState.SaveFailed
            }
        }
    }

    private companion object {
        const val TAG = "OnboardingViewModel"
    }
}
