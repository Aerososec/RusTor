package com.rustor.core.ui.components.skeleton

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import com.rustor.core.ui.theme.RusTorTheme
import com.rustor.core.ui.R

private const val SHIMMER_DURATION_MILLIS = 1200
private val SHIMMER_BAND_WIDTH = 240.dp

@Stable
class Shimmer internal constructor(
    internal val progress: State<Float>?,
    internal val baseColor: Color,
    internal val highlightColor: Color,
    internal val windowWidthPx: Float,
    internal val bandWidthPx: Float,
)

internal val LocalShimmer = staticCompositionLocalOf<Shimmer?> { null }

@Composable
fun rememberShimmer(): Shimmer {
    val baseColor = RusTorTheme.colors.surfaceContainerHigh
    val highlightColor = RusTorTheme.colors.surfaceBright
    val windowWidthPx = LocalWindowInfo.current.containerSize.width.toFloat()
    val bandWidthPx = with(LocalDensity.current) { SHIMMER_BAND_WIDTH.toPx() }

    val progress = if (rememberAnimationsEnabled()) {
        rememberInfiniteTransition(label = "shimmer").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(SHIMMER_DURATION_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "shimmerProgress",
        )
    } else {
        null
    }

    return remember(progress, baseColor, highlightColor, windowWidthPx, bandWidthPx) {
        Shimmer(progress, baseColor, highlightColor, windowWidthPx, bandWidthPx)
    }
}

@Composable
fun RusTorSkeleton(
    modifier: Modifier = Modifier,
    loadingDescription: String = stringResource(R.string.rustor_loading),
    content: @Composable BoxScope.() -> Unit,
) {
    val shimmer = rememberShimmer()
    CompositionLocalProvider(LocalShimmer provides shimmer) {
        Box(
            modifier = modifier.clearAndSetSemantics {
                contentDescription = loadingDescription
            },
            content = content,
        )
    }
}

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(RusTorTheme.radius.sm),
    shimmer: Shimmer = LocalShimmer.current ?: rememberShimmer(),
) {
    var windowX by remember { mutableFloatStateOf(0f) }

    Spacer(
        modifier = modifier
            .onPlaced { coordinates -> windowX = coordinates.positionInWindow().x }
            .drawWithCache {
                val outline = shape.createOutline(size, layoutDirection, this)
                onDrawBehind {
                    drawOutline(outline = outline, brush = shimmer.brushAt(windowX))
                }
            },
    )
}


@Composable
fun SkeletonTextLine(
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val lineHeight = with(density) { style.lineHeight.takeOrElse { style.fontSize * 1.4f }.toDp() }
    val glyphHeight = with(density) { style.fontSize.toDp() }

    Box(
        modifier = modifier.height(lineHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(glyphHeight),
            shape = RoundedCornerShape(RusTorTheme.radius.xs),
        )
    }
}

private fun Shimmer.brushAt(blockWindowX: Float): Brush {
    val progress = progress ?: return SolidColor(baseColor)
    val bandStartInWindow = -bandWidthPx + progress.value * (windowWidthPx + bandWidthPx)
    val bandStart = bandStartInWindow - blockWindowX
    return Brush.horizontalGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        startX = bandStart,
        endX = bandStart + bandWidthPx,
        tileMode = TileMode.Clamp,
    )
}

@Composable
private fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
}