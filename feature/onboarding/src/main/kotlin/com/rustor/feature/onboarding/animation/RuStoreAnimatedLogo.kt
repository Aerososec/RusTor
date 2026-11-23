package com.rustor.feature.onboarding.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

private const val LOGO_VIEWPORT = 512f
private const val LOGO_ANIMATION_DURATION_MILLIS = 900

private val LOGO_BLUE = Color(0xFF0077FF)
private val LOGO_ENTER_EASING = CubicBezierEasing(0.2f, 0f, 0f, 1f)

private const val BLUE_PATH_DATA =
    "M41.15,470.85" +
        "C76.29,506 132.86,506 246,506H266" +
        "C379.14,506 435.7,506 470.85,470.85" +
        "C506,435.7 506,379.14 506,266V246" +
        "C506,132.86 506,76.29 470.85,41.15" +
        "C435.7,6 379.14,6 266,6H246" +
        "C132.86,6 76.29,6 41.15,41.15" +
        "C6,76.29 6,132.86 6,246V266" +
        "C6,379.14 6,435.71 41.15,470.85Z"

private const val WHITE_PATH_DATA =
    "M392.03,338.34L358.24,329.9" +
        "C354.18,328.73 351.3,325.07 351.16,320.82L346.94,196.48" +
        "C345.72,180.02 333.51,166.93 320.74,163.07" +
        "C320.02,162.86 319.26,163.15 318.83,163.76" +
        "C318.4,164.39 318.57,165.26 319.17,165.73" +
        "C322.32,168.16 331,176.13 331,189.87L330.98,352.44" +
        "C330.98,368.14 316.14,379.65 300.84,375.83L266.54,367.26" +
        "C262.73,365.93 260.09,362.39 259.95,358.3L255.73,233.95" +
        "C254.51,217.49 242.3,204.4 229.53,200.55" +
        "C228.82,200.33 228.05,200.62 227.63,201.24" +
        "C227.19,201.87 227.36,202.73 227.96,203.2" +
        "C231.11,205.64 239.79,213.6 239.79,227.34" +
        "L239.77,362.86L239.79,362.86V389.93" +
        "C239.79,405.63 224.95,417.14 209.65,413.32L112.75,389.11" +
        "C99.28,385.75 89.83,373.7 89.83,359.88L89.83,197.05" +
        "C89.83,181.35 104.67,169.84 119.97,173.66L181.02,188.91V159.56" +
        "C181.02,143.86 195.86,132.35 211.16,136.17L272.21,151.42V122.07" +
        "C272.21,106.37 287.05,94.86 302.35,98.68L399.25,122.88" +
        "C412.72,126.25 422.16,138.3 422.16,152.12V314.95" +
        "C422.16,330.65 407.33,342.16 392.03,338.34Z"

@Composable
internal fun RuStoreAnimatedLogo(accessibilityLabel: String, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    val backgroundPath = remember {
        PathParser().parsePathString(BLUE_PATH_DATA).toPath()
    }
    val symbolPath = remember {
        PathParser().parsePathString(WHITE_PATH_DATA).toPath().apply {
            fillType = PathFillType.EvenOdd
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = LOGO_ANIMATION_DURATION_MILLIS,
                easing = LinearEasing,
            ),
        )
    }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = accessibilityLabel },
    ) {
        val elapsedFraction = progress.value
        val backgroundProgress = LOGO_ENTER_EASING.transform(
            logoPhase(elapsedFraction, start = 0f, end = 0.4f),
        )
        val symbolProgress = LOGO_ENTER_EASING.transform(
            logoPhase(elapsedFraction, start = 0.16f, end = 0.9f),
        )
        val symbolAlpha = logoPhase(elapsedFraction, start = 0.16f, end = 0.55f)

        val backgroundScale = 0.94f + 0.06f * backgroundProgress
        val symbolScale = 0.98f + 0.02f * symbolProgress
        val symbolOffsetY = 32f * (1f - symbolProgress)

        val canvasScale = size.minDimension / LOGO_VIEWPORT
        val viewportCenter = Offset(LOGO_VIEWPORT / 2f, LOGO_VIEWPORT / 2f)
        val left = (size.width - LOGO_VIEWPORT * canvasScale) / 2f
        val top = (size.height - LOGO_VIEWPORT * canvasScale) / 2f

        withTransform({
            translate(left = left, top = top)
            scale(scaleX = canvasScale, scaleY = canvasScale, pivot = Offset.Zero)
            scale(scaleX = backgroundScale, scaleY = backgroundScale, pivot = viewportCenter)
        }) {
            drawPath(
                path = backgroundPath,
                color = LOGO_BLUE,
                alpha = backgroundProgress,
            )

            clipPath(backgroundPath) {
                withTransform({
                    translate(top = symbolOffsetY)
                    scale(scaleX = symbolScale, scaleY = symbolScale, pivot = viewportCenter)
                }) {
                    drawPath(
                        path = symbolPath,
                        color = Color.White,
                        alpha = symbolAlpha,
                    )
                }
            }
        }
    }
}

private fun logoPhase(progress: Float, start: Float, end: Float): Float =
    ((progress - start) / (end - start)).coerceIn(0f, 1f)
