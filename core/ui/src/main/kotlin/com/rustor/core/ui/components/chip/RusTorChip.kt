package com.rustor.core.ui.components.chip

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.theme.RusTorTheme

@Composable
fun RusTorFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        label = {
            Text(
                text = text,
                style = RusTorTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        shape = RoundedCornerShape(RusTorTheme.radius.full),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = RusTorTheme.colors.surfaceContainerHigh,
            labelColor = RusTorTheme.colors.onSurfaceVariant,
            selectedContainerColor = RusTorTheme.colors.primary,
            selectedLabelColor = RusTorTheme.colors.onPrimary,
        ),
        border = null,
    )
}

@Composable
fun RusTorAssistChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
) {
    AssistChip(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        label = {
            Text(
                text = text,
                style = RusTorTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingIcon = trailingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            }
        },
        shape = RoundedCornerShape(RusTorTheme.radius.full),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = RusTorTheme.colors.surfaceContainerHigh,
            labelColor = RusTorTheme.colors.onSurfaceVariant,
            trailingIconContentColor = RusTorTheme.colors.onSurfaceVariant,
        ),
        border = null,
    )
}

@Composable
fun RusTorCategoryTag(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(RusTorTheme.radius.full),
        color = RusTorTheme.colors.primaryContainer,
        contentColor = RusTorTheme.colors.onPrimaryContainer,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                PaddingValues(
                    horizontal = RusTorTheme.spacing.md,
                    vertical = RusTorTheme.spacing.xs,
                ),
            ),
            style = RusTorTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@ThemePreviews
@Composable
private fun RusTorChipsPreview() {
    val categories = listOf("Все", "Финансы", "Инструменты", "Игры")
    var selected by remember { mutableStateOf("Все") }

    RusTorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(vertical = RusTorTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.lg),
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = RusTorTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.sm),
                ) {
                    categories.forEach { category ->
                        RusTorFilterChip(
                            text = category,
                            selected = category == selected,
                            onClick = { selected = category },
                        )
                    }
                    RusTorAssistChip(
                        text = "Все категории",
                        onClick = {},
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    )
                }
                Row(
                    modifier = Modifier.padding(horizontal = RusTorTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.sm),
                ) {
                    RusTorCategoryTag(text = "Финансы")
                    RusTorCategoryTag(text = "Путешествия")
                    RusTorCategoryTag(text = "Игры")
                }
            }
        }
    }
}
