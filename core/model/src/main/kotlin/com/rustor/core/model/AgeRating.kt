package com.rustor.core.model

enum class AgeRating(val value: Int) {
    ZERO_PLUS(0),
    SIX_PLUS(6),
    EIGHT_PLUS(8),
    TWELVE_PLUS(12),
    SIXTEEN_PLUS(16),
    EIGHTEEN_PLUS(18),
    ;

    companion object {
        fun fromMinAge(minAge: Int): AgeRating = entries.firstOrNull { it.value == minAge } ?: EIGHTEEN_PLUS
    }
}
