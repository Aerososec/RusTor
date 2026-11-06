package com.rustor.core.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import com.rustor.core.ui.R
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.theme.RusTorTheme

private val CategoryIconContainerSize = 48.dp
private val CategoryIconSize = 24.dp
private val CategoryArrowSize = 24.dp

@Composable
fun RusTorCategoryCard(
    title: String,
    appCount: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(RusTorTheme.radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = RusTorTheme.colors.surfaceContainerLow,
            contentColor = RusTorTheme.colors.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RusTorTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(
                RusTorTheme.spacing.md,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(CategoryIconContainerSize),
                shape = RoundedCornerShape(RusTorTheme.radius.md),
                color = RusTorTheme.colors.primaryContainer,
                contentColor = RusTorTheme.colors.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(CategoryIconSize),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    RusTorTheme.spacing.xs,
                ),
            ) {
                Text(
                    text = title,
                    style = RusTorTheme.typography.titleMedium,
                )

                Text(
                    text = pluralStringResource(
                        R.plurals.rustor_category_app_count,
                        appCount,
                        appCount,
                    ),
                    style = RusTorTheme.typography.bodyMedium,
                    color = RusTorTheme.colors.onSurfaceVariant,
                )
            }

            Icon(
                imageVector =
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = RusTorTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(CategoryArrowSize),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RusTorCategoryCardPreview() {
    RusTorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(RusTorTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(
                    RusTorTheme.spacing.md,
                ),
            ) {
                RusTorCategoryCard(
                    title = "Финансы",
                    appCount = 21,
                    icon = Icons.Filled.Star,
                    onClick = {},
                )

                RusTorCategoryCard(
                    title = "Государственные",
                    appCount = 5,
                    icon = Icons.Filled.Star,
                    onClick = {},
                )

                RusTorCategoryCard(
                    title = "Транспорт",
                    appCount = 0,
                    icon = Icons.Filled.Star,
                    onClick = {},
                )
            }
        }
    }
}
