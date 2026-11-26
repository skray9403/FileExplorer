package com.explorer.fileexplorer.core.designsystem

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object FolioSubScreenBarDefaults {
    val BottomBarHeight = 64.dp
    val BottomBarInset = 16.dp
}

@Composable
fun FolioSubScreenHeadline(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Detached 3-bubble bottom header bar for every sub-screen:
 * - Left: 56.dp circular [←] Back bubble
 * - Center: Floating [Title] pill (always centered!)
 * - Right (optional): 56.dp circular [🔍] Search, single [+] FAB, or multi-item FAB Menu
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FolioSubScreenBottomBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String? = null,
    searchHint: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    fabIcon: ImageVector? = Icons.Rounded.Add,
    fabContentDescription: String? = null,
    onFabClick: (() -> Unit)? = null,
    fabMenuItems: List<FolioFabMenuAction> = emptyList(),
    scrollCompact: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme
    val fastSpatial = motionScheme.fastSpatialSpec<Float>()
    val defaultDp = motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.Dp>()

    var searchExpanded by remember { mutableStateOf(false) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    val enabledMenuItems = remember(fabMenuItems) { fabMenuItems.filter { it.enabled } }
    val hasFabMenu = enabledMenuItems.isNotEmpty()
    val showRightFab = hasFabMenu || (onFabClick != null && fabIcon != null)
    val showRightSearch = onSearchQueryChange != null && !showRightFab

    BackHandler(enabled = searchExpanded || fabMenuExpanded) {
        if (fabMenuExpanded) {
            fabMenuExpanded = false
        } else if (searchExpanded) {
            onSearchQueryChange?.invoke("")
            searchExpanded = false
        }
    }

    val pillScale by animateFloatAsState(
        targetValue = if (scrollCompact && !searchExpanded && !fabMenuExpanded) 0.93f else 1f,
        animationSpec = fastSpatial,
        label = "subBarScrollScale",
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (scrollCompact && !searchExpanded && !fabMenuExpanded) 0.88f else 1f,
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "subBarScrollAlpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.End,
    ) {
        if (hasFabMenu && !searchExpanded) {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {},
                modifier = Modifier.padding(bottom = if (fabMenuExpanded) 6.dp else 0.dp),
            ) {
                enabledMenuItems.forEach { action ->
                    FloatingActionButtonMenuItem(
                        onClick = {
                            fabMenuExpanded = false
                            action.onClick()
                        },
                        text = { Text(stringResource(action.labelRes)) },
                        icon = { Icon(action.icon, null) },
                    )
                }
            }
        }

        AnimatedContent(
            targetState = searchExpanded && onSearchQueryChange != null,
            transitionSpec = {
                (fadeIn(tween(130)) + scaleIn(initialScale = 0.95f, animationSpec = fastSpatial))
                    .togetherWith(fadeOut(tween(70)) + scaleOut(targetScale = 0.95f, animationSpec = fastSpatial))
            },
            label = "subScreenSplitBarMode",
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = pillScale
                    scaleY = pillScale
                    alpha = pillAlpha
                },
        ) { isSearching ->
            if (isSearching && onSearchQueryChange != null) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
                Surface(
                    shape = CircleShape,
                    color = colorScheme.surfaceContainerHigh,
                    contentColor = colorScheme.onSurface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onSearchQueryChange("")
                                    searchExpanded = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.close_search),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        BasicTextField(
                            value = searchQuery.orEmpty(),
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface),
                            cursorBrush = SolidColor(colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .padding(horizontal = 8.dp),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isNullOrEmpty()) {
                                        Text(
                                            text = searchHint,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    inner()
                                }
                            },
                        )
                        if (!searchQuery.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable { onSearchQueryChange("") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.clear_search),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    // LEFT: Detached 56.dp Circular [←] Back Bubble
                    val backInteraction = remember { MutableInteractionSource() }
                    val backPressed by backInteraction.collectIsPressedAsState()
                    val backScale by animateFloatAsState(
                        targetValue = if (backPressed) 0.90f else 1f,
                        animationSpec = fastSpatial,
                        label = "subBackScale",
                    )
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(56.dp)
                            .graphicsLayer(scaleX = backScale, scaleY = backScale)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = backInteraction,
                                indication = ripple(bounded = true),
                                onClick = onNavigateBack,
                            ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    // CENTER: Always-Centered Floating Title Pill
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .padding(horizontal = 66.dp)
                            .height(56.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 188.dp),
                            )
                            if (onSearchQueryChange != null && showRightFab) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            fabMenuExpanded = false
                                            searchExpanded = true
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = stringResource(R.string.search),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    // RIGHT: Optional Contextual Action / FAB Menu / Search Bubble
                    if (showRightFab || showRightSearch) {
                        val rightInteraction = remember { MutableInteractionSource() }
                        val rightPressed by rightInteraction.collectIsPressedAsState()
                        val rightScale by animateFloatAsState(
                            targetValue = if (rightPressed) 0.90f else 1f,
                            animationSpec = fastSpatial,
                            label = "subRightScale",
                        )
                        val fabCorner by animateDpAsState(
                            targetValue = if (fabMenuExpanded) 18.dp else 28.dp,
                            animationSpec = defaultDp,
                            label = "subFabCorner",
                        )
                        val fabRot by animateFloatAsState(
                            targetValue = if (fabMenuExpanded) 45f else 0f,
                            animationSpec = fastSpatial,
                            label = "subFabRot",
                        )
                        val rightShape = RoundedCornerShape(fabCorner)
                        Surface(
                            shape = rightShape,
                            color = when {
                                fabMenuExpanded -> colorScheme.primary
                                showRightFab -> colorScheme.primaryContainer
                                else -> colorScheme.surfaceContainerHigh
                            },
                            contentColor = when {
                                fabMenuExpanded -> colorScheme.onPrimary
                                showRightFab -> colorScheme.onPrimaryContainer
                                else -> colorScheme.onSurface
                            },
                            shadowElevation = 6.dp,
                            border = if (showRightSearch) BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)) else null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(56.dp)
                                .graphicsLayer(scaleX = rightScale, scaleY = rightScale)
                                .clip(rightShape)
                                .clickable(
                                    interactionSource = rightInteraction,
                                    indication = ripple(bounded = true),
                                    onClick = {
                                        when {
                                            hasFabMenu -> fabMenuExpanded = !fabMenuExpanded
                                            showRightFab -> onFabClick?.invoke()
                                            else -> searchExpanded = true
                                        }
                                    },
                                ),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when {
                                        hasFabMenu -> Icons.Rounded.Add
                                        showRightFab -> fabIcon ?: Icons.Rounded.Add
                                        else -> Icons.Rounded.Search
                                    },
                                    contentDescription = if (showRightFab) {
                                        fabContentDescription ?: stringResource(R.string.add)
                                    } else {
                                        stringResource(R.string.search)
                                    },
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer { rotationZ = if (hasFabMenu) fabRot else 0f },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
