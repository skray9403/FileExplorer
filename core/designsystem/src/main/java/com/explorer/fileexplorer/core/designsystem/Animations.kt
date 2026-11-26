package com.explorer.fileexplorer.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Standard Material 3 cubic bezier curve (200ms) for intra-screen layout transforms.
 */
val M3StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

/**
 * Centralized motion tokens from Monomail foundation for consistent physics across Folio.
 */
object MonoSpring {
    fun <T> micro() = spring<T>(dampingRatio = 0.82f, stiffness = 900f)
    fun <T> spatial() = spring<T>(dampingRatio = 0.76f, stiffness = 420f)
    fun <T> expressive() = spring<T>(dampingRatio = 0.64f, stiffness = 320f)
    fun <T> dismiss() = spring<T>(dampingRatio = 0.88f, stiffness = 650f)

    // Backwards-compatible aliases for existing call sites.
    fun <T> bouncy() = micro()
    fun <T> gentle() = spatial()
    fun <T> snappy() = expressive()
    fun <T> expressiveSpatial() = expressive()

    fun <T> noBounceColor() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 1400f,
    )
}

object MonoTween {
    // Standard content appearance
    val fadeIn = tween<Float>(durationMillis = 250, easing = LinearOutSlowInEasing)

    // Standard content removal
    val fadeOut = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

    // Screen transitions
    fun <T> slide() = tween<T>(durationMillis = 350, easing = FastOutSlowInEasing)

    // Fast scroll morph
    fun <T> fastMorph() = tween<T>(durationMillis = 200, easing = M3StandardEasing)
}

object MonoStagger {
    const val delay = 30
    const val maxDelay = 300
}
