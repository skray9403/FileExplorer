package com.explorer.fileexplorer.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

/**
 * Refresh-rate-independent scroll-velocity gate for the Folio dual-pill morphing bottom bar.
 *
 * Uses a 50ms sliding time window converted to dp/s via [density] so 60Hz, 90Hz,
 * and 120Hz displays trigger at the exact same physical swipe speed.
 *
 * - Fast downward scroll (available.y < 0, speed >= [velocityThresholdDpPerSec]) -> [onFastScrollDown]
 * - Fast upward scroll   (available.y > 0, speed >= [velocityThresholdDpPerSec]) -> [onFastScrollUp]
 * - Slow drags below threshold never trigger regardless of distance scrolled.
 */
class ScrollVelocityConnection(
    private val density: Float,
    private val velocityThresholdDpPerSec: Float = 600f,
    private val cooldownMillis: Long = 160L,
    private val onFastScrollDown: () -> Unit,
    private val onFastScrollUp: () -> Unit,
    private val onVelocitySample: (Float) -> Unit = {},
) : NestedScrollConnection {

    private data class DeltaSample(val timeNanos: Long, val dyPx: Float)

    private val windowNanos = 50_000_000L // 50ms rolling window
    private val minWindowNanos = 16_000_000L // require >= 16ms of samples
    private val samples = ArrayDeque<DeltaSample>(16)
    private var lastTriggerMillis = 0L

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        if (available.y == 0f) return Offset.Zero

        val nowNanos = System.nanoTime()
        samples.addLast(DeltaSample(nowNanos, available.y))

        val cutoff = nowNanos - windowNanos
        while (samples.isNotEmpty() && samples.first().timeNanos < cutoff) {
            samples.removeFirst()
        }

        if (samples.size < 2) return Offset.Zero

        val elapsedNanos = nowNanos - samples.first().timeNanos
        if (elapsedNanos < minWindowNanos) return Offset.Zero

        var sumDyPx = 0f
        for (sample in samples) {
            sumDyPx += sample.dyPx
        }

        val elapsedSec = elapsedNanos / 1_000_000_000f
        val velocityDpPerSec = (sumDyPx / density) / elapsedSec
        onVelocitySample(velocityDpPerSec)

        val nowMillis = nowNanos / 1_000_000L
        if (nowMillis - lastTriggerMillis >= cooldownMillis && abs(velocityDpPerSec) >= velocityThresholdDpPerSec) {
            lastTriggerMillis = nowMillis
            if (velocityDpPerSec < 0f) {
                // Finger moving up -> list scrolling down
                onFastScrollDown()
            } else {
                // Finger moving down -> list scrolling up
                onFastScrollUp()
            }
        }

        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        samples.clear()
        onVelocitySample(0f)
        return Velocity.Zero
    }
}

@Composable
fun rememberScrollVelocityConnection(
    velocityThresholdDpPerSec: Float = 600f,
    onFastScrollDown: () -> Unit,
    onFastScrollUp: () -> Unit,
    onVelocitySample: (Float) -> Unit = {},
): ScrollVelocityConnection {
    val density = LocalDensity.current.density
    return remember(density, velocityThresholdDpPerSec) {
        ScrollVelocityConnection(
            density = density,
            velocityThresholdDpPerSec = velocityThresholdDpPerSec,
            onFastScrollDown = onFastScrollDown,
            onFastScrollUp = onFastScrollUp,
            onVelocitySample = onVelocitySample,
        )
    }
}
