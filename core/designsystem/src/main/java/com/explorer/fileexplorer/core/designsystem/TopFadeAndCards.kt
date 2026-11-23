package com.explorer.fileexplorer.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lightweight vertical gradient scrim for floating top-bars.
 * Avoids offscreen GPU compositing passes.
 */
@Composable
fun FolioTopScrim(
    height: Dp,
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.background,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.92f),
                        baseColor.copy(alpha = 0.65f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

/**
 * Offscreen alpha feather mask for the top viewport edge.
 * Lets full-bleed scrollable lists dissolve smoothly behind a transparent floating top bar
 * without abrupt hard clipping or theme color mismatches.
 */
fun Modifier.topEdgeFeatherFade(
    opaqueStartDp: Dp = 64.dp,
    fadeHeightDp: Dp = 30.dp,
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val heightPx = size.height
        if (heightPx <= 0f) return@drawWithContent

        val startStop = (opaqueStartDp.toPx() / heightPx).coerceIn(0f, 0.5f)
        val endStop = ((opaqueStartDp + fadeHeightDp).toPx() / heightPx).coerceIn(startStop + 0.01f, 0.7f)

        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    startStop to Color.Transparent,
                    endStop to Color.Black,
                    1.0f to Color.Black,
                ),
            ),
            blendMode = BlendMode.DstIn,
        )
    }

/**
 * Pixel / Material 3 Expressive Segmented Card Stack shape helper:
 * 24.dp outer corners, 6.dp inner corners, paired with 2.dp vertical spacing.
 */
fun folioSegmentedCardShape(index: Int, count: Int): Shape {
    if (count <= 1) return RoundedCornerShape(24.dp)
    return when (index) {
        0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
        count - 1 -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        else -> RoundedCornerShape(6.dp)
    }
}

@Composable
fun FolioSegmentedColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content,
    )
}

@Composable
fun FolioSegmentedCard(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable () -> Unit,
) {
    val shape = folioSegmentedCardShape(index, count)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = containerColor,
            modifier = modifier.fillMaxWidth(),
            content = content,
        )
    } else {
        Surface(
            shape = shape,
            color = containerColor,
            modifier = modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
fun FolioSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 6.dp, vertical = 4.dp),
    )
}
