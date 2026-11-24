package com.explorer.fileexplorer.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.explorer.fileexplorer.core.designsystem.FolioDockController
import com.explorer.fileexplorer.core.designsystem.FolioNavigationDock
import com.explorer.fileexplorer.core.designsystem.LocalFolioDockController
import com.explorer.fileexplorer.feature.apps.AppsScreen
import com.explorer.fileexplorer.feature.browser.BrowserScreen
import com.explorer.fileexplorer.feature.browser.CollectionsScreen
import com.explorer.fileexplorer.feature.browser.RootModulesScreen
import com.explorer.fileexplorer.feature.browser.ShizukuScreen
import com.explorer.fileexplorer.feature.browser.StorageAnalyzerScreen
import com.explorer.fileexplorer.feature.browser.TrashScreen
import com.explorer.fileexplorer.feature.cloud.CloudScreen
import com.explorer.fileexplorer.feature.editor.DocumentPreviewScreen
import com.explorer.fileexplorer.feature.editor.EditorScreen
import com.explorer.fileexplorer.feature.editor.HexEditorScreen
import com.explorer.fileexplorer.feature.network.NetworkScreen
import com.explorer.fileexplorer.feature.network.ShareServerScreen
import com.explorer.fileexplorer.feature.search.SearchScreen
import com.explorer.fileexplorer.feature.search.TagsScreen
import com.explorer.fileexplorer.feature.security.SecurityScreen
import com.explorer.fileexplorer.feature.settings.SettingsScreen
import com.explorer.fileexplorer.feature.tools.ToolsHubScreen
import com.explorer.fileexplorer.feature.transfer.TransferQueueScreen

object Routes {
    const val BROWSER = "browser"
    const val BROWSER_AT_PATH = "browser/{path}"
    const val SEARCH = "search"
    const val SAVED_SEARCH = "search/saved/{query}/{scopePath}/{useRegex}"
    const val TAGS = "tags"
    const val COLLECTIONS = "collections"
    const val TOOLS = "tools"
    const val SETTINGS = "settings"
    const val NETWORK = "network"
    const val SHIZUKU = "shizuku"
    const val SHARE_SERVER = "share-server"
    const val CLOUD = "cloud"
    const val SECURITY = "security"
    const val EDITOR = "editor/{filePath}"
    const val HEX_EDITOR = "hex/{filePath}"
    const val PREVIEW = "preview/{filePath}"
    const val APPS = "apps"
    const val TRASH = "trash"
    const val ANALYZER = "analyzer"
    const val TRANSFERS = "transfers"
    const val ROOT_MODULES = "root-modules"

    fun editorRoute(filePath: String) = "editor/${java.net.URLEncoder.encode(filePath, "UTF-8")}"
    fun hexEditorRoute(filePath: String) = "hex/${java.net.URLEncoder.encode(filePath, "UTF-8")}"
    fun previewRoute(filePath: String) = "preview/${java.net.URLEncoder.encode(filePath, "UTF-8")}"
    fun browserAtPath(path: String) = "browser/${java.net.URLEncoder.encode(path, "UTF-8")}"
    fun savedSearchRoute(query: String, scopePath: String, useRegex: Boolean) =
        "search/saved/${java.net.URLEncoder.encode(query, "UTF-8")}/${java.net.URLEncoder.encode(scopePath, "UTF-8")}/$useRegex"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    initialPath: String? = null,
    dockViewModel: DockStateViewModel = hiltViewModel(),
) {
    val dockController = remember { FolioDockController() }
    val dockSnapshot by dockViewModel.snapshot.collectAsStateWithLifecycle()
    var showTaskSheet by remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val selectedDestination = when (currentRoute) {
        Routes.BROWSER, Routes.BROWSER_AT_PATH -> "browser"
        Routes.COLLECTIONS -> "collections"
        Routes.TOOLS -> "tools"
        else -> null
    }

    LaunchedEffect(dockSnapshot) {
        dockController.taskBadgeCount = dockSnapshot.totalBadgeCount
        dockController.taskProgress = dockSnapshot.aggregateProgress
        dockController.taskHasConflict = dockSnapshot.hasConflict
        dockController.onTaskPillClick = { showTaskSheet = true }
    }

    LaunchedEffect(currentRoute) {
        dockController.resetScrollMorph()
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.BROWSER) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalFolioDockController provides dockController) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.BROWSER,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(tween(180)) + slideInHorizontally(
                        animationSpec = tween(240),
                        initialOffsetX = { width -> width / 14 },
                    )
                },
                exitTransition = {
                    fadeOut(tween(120)) + slideOutHorizontally(
                        animationSpec = tween(180),
                        targetOffsetX = { width -> -width / 24 },
                    )
                },
                popEnterTransition = {
                    fadeIn(tween(180)) + slideInHorizontally(
                        animationSpec = tween(240),
                        initialOffsetX = { width -> -width / 24 },
                    )
                },
                popExitTransition = {
                    fadeOut(tween(120)) + slideOutHorizontally(
                        animationSpec = tween(180),
                        targetOffsetX = { width -> width / 14 },
                    )
                },
            ) {
                composable(Routes.BROWSER) {
                    BrowserScreen(
                        initialPath = initialPath,
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenSavedSearch = { search ->
                            navController.navigate(Routes.savedSearchRoute(search.query, search.scopePath, search.useRegex))
                        },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenNetwork = { navController.navigate(Routes.NETWORK) },
                        onOpenShizuku = { navController.navigate(Routes.SHIZUKU) },
                        onOpenServer = { navController.navigate(Routes.SHARE_SERVER) },
                        onOpenCloud = { navController.navigate(Routes.CLOUD) },
                        onOpenCollections = { navController.navigate(Routes.COLLECTIONS) },
                        onOpenTags = { navController.navigate(Routes.TAGS) },
                        onOpenSecurity = { navController.navigate(Routes.SECURITY) },
                        onOpenApps = { navController.navigate(Routes.APPS) },
                        onOpenRootModules = { navController.navigate(Routes.ROOT_MODULES) },
                        onOpenTrash = { navController.navigate(Routes.TRASH) },
                        onOpenAnalyzer = { navController.navigate(Routes.ANALYZER) },
                        onOpenTransfers = { navController.navigate(Routes.TRANSFERS) },
                        onOpenEditor = { path -> navController.navigate(Routes.editorRoute(path)) },
                        onOpenHexEditor = { path -> navController.navigate(Routes.hexEditorRoute(path)) },
                        onOpenPreview = { path -> navController.navigate(Routes.previewRoute(path)) },
                    )
                }

                composable(
                    route = Routes.BROWSER_AT_PATH,
                    arguments = listOf(navArgument("path") { type = NavType.StringType }),
                ) { backStackEntry ->
                    BrowserScreen(
                        initialPath = java.net.URLDecoder.decode(
                            backStackEntry.arguments?.getString("path") ?: "",
                            "UTF-8",
                        ),
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenSavedSearch = { search ->
                            navController.navigate(Routes.savedSearchRoute(search.query, search.scopePath, search.useRegex))
                        },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenNetwork = { navController.navigate(Routes.NETWORK) },
                        onOpenShizuku = { navController.navigate(Routes.SHIZUKU) },
                        onOpenServer = { navController.navigate(Routes.SHARE_SERVER) },
                        onOpenCloud = { navController.navigate(Routes.CLOUD) },
                        onOpenCollections = { navController.navigate(Routes.COLLECTIONS) },
                        onOpenTags = { navController.navigate(Routes.TAGS) },
                        onOpenSecurity = { navController.navigate(Routes.SECURITY) },
                        onOpenApps = { navController.navigate(Routes.APPS) },
                        onOpenRootModules = { navController.navigate(Routes.ROOT_MODULES) },
                        onOpenTrash = { navController.navigate(Routes.TRASH) },
                        onOpenAnalyzer = { navController.navigate(Routes.ANALYZER) },
                        onOpenTransfers = { navController.navigate(Routes.TRANSFERS) },
                        onOpenEditor = { path -> navController.navigate(Routes.editorRoute(path)) },
                        onOpenHexEditor = { path -> navController.navigate(Routes.hexEditorRoute(path)) },
                        onOpenPreview = { path -> navController.navigate(Routes.previewRoute(path)) },
                    )
                }

                composable(Routes.TOOLS) {
                    ToolsHubScreen(
                        onOpenTransfers = { navController.navigate(Routes.TRANSFERS) },
                        onOpenAnalyzer = { navController.navigate(Routes.ANALYZER) },
                        onOpenTrash = { navController.navigate(Routes.TRASH) },
                        onOpenApps = { navController.navigate(Routes.APPS) },
                        onOpenCloud = { navController.navigate(Routes.CLOUD) },
                        onOpenServer = { navController.navigate(Routes.SHARE_SERVER) },
                        onOpenTags = { navController.navigate(Routes.TAGS) },
                        onOpenSecurity = { navController.navigate(Routes.SECURITY) },
                        onOpenShizuku = { navController.navigate(Routes.SHIZUKU) },
                        onOpenRootModules = { navController.navigate(Routes.ROOT_MODULES) },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenNetwork = { navController.navigate(Routes.NETWORK) },
                    )
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToFolder = { path ->
                            navController.popBackStack()
                            navController.navigate(Routes.browserAtPath(path)) {
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable(
                    route = Routes.SAVED_SEARCH,
                    arguments = listOf(
                        navArgument("query") { type = NavType.StringType },
                        navArgument("scopePath") { type = NavType.StringType },
                        navArgument("useRegex") { type = NavType.BoolType },
                    ),
                ) { backStackEntry ->
                    SearchScreen(
                        currentPath = java.net.URLDecoder.decode(
                            backStackEntry.arguments?.getString("scopePath") ?: "",
                            "UTF-8",
                        ),
                        initialQuery = java.net.URLDecoder.decode(
                            backStackEntry.arguments?.getString("query") ?: "",
                            "UTF-8",
                        ),
                        initialUseRegex = backStackEntry.arguments?.getBoolean("useRegex") ?: false,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToFolder = { path ->
                            navController.popBackStack()
                            navController.navigate(Routes.browserAtPath(path)) {
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable(Routes.TAGS) { TagsScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.COLLECTIONS) {
                    CollectionsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    )
                }
                composable(Routes.SETTINGS) { SettingsScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.NETWORK) { NetworkScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.SHIZUKU) {
                    ShizukuScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onOpenAndroidData = { path -> navController.navigate(Routes.browserAtPath(path)) },
                    )
                }
                composable(Routes.SHARE_SERVER) { ShareServerScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.CLOUD) { CloudScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.SECURITY) { SecurityScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.APPS) { AppsScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.ROOT_MODULES) { RootModulesScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.TRASH) { TrashScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.ANALYZER) { StorageAnalyzerScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Routes.TRANSFERS) { TransferQueueScreen(onNavigateBack = { navController.popBackStack() }) }

                composable(
                    route = Routes.EDITOR,
                    arguments = listOf(navArgument("filePath") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val filePath = java.net.URLDecoder.decode(
                        backStackEntry.arguments?.getString("filePath") ?: "",
                        "UTF-8",
                    )
                    EditorScreen(filePath = filePath, onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = Routes.HEX_EDITOR,
                    arguments = listOf(navArgument("filePath") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val filePath = java.net.URLDecoder.decode(
                        backStackEntry.arguments?.getString("filePath") ?: "",
                        "UTF-8",
                    )
                    HexEditorScreen(filePath = filePath, onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = Routes.PREVIEW,
                    arguments = listOf(navArgument("filePath") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val filePath = java.net.URLDecoder.decode(
                        backStackEntry.arguments?.getString("filePath") ?: "",
                        "UTF-8",
                    )
                    DocumentPreviewScreen(filePath = filePath, onNavigateBack = { navController.popBackStack() })
                }
            }

            AnimatedVisibility(
                visible = selectedDestination != null,
                enter = slideInVertically(tween(220)) { it / 2 } + fadeIn(tween(180)),
                exit = slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(140)),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                FolioNavigationDock(
                    selectedDestination = selectedDestination ?: "browser",
                    controller = dockController,
                    onBrowse = { navigateTopLevel(Routes.BROWSER) },
                    onCollections = { navigateTopLevel(Routes.COLLECTIONS) },
                    onTools = { navigateTopLevel(Routes.TOOLS) },
                    onSearch = { navController.navigate(Routes.SEARCH) },
                )
            }

            if (showTaskSheet) {
                StackedTaskProgressBottomSheet(
                    snapshot = dockSnapshot,
                    viewModel = dockViewModel,
                    onExecuteClipboardTask = dockController.onExecuteClipboardTask,
                    onDismiss = { showTaskSheet = false },
                )
            }
        }
    }
}
