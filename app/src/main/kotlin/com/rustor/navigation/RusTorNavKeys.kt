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

@Serializable
data class AppDetailsKey(val appId: String) : NavKey

@Serializable
data class ScreenshotsKey(val screenshotUrls: List<String>, val initialIndex: Int) : NavKey
