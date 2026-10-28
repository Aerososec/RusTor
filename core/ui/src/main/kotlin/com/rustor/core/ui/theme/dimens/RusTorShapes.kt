package com.rustor.core.ui.theme.dimens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

private val radius = RusTorRadius()

internal val RusTorShapes = Shapes(
    extraSmall = RoundedCornerShape(radius.xs),
    small = RoundedCornerShape(radius.sm),
    medium = RoundedCornerShape(radius.lg),
    large = RoundedCornerShape(radius.xl),
    extraLarge = RoundedCornerShape(radius.xl),
)
