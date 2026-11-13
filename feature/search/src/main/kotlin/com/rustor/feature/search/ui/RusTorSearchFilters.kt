package com.rustor.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rustor.core.model.Category
import com.rustor.core.ui.components.chip.RusTorFilterChip
import com.rustor.core.ui.theme.RusTorTheme

@Composable
fun RusTorSearchFilters(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = RusTorTheme.spacing.lg,
        ),
        horizontalArrangement = Arrangement.spacedBy(
            RusTorTheme.spacing.sm,
        ),
    ) {
        item(key = "all") {
            RusTorFilterChip(
                text = stringResource(com.rustor.feature.search.R.string.rustor_search_all),
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
            )
        }

        items(
            items = categories,
            key = { "category:${it.id}" },
        ) { category ->
            RusTorFilterChip(
                text = category.title,
                selected = selectedCategoryId == category.id.value,
                onClick = { onCategorySelected(category.id.value) },
            )
        }
    }
}
