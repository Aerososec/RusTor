package com.rustor.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.onboarding.ui.OnboardingIntent
import com.rustor.feature.onboarding.ui.OnboardingUiState
import com.rustor.feature.onboarding.viewModel.OnboardingViewModel

private val ContentMaxWidth = 480.dp
private val ContinueButtonMinHeight = 56.dp
private val SavingIndicatorSize = 20.dp
private val SavingIndicatorStrokeWidth = 2.dp

@Composable
fun OnboardingRoute(modifier: Modifier = Modifier) {
    val viewModel = hiltViewModel<OnboardingViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingScreen(
    uiState: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSaving = uiState == OnboardingUiState.Saving

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RusTorTheme.colors.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(RusTorTheme.spacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_brand),
                    style = RusTorTheme.typography.displayMedium,
                    color = RusTorTheme.colors.primary,
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(RusTorTheme.spacing.xxxl),
                )

                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = RusTorTheme.typography.headlineMedium,
                    color = RusTorTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() },
                )

                Spacer(
                    modifier = Modifier.height(RusTorTheme.spacing.lg),
                )

                Text(
                    text = stringResource(R.string.onboarding_description),
                    style = RusTorTheme.typography.bodyLarge,
                    color = RusTorTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(
                    modifier = Modifier.height(RusTorTheme.spacing.xxxl),
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(
                        RusTorTheme.spacing.md,
                    ),
                ) {
                    if (uiState == OnboardingUiState.SaveFailed) {
                        Text(
                            text = stringResource(
                                R.string.onboarding_save_error,
                            ),
                            style = RusTorTheme.typography.bodyMedium,
                            color = RusTorTheme.colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    liveRegion = LiveRegionMode.Polite
                                },
                        )
                    }

                    OnboardingContinueButton(
                        isSaving = isSaving,
                        onClick = {
                            onIntent(OnboardingIntent.ContinueClicked)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingContinueButton(isSaving: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = modifier.heightIn(min = ContinueButtonMinHeight),
        shape = RoundedCornerShape(RusTorTheme.radius.full),
        contentPadding = PaddingValues(
            horizontal = RusTorTheme.spacing.xl,
            vertical = RusTorTheme.spacing.md,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = RusTorTheme.colors.primary,
            contentColor = RusTorTheme.colors.onPrimary,
        ),
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(SavingIndicatorSize),
                color = RusTorTheme.colors.primary,
                strokeWidth = SavingIndicatorStrokeWidth,
            )

            Spacer(
                modifier = Modifier.width(RusTorTheme.spacing.sm),
            )
        }

        Text(
            text = stringResource(
                if (isSaving) {
                    R.string.onboarding_saving
                } else {
                    R.string.onboarding_continue
                },
            ),
            style = RusTorTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}
