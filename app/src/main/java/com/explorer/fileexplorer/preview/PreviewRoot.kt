package com.explorer.fileexplorer.preview

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.explorer.fileexplorer.core.designsystem.FolioDockController
import com.explorer.fileexplorer.core.designsystem.FolioMorphingBottomBar
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import kotlinx.coroutines.CancellationException

@Composable
fun FolioPreviewRoot(
    appearance: PreviewAppearanceState,
    onAppearanceChange: (PreviewAppearanceState) -> Unit,
) {
    val dockController = remember { FolioDockController() }
    val backStack = remember { mutableStateListOf(PreviewRoute.BROWSER) }
    val currentRoute = backStack.lastOrNull() ?: PreviewRoute.BROWSER
    val backPreviewAnim = remember { Animatable(0f) }
    var backSwipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    val navigateTo: (PreviewRoute) -> Unit = { route ->
        if (route == PreviewRoute.BROWSER || route == PreviewRoute.COLLECTIONS) {
            backStack.clear()
            backStack.add(route)
        } else if (backStack.lastOrNull() != route) {
            backStack.add(route)
        }
    }

    val navigateBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    PredictiveBackHandler(enabled = backStack.size > 1) { progress ->
        try {
            progress.collect { event ->
                backSwipeEdge = event.swipeEdge
                backPreviewAnim.snapTo(event.progress)
                dockController.predictiveBackProgress = event.progress
                dockController.predictiveBackEdgeSign = if (event.swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f
            }
            navigateBack()
            dockController.predictiveBackProgress = 0f
            backPreviewAnim.animateTo(0f)
        } catch (_: CancellationException) {
            dockController.predictiveBackProgress = 0f
            backPreviewAnim.animateTo(0f)
        }
    }

    val showDock = currentRoute == PreviewRoute.BROWSER || currentRoute == PreviewRoute.COLLECTIONS
    val selectedDestination = if (currentRoute == PreviewRoute.COLLECTIONS) "collections" else "browser"

    CompositionLocalProvider(LocalFolioDockController provides dockController) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    (fadeIn(tween(150)) + scaleIn(initialScale = 0.98f, animationSpec = tween(160)))
                        .togetherWith(fadeOut(tween(90)))
                },
                label = "previewRoute",
                modifier = Modifier
                    .fillMaxSize()
                    .sideAwarePredictiveBack(backPreviewAnim.value, backSwipeEdge),
            ) { route ->
                when (route) {
                    PreviewRoute.BROWSER -> PreviewBrowserScreen(
                        onOpenSettings = { navigateTo(PreviewRoute.SETTINGS) },
                    )
                    PreviewRoute.COLLECTIONS -> PreviewCollectionsScreen(
                        onOpenSettings = { navigateTo(PreviewRoute.SETTINGS) },
                    )
                    PreviewRoute.SETTINGS -> PreviewToolsAndSettingsScreen(
                        appearance = appearance,
                        onAppearanceChange = onAppearanceChange,
                        onNavigateBack = navigateBack,
                        onNavigate = navigateTo,
                    )
                    PreviewRoute.SECURITY -> PreviewSecurityScreen(
                        onNavigateBack = navigateBack,
                    )
                    PreviewRoute.APPS -> PreviewAppsScreen(
                        onNavigateBack = navigateBack,
                    )
                    PreviewRoute.TRASH -> PreviewTrashScreen(
                        onNavigateBack = navigateBack,
                    )
                    PreviewRoute.NETWORK -> PreviewNetworkScreen(
                        onNavigateBack = navigateBack,
                    )
                    PreviewRoute.CLOUD -> PreviewCloudScreen(
                        onNavigateBack = navigateBack,
                    )
                    PreviewRoute.STORAGE_ANALYZER -> PreviewStorageAnalyzerScreen(
                        onNavigateBack = navigateBack,
                    )
                }
            }

            if (showDock) {
                FolioMorphingBottomBar(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { dest ->
                        when (dest) {
                            "browser" -> navigateTo(PreviewRoute.BROWSER)
                            "collections" -> navigateTo(PreviewRoute.COLLECTIONS)
                        }
                    },
                    controller = dockController,
                    onSearchClick = { dockController.onInlineSearchOpen?.invoke() },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
