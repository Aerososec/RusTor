package com.rustor.feature.appdetails.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.appdetails.R
import com.rustor.core.ui.R as CoreUiR

internal const val SCREENSHOT_REFRESH_AREA_TAG = "screenshot_refresh_area"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RusTorScreenshotViewer(
    screenshotUrls: List<String>,
    initialIndex: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBackClick)

    key(screenshotUrls, initialIndex) {
        val pagerState = rememberPagerState(
            initialPage = if (screenshotUrls.isEmpty()) {
                0
            } else {
                initialIndex.coerceIn(screenshotUrls.indices)
            },
            pageCount = { screenshotUrls.size },
        )

        val attempts = remember { mutableStateMapOf<Int, Int>() }
        val pendingRefreshes = remember { mutableStateMapOf<Int, Int>() }

        val refreshCurrentScreenshot: () -> Unit = {
            val page = pagerState.currentPage
            if (page in screenshotUrls.indices && page !in pendingRefreshes) {
                val attempt = (attempts[page] ?: 0) + 1
                pendingRefreshes[page] = attempt
                attempts[page] = attempt
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = RusTorTheme.colors.surface,
            topBar = {
                ScreenshotViewerTopBar(
                    currentPage = pagerState.currentPage,
                    pageCount = screenshotUrls.size,
                    isRefreshing = pagerState.currentPage in pendingRefreshes,
                    onBackClick = onBackClick,
                    onRefreshClick = refreshCurrentScreenshot,
                )
            },
        ) { innerPadding ->
            if (screenshotUrls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(
                            R.string.rustor_no_screenshots,
                        ),
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = pagerState.currentPage in pendingRefreshes,
                    onRefresh = refreshCurrentScreenshot,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                        .testTag(SCREENSHOT_REFRESH_AREA_TAG),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        val attempt = attempts[page] ?: 0

                        RusTorScreenshotImage(
                            url = screenshotUrls[page],
                            contentDescription = stringResource(
                                R.string.rustor_screenshot_position,
                                page + 1,
                                screenshotUrls.size,
                            ),
                            retryKey = attempt,
                            onLoadFinished = {
                                if (pendingRefreshes[page] == attempt) {
                                    pendingRefreshes.remove(page)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .scrollable(
                                    state = rememberScrollableState { 0f },
                                    orientation = Orientation.Vertical,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenshotViewerTopBar(
    currentPage: Int,
    pageCount: Int,
    isRefreshing: Boolean,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
) {
    TopAppBar(
        title = {
            val title = if (pageCount == 0) {
                stringResource(R.string.rustor_screenshots)
            } else {
                stringResource(
                    R.string.rustor_screenshot_position,
                    currentPage + 1,
                    pageCount,
                )
            }
            Text(
                text = title,
                style = RusTorTheme.typography.titleMedium,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(CoreUiR.string.rustor_action_back),
                )
            }
        },
        actions = {
            if (pageCount > 0) {
                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isRefreshing,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.rustor_reload_screenshot),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = RusTorTheme.colors.surface,
        ),
    )
}
