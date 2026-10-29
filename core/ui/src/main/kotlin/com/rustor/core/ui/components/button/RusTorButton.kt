package com.rustor.core.ui.components.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.theme.RusTorTheme

@Composable
fun RusTorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
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
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        }
        Text(
            text = text,
            style = RusTorTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RusTorTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(RusTorTheme.radius.full),
        colors = ButtonDefaults.textButtonColors(
            contentColor = RusTorTheme.colors.primary,
        ),
    ) {
        Text(
            text = text,
            style = RusTorTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@ThemePreviews
@Composable
private fun RusTorButtonPreview() {
    RusTorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(RusTorTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
            ) {
                RusTorButton(text = "Вернуться к витрине", onClick = {})
                RusTorButton(text = "Повторить", onClick = {}, leadingIcon = Icons.Filled.Refresh)
                RusTorButton(text = "Повторить", onClick = {}, enabled = false)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun RusTorTextButtonPreview() {
    RusTorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(RusTorTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.md),
            ) {
                RusTorTextButton(text = "Отмена", onClick = {})
                RusTorTextButton(text = "Отмена", onClick = {}, enabled = false)
            }
        }
    }
}
