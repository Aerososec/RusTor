package com.rustor.feature.onboarding.ui

sealed interface OnboardingUiState {
    data object Ready : OnboardingUiState

    data object Saving : OnboardingUiState

    data object SaveFailed : OnboardingUiState
}
