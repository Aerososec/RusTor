package com.rustor.feature.showcase.ui.showcaseFeed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rustor.core.ui.components.card.RusTorAppCardSkeleton
import com.rustor.core.ui.components.skeleton.RusTorSkeleton
import com.rustor.core.ui.theme.RusTorTheme

@Composable
internal fun ShowcaseFeedSkeleton(modifier: Modifier = Modifier) {
    RusTorSkeleton(modifier = modifier.fillMaxSize()) {
        RusTorAppCardSkeleton(
            modifier = Modifier.padding(horizontal = RusTorTheme.spacing.lg),
        )
    }
}
