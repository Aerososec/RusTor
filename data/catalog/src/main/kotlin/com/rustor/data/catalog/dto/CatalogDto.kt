package com.rustor.data.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDto(val categories: List<CategoryDto>, val apps: List<AppDto>)
