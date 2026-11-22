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
    // For press feedback, FAB clicks, and bouncy indicators
    fun <T> bouncy() = spring<T>(
        dampingRatio = 0.65f,
        stiffness = 800f,
    )

    // For layout shifts, expanding areas, and bottom bar morphs
    fun <T> gentle() = spring<T>(
        dampingRatio = 0.8f,
        stiffness = 400f,
    )

    // For quick toggles, chips, selection states
    fun <T> snappy() = spring<T>(
        dampingRatio = 0.75f,
        stiffness = 1200f,
    )

    // Spatial spring for cross-screen container transforms (M3 Expressive)
    fun <T> expressiveSpatial() = spring<T>(
        dampingRatio = 0.85f,
        stiffness = 500f,
    )

    // Strict no-bounce spring for color and alpha transitions
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
