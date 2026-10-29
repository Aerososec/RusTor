package com.rustor.core.ui.components.search

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import com.rustor.core.ui.R
import com.rustor.core.ui.components.annotation.ThemePreviews
import com.rustor.core.ui.theme.RusTorTheme

@Composable
fun RusTorSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.rustor_search_placeholder),
    enabled: Boolean = true,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        textStyle = RusTorTheme.typography.bodyLarge,
        placeholder = {
            Text(
                text = placeholder,
                style = RusTorTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.rustor_action_clear),
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch(query)
                keyboardController?.hide()
            },
        ),
        shape = RoundedCornerShape(RusTorTheme.radius.full),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = RusTorTheme.colors.surfaceContainerHigh,
            unfocusedContainerColor = RusTorTheme.colors.surfaceContainerHigh,
            disabledContainerColor = RusTorTheme.colors.surfaceContainerHigh,
            focusedBorderColor = RusTorTheme.colors.primary,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedTextColor = RusTorTheme.colors.onSurface,
            unfocusedTextColor = RusTorTheme.colors.onSurface,
            cursorColor = RusTorTheme.colors.primary,
            focusedLeadingIconColor = RusTorTheme.colors.primary,
            unfocusedLeadingIconColor = RusTorTheme.colors.onSurfaceVariant,
            focusedTrailingIconColor = RusTorTheme.colors.onSurfaceVariant,
            unfocusedTrailingIconColor = RusTorTheme.colors.onSurfaceVariant,
            focusedPlaceholderColor = RusTorTheme.colors.onSurfaceVariant,
            unfocusedPlaceholderColor = RusTorTheme.colors.onSurfaceVariant,
        ),
    )
}

@ThemePreviews
@Composable
private fun RusTorSearchFieldEmptyPreview() {
    var query by remember { mutableStateOf("") }

    RusTorTheme {
        Surface {
            RusTorSearchField(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                modifier = Modifier.padding(RusTorTheme.spacing.lg),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun RusTorSearchFieldFilledPreview() {
    var query by remember { mutableStateOf("Финансы") }

    RusTorTheme {
        Surface {
            RusTorSearchField(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                modifier = Modifier.padding(RusTorTheme.spacing.lg),
            )
        }
    }
}
