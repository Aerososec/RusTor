package com.rustor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.feature.showcase.ShowcaseRoute
import com.rustor.feature.showcase.uiState.ShowcaseMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RusTorTheme {
                ShowcaseRoute(
                    mode = ShowcaseMode.Main,
                    onAppClick = {},
                    onSearchClick = {},
                    onAllCategoriesClick = {},
                    onBackClick = { finish() },
                )
            }
        }
    }
}
