package com.rustor.core.ui.components.details

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.rustor.core.ui.R
import com.rustor.core.ui.theme.RusTorTheme

private val PortraitWidth = 160.dp
private val PortraitHeight = 284.dp
private val LandscapeWidth = 280.dp
private val BackgroundBlurRadius = 16.dp
private val LoadingIndicatorSize = 28.dp

@Composable
internal fun RusTorScreenshotThumbnail(
    url: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current
    val density = LocalDensity.current
    val requestWidth = with(density) { LandscapeWidth.roundToPx() }
    val requestHeight = with(density) { PortraitHeight.roundToPx() }

    val request = remember(context, url, requestWidth, requestHeight) {
        ImageRequest.Builder(context)
            .data(url)
            .size(requestWidth, requestHeight)
            .build()
    }

    val painter = rememberAsyncImagePainter(model = request)
    val state by painter.state.collectAsState()
    val image = (state as? AsyncImagePainter.State.Success)?.result?.image

    val frameModifier = if (image != null && image.height > 0 && image.width > image.height) {
        Modifier
            .width(LandscapeWidth)
            .aspectRatio(image.width.toFloat() / image.height)
    } else {
        Modifier.size(width = PortraitWidth, height = PortraitHeight)
    }

    Card(
        onClick = onClick,
        modifier = modifier.then(frameModifier),
        shape = RoundedCornerShape(RusTorTheme.radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = RusTorTheme.colors.surfaceContainerHigh,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when (val imageState = state) {
                is AsyncImagePainter.State.Success -> {
                    val loadedImage = imageState.result.image
                    val isPortrait = loadedImage.height > loadedImage.width

                    if (isPortrait && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Image(
                            painter = imageState.painter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(BackgroundBlurRadius),
                        )
                    }

                    Image(
                        painter = imageState.painter,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is AsyncImagePainter.State.Error -> {
                    Text(
                        text = stringResource(R.string.rustor_screenshot_error),
                        style = RusTorTheme.typography.bodySmall,
                        color = RusTorTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(RusTorTheme.spacing.md),
                    )
                }

                AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading,
                -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(LoadingIndicatorSize),
                        color = RusTorTheme.colors.primary,
                    )
                }
            }
        }
    }
}
