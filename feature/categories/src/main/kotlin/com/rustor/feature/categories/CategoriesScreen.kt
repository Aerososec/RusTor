package com.rustor.feature.categories

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.rustor.feature.categories.ui.CategorySideEffect
import com.rustor.feature.categories.viewModel.CategoriesViewModel

@Composable
fun CategoriesRoute() {
    val viewModel = hiltViewModel<CategoriesViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner, snackbarHostState) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CategorySideEffect.ShowSnackbarError ->
                        snackbarHostState.showSnackbar(
                            "Не удалось отобразить . ${effect.error}",
                        )
                }
            }
        }
    }
}

/*@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onIntent: (CategoriesIntent) -> Unit,
    onCategoryClick: (CategoryId) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
}*/
