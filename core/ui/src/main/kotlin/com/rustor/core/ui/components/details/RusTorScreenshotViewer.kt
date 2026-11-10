package com.rustor.core.ui.components.details

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rustor.core.ui.R
import com.rustor.core.ui.theme.RusTorTheme

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

        var retryKey by rememberSaveable { mutableIntStateOf(0) }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = RusTorTheme.colors.surface,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (screenshotUrls.isEmpty()) {
                                stringResource(R.string.rustor_screenshots)
                            } else {
                                stringResource(
                                    R.string.rustor_screenshot_position,
                                    pagerState.currentPage + 1,
                                    screenshotUrls.size,
                                )
                            },
                            style = RusTorTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(
                                    R.string.rustor_action_back,
                                ),
                            )
                        }
                    },
                    actions = {
                        if (screenshotUrls.isNotEmpty()) {
                            IconButton(onClick = { retryKey++ }) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = stringResource(
                                        R.string.rustor_reload_screenshot,
                                    ),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = RusTorTheme.colors.surface,
                    ),
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                ) { page ->
                    RusTorScreenshotImage(
                        url = screenshotUrls[page],
                        contentDescription = stringResource(
                            R.string.rustor_screenshot_position,
                            page + 1,
                            screenshotUrls.size,
                        ),
                        retryKey = if (page == pagerState.currentPage) {
                            retryKey
                        } else {
                            0
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
