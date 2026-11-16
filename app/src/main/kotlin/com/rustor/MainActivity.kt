package com.rustor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rustor.core.ui.components.state.RusTorErrorState
import com.rustor.core.ui.components.state.RusTorLoadingState
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.onboarding.OnboardingRoute
import com.rustor.navigation.RusTorNavDisplay
import com.rustor.ui.AppUiState
import com.rustor.viewModel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RusTorTheme {
                val viewModel = hiltViewModel<AppViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                when (uiState) {
                    AppUiState.Error -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = RusTorTheme.colors.background,
                        ) {
                            RusTorErrorState(
                                title = stringResource(R.string.startup_error_title),
                                description = stringResource(
                                    R.string.startup_error_description,
                                ),
                                onRetry = viewModel::retry,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .safeDrawingPadding(),
                            )
                        }
                    }

                    AppUiState.Loading -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = RusTorTheme.colors.background,
                        ) {
                            RusTorLoadingState(
                                text = stringResource(R.string.startup_loading),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .safeDrawingPadding(),
                            )
                        }
                    }

                    AppUiState.Main -> {
                        RusTorNavDisplay()
                    }

                    AppUiState.Onboarding -> {
                        OnboardingRoute()
                    }
                }
            }
        }
    }
}
