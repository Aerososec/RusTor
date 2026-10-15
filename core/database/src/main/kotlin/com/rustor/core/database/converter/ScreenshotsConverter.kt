package com.rustor.core.database.converter

import androidx.room.TypeConverter

internal class ScreenshotsConverter {
    @TypeConverter
    fun fromList(urls: List<String>): String = urls.joinToString(SEPARATOR)

    @TypeConverter
    fun toList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    private companion object {
        const val SEPARATOR = "\n"
    }
}
