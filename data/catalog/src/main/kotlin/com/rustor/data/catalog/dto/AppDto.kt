package com.rustor.data.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppDto(
    val packageName: String,
    val name: String,
    val developer: String = "",
    val categoryId: String,
    val ageRating: String? = null,
    val iconUrl: String? = null,
    val summary: String = "",
    val description: String = "",
    val screenshots: List<String> = emptyList(),
    val apkUrl: String? = null,
)
