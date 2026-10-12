package com.rustor.core.model

data class App(
    val id: AppId,
    val name: String,
    val iconUrl: String?,
    val category: Category,
    val summary: String,
    val description: String,
    val developer: String,
    val ageRating: AgeRating,
    val screenshots: List<String>,
    val apkUrl: String?,
)
