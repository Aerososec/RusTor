package com.rustor.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ShowcaseKey : NavKey

@Serializable
data object CategoriesKey : NavKey

@Serializable
data class CategoryShowcaseKey(val categoryId: String) : NavKey

@Serializable
data object SearchKey : NavKey
