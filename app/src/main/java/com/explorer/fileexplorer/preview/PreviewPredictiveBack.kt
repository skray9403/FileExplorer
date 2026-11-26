package com.explorer.fileexplorer.preview

import androidx.activity.BackEventCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

internal fun Modifier.sideAwarePredictiveBack(progress: Float, swipeEdge: Int): Modifier =
    graphicsLayer {
        val p = progress.coerceIn(0f, 1f)
        val direction = if (swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
        val scale = 1f - 0.055f * p
        scaleX = scale
        scaleY = scale
        translationX = direction * 24.dp.toPx() * p
        shape = RoundedCornerShape(28.dp * p)
        clip = p > 0f
    }
