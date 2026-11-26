package com.explorer.fileexplorer.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

@Composable
fun FolioSubScreenHeadline(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        if (!subtitle.isNullOrBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Detached three-bubble sub-screen dock: Back, page identity, and contextual action. */
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
    val colors = MaterialTheme.colorScheme
    val motion = MaterialTheme.motionScheme
    val border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.35f))
    var searchExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedContent(
            targetState = searchExpanded && onSearchQueryChange != null,
            transitionSpec = {
                (fadeIn(tween(150)) + scaleIn(initialScale = 0.95f, animationSpec = motion.fastSpatialSpec()))
                    .togetherWith(fadeOut(tween(70)) + scaleOut(targetScale = 0.95f, animationSpec = motion.fastSpatialSpec()))
            },
            label = "subScreenBarMode",
            modifier = Modifier.fillMaxWidth(),
        ) { searching ->
            if (searching && onSearchQueryChange != null) {
                val focus = remember { FocusRequester() }
                LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
                Surface(
                    shape = CircleShape,
                    color = colors.surfaceContainerHigh,
                    contentColor = colors.onSurface,
                    shadowElevation = 6.dp,
                    border = border,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { onSearchQueryChange(""); searchExpanded = false },
                            shape = CircleShape,
                            color = colors.surfaceContainerHighest,
                            modifier = Modifier.size(42.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.close_search), modifier = Modifier.size(20.dp))
                            }
                        }
                        BasicTextField(
                            value = searchQuery.orEmpty(),
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                            cursorBrush = SolidColor(colors.primary),
                            modifier = Modifier.weight(1f).focusRequester(focus).padding(horizontal = 12.dp),
                            decorationBox = { inner ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isNullOrEmpty()) Text(searchHint, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    inner()
                                }
                            },
                        )
                        if (!searchQuery.isNullOrEmpty()) {
                            Surface(onClick = { onSearchQueryChange("") }, shape = CircleShape, color = colors.surfaceContainerHighest, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Close, stringResource(R.string.clear_search), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onNavigateBack,
                        shape = CircleShape,
                        color = colors.surfaceContainerHigh,
                        contentColor = colors.onSurface,
                        shadowElevation = 6.dp,
                        border = border,
                        modifier = Modifier.size(56.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), modifier = Modifier.size(21.dp))
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceContainerHigh,
                        contentColor = colors.onSurface,
                        shadowElevation = 6.dp,
                        border = border,
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        Row(Modifier.fillMaxSize().padding(start = 20.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (onSearchQueryChange != null) {
                                Surface(onClick = { searchExpanded = true }, shape = CircleShape, color = colors.surfaceContainerHighest, modifier = Modifier.size(44.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Search, stringResource(R.string.search), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (onFabClick != null && fabIcon != null) {
                        Surface(
                            onClick = onFabClick,
                            shape = CircleShape,
                            color = colors.primaryContainer,
                            contentColor = colors.onPrimaryContainer,
                            shadowElevation = 6.dp,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(fabIcon, fabContentDescription ?: stringResource(R.string.add), modifier = Modifier.size(24.dp))
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
    val BottomBarInset = 16.dp
}
