package com.explorer.fileexplorer.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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

/**
 * Large scrolling headline placed as item 0 inside sub-screens (Settings, Apps, Security, etc.)
 * so top bars stay out of the way while the title + back + search/FAB live at the bottom.
 */
@Composable
fun FolioSubScreenHeadline(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
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
 * Floating bottom bar for sub-pages: [← Back | Title] (+ optional morphing Search + optional + FAB).
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
) {
    val colorScheme = MaterialTheme.colorScheme
    val motionScheme = MaterialTheme.motionScheme
    var searchExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedContent(
            targetState = searchExpanded && onSearchQueryChange != null,
            transitionSpec = {
                (fadeIn(tween(150)) + scaleIn(initialScale = 0.95f, animationSpec = motionScheme.fastSpatialSpec()))
                    .togetherWith(fadeOut(tween(70)) + scaleOut(targetScale = 0.95f, animationSpec = motionScheme.fastSpatialSpec()))
            },
            label = "subScreenBarMode",
            modifier = Modifier.fillMaxWidth(),
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Main pill: [← Back | Title] (+ optional Search trigger)
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.surfaceContainerHigh,
                        contentColor = colorScheme.onSurface,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .align(if (onFabClick != null) Alignment.CenterStart else Alignment.Center)
                            .padding(end = if (onFabClick != null) 64.dp else 0.dp)
                            .height(56.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 6.dp, end = if (onSearchQueryChange != null) 6.dp else 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                onClick = onNavigateBack,
                                shape = CircleShape,
                                color = colorScheme.surfaceContainerHighest,
                                contentColor = colorScheme.onSurface,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = stringResource(R.string.back),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 200.dp),
                            )
                            if (onSearchQueryChange != null) {
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .clickable { searchExpanded = true },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = stringResource(R.string.search),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Contextual FAB (only rendered when onFabClick != null)
                    if (onFabClick != null && fabIcon != null) {
                        val interaction = remember { MutableInteractionSource() }
                        val pressed by interaction.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (pressed) 0.90f else 1f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                            label = "subFabScale",
                        )
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(56.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = interaction,
                                    indication = ripple(bounded = true),
                                    onClick = onFabClick,
                                ),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = fabIcon,
                                    contentDescription = fabContentDescription ?: stringResource(R.string.add),
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

object FolioSubScreenBarDefaults {
    val BottomBarHeight = 64.dp
}
