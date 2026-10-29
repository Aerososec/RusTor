package com.rustor.core.ui.components.state

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rustor.core.ui.R
import com.rustor.core.ui.components.button.RusTorButton
import com.rustor.core.ui.theme.RusTorTheme

private val IllustrationSize = 96.dp
private val IllustrationIconSize = 48.dp
private val MessageMaxWidth = 320.dp

@Composable
fun RusTorLoadingState(modifier: Modifier = Modifier, text: String? = null) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.lg),
        ) {
            CircularProgressIndicator(
                color = RusTorTheme.colors.primary,
                trackColor = RusTorTheme.colors.surfaceContainerHigh,
            )
            if (text != null) {
                Text(
                    text = text,
                    style = RusTorTheme.typography.bodyMedium,
                    color = RusTorTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun RusTorErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.rustor_error_title),
    description: String = stringResource(R.string.rustor_error_description),
    actionText: String = stringResource(R.string.rustor_action_retry),
    icon: ImageVector = Icons.Outlined.Warning,
) {
    MessageState(
        icon = icon,
        title = title,
        description = description,
        actionText = actionText,
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
fun RusTorEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    MessageState(
        icon = icon,
        title = title,
        description = description,
        actionText = actionText,
        onAction = onAction,
        modifier = modifier,
    )
}

@Composable
private fun MessageState(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = MessageMaxWidth)
                .padding(
                    horizontal = RusTorTheme.spacing.xl,
                    vertical = RusTorTheme.spacing.xxl,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(IllustrationSize),
                shape = CircleShape,
                color = RusTorTheme.colors.primaryContainer,
                contentColor = RusTorTheme.colors.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(IllustrationIconSize),
                    )
                }
            }

            Spacer(modifier = Modifier.height(RusTorTheme.spacing.xl))

            Text(
                text = title,
                style = RusTorTheme.typography.titleMedium,
                color = RusTorTheme.colors.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(RusTorTheme.spacing.sm))

            Text(
                text = description,
                style = RusTorTheme.typography.bodySmall,
                color = RusTorTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(RusTorTheme.spacing.xl))
                RusTorButton(
                    text = actionText,
                    onClick = onAction,
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 480)
@Preview(name = "Dark", showBackground = true, heightDp = 480, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RusTorLoadingStatePreview() {
    RusTorTheme {
        Surface {
            RusTorLoadingState(text = "Загрузка…")
        }
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 480)
@Preview(name = "Dark", showBackground = true, heightDp = 480, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RusTorErrorStatePreview() {
    RusTorTheme {
        Surface {
            RusTorErrorState(onRetry = {})
        }
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 480)
@Preview(name = "Dark", showBackground = true, heightDp = 480, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RusTorEmptyStatePreview() {
    RusTorTheme {
        Surface {
            RusTorEmptyState(
                title = "Пока ничего нет",
                description = "В этой категории ещё нет приложений. Попробуйте другую категорию.",
                actionText = "Вернуться к витрине",
                onAction = {},
            )
        }
    }
}
