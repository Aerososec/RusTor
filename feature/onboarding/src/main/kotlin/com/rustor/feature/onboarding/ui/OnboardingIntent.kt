package com.rustor.feature.onboarding.ui

sealed interface OnboardingIntent {
    data object ContinueClicked : OnboardingIntent
}
