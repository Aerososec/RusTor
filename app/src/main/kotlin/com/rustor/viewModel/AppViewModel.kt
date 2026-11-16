package com.rustor.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rustor.core.common.error.UserPreferencesException
import com.rustor.core.domain.repository.UserPreferencesRepository
import com.rustor.ui.AppUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AppUiState>(AppUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeOnboarding()
    }

    fun retry() {
        observeOnboarding()
    }

    private fun observeOnboarding() {
        if (observationJob?.isActive == true) return

        _uiState.value = AppUiState.Loading

        observationJob = viewModelScope.launch {
            try {
                repository.onboardingCompleted.collect { completed ->
                    _uiState.value = if (completed) {
                        AppUiState.Main
                    } else {
                        AppUiState.Onboarding
                    }
                }
            } catch (error: UserPreferencesException) {
                Log.w(TAG, "Unable to read onboarding completion", error)
                _uiState.value = AppUiState.Error
            }
        }
    }

    private companion object {
        const val TAG = "AppViewModel"
    }
}
