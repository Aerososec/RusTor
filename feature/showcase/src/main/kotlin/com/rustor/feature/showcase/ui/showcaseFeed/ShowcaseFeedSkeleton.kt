package com.rustor.feature.showcase.ui.showcaseFeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rustor.core.ui.components.card.RusTorAppCardSkeleton
import com.rustor.core.ui.components.skeleton.RusTorSkeleton
import com.rustor.core.ui.theme.RusTorTheme

private val CategoryRowHeight = 48.dp
private val CategoryChipHeight = 32.dp
private val CategoryChipWidths = listOf(52.dp, 112.dp, 68.dp, 108.dp)

@Composable
internal fun ShowcaseFeedSkeleton(showCategoryChips: Boolean, modifier: Modifier = Modifier) {
    RusTorSkeleton(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showCategoryChips) {
                CategoryChipsSkeleton()
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(RusTorTheme.spacing.lg),
                contentAlignment = BiasAlignment(
                    horizontalBias = 0f,
                    verticalBias = -0.5f,
                ),
            ) {
                RusTorAppCardSkeleton(
                    modifier = Modifier.fillMaxWidth(),
                    coverAspectRatio = 4f / 5f,
                )
            }
        }
    }
}

@Composable
private fun CategoryChipsSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CategoryRowHeight)
            .padding(horizontal = RusTorTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(RusTorTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryChipWidths.forEach { chipWidth ->
            Box(
                modifier = Modifier
                    .width(chipWidth)
                    .height(CategoryChipHeight)
                    .background(
                        color = RusTorTheme.colors.surfaceContainerHighest,
                        shape = RoundedCornerShape(RusTorTheme.radius.full),
                    ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 700)
@Composable
private fun ShowcaseLoadingPreview() {
    RusTorTheme {
        Surface(color = RusTorTheme.colors.surface) {
            ShowcaseFeedSkeleton(showCategoryChips = true)
        }
    }
}
