package com.rustor.feature.showcase.uiState

data class ShowcaseUiState(val header: ShowcaseHeader, val feed: FeedState, val refreshRevision: Long = 0L)
