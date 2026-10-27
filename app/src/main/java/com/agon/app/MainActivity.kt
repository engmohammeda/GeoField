package com.agon.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agon.app.data.AppLanguage
import com.agon.app.data.ThemeMode
import com.agon.app.ui.EnglishStrings
import com.agon.app.ui.Strings
import com.agon.app.ui.components.PointEditorSheet
import com.agon.app.ui.screens.HomeScreen
import com.agon.app.ui.screens.PointDetailScreen
import com.agon.app.ui.screens.PointsScreen
import com.agon.app.ui.screens.SettingsScreen
import com.agon.app.ui.screens.TraverseScreen
import com.agon.app.ui.theme.AgonAppTheme
import com.agon.app.viewmodel.SurveyUiState
import com.agon.app.viewmodel.SurveyViewModel
import com.agon.app.viewmodel.UiEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SurveyViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            val strings = if (state.settings.language == AppLanguage.ARABIC) {
                Strings()
            } else {
                EnglishStrings
            }

            val darkTheme = when (state.settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AgonAppTheme(darkTheme = darkTheme) {
                val layoutDirection = if (state.settings.language == AppLanguage.ARABIC) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    GeoFieldApp(viewModel = viewModel, state = state, strings = strings)
                }
            }
        }
    }
}

private data class NavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoFieldApp(
    viewModel: SurveyViewModel,
    state: SurveyUiState,
    strings: Strings,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Route.Home
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val currentStrings by rememberUpdatedState(strings)

    var editorOpen by remember { mutableStateOf(false) }
    var editorTargetId by remember { mutableStateOf<String?>(null) }
    var exportMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.Message -> {
                    val text = when (event.text) {
                        SurveyViewModel.MESSAGE_SAVED -> currentStrings.pointSaved
                        SurveyViewModel.MESSAGE_UPDATED -> currentStrings.pointUpdated
                        SurveyViewModel.MESSAGE_DELETED -> currentStrings.pointDeleted
                        SurveyViewModel.MESSAGE_RESTORED -> currentStrings.pointSaved
                        SurveyViewModel.MESSAGE_CLEARED -> currentStrings.dataCleared
                        SurveyViewModel.MESSAGE_SAMPLE -> currentStrings.sampleLoaded
                        else -> event.text
                    }
                    val result = snackbarHostState.showSnackbar(
                        message = text,
                        actionLabel = if (event.text == SurveyViewModel.MESSAGE_DELETED) {
                            currentStrings.undo
                        } else {
                            null
                        },
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreLastDeleted()
                    }
                }

                is UiEvent.Share -> shareText(context, event.subject, event.body)
            }
        }
    }

    val destinations = listOf(
        NavDestination(Route.Home, strings.navHome, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        NavDestination(Route.Points, strings.navPoints, Icons.Outlined.Place, Icons.Filled.Place),
        NavDestination(Route.Traverse, strings.navTraverse, Icons.Outlined.Route, Icons.Filled.Route),
        NavDestination(Route.Settings, strings.navSettings, Icons.Outlined.Settings, Icons.Filled.Settings),
    )

    val isDetail = route == Route.PointDetail
    val titleText = when (route) {
        Route.Home -> strings.appName
        Route.Points -> strings.navPoints
        Route.Traverse -> strings.navTraverse
        Route.Settings -> strings.navSettings
        Route.PointDetail -> state.points
            .firstOrNull { it.id == backStackEntry?.arguments?.getString("id") }
            ?.code
            ?: strings.pointDetails
        else -> strings.appName
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (route == Route.Home) {
                            Text(
                                text = state.settings.projectName.ifBlank { strings.projectLabel },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isDetail) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = strings.close,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.setLanguage(
                                if (state.settings.language == AppLanguage.ARABIC) {
                                    AppLanguage.ENGLISH
                                } else {
                                    AppLanguage.ARABIC
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Translate,
                            contentDescription = strings.language,
                        )
                    }
                    IconButton(onClick = { exportMenuOpen = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = strings.actionExport,
                        )
                    }
                    DropdownMenu(
                        expanded = exportMenuOpen,
                        onDismissRequest = { exportMenuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.exportCsv) },
                            onClick = {
                                exportMenuOpen = false
                                viewModel.shareCsv(state.points, state.settings.projectName)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(strings.exportGeoJson) },
                            onClick = {
                                exportMenuOpen = false
                                viewModel.shareGeoJson(state.points, state.settings.projectName)
                            },
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            if (!isDetail) {
                NavigationBar {
                    destinations.forEach { destination ->
                        val selected = route == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Route.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label, maxLines = 1) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Route.Home) {
                HomeScreen(
                    state = state,
                    strings = strings,
                    onAddPoint = {
                        editorTargetId = null
                        editorOpen = true
                    },
                    onOpenPoints = {
                        navController.navigate(Route.Points) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenTraverse = {
                        navController.navigate(Route.Traverse) {
                            popUpTo(Route.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onExport = { exportMenuOpen = true },
                    onOpenPoint = { id -> navController.navigate("point/$id") },
                )
            }

            composable(Route.Points) {
                PointsScreen(
                    state = state,
                    strings = strings,
                    onQueryChange = viewModel::setQuery,
                    onFilterChange = viewModel::setCategoryFilter,
                    onSortChange = viewModel::setSort,
                    onDelete = viewModel::deletePoint,
                    onAddPoint = {
                        editorTargetId = null
                        editorOpen = true
                    },
                    onOpenPoint = { id -> navController.navigate("point/$id") },
                )
            }

            composable(Route.Traverse) {
                TraverseScreen(
                    state = state,
                    strings = strings,
                    onToggleClosed = viewModel::setTraverseClosed,
                    onReverse = viewModel::reverseTraverse,
                    onExportCsv = {
                        viewModel.shareCsv(state.traverseOrder, state.settings.projectName)
                    },
                )
            }

            composable(Route.Settings) {
                SettingsScreen(
                    state = state,
                    strings = strings,
                    onThemeChange = viewModel::setThemeMode,
                    onUnitsChange = viewModel::setUnits,
                    onLanguageChange = viewModel::setLanguage,
                    onDecimalsChange = viewModel::setDecimals,
                    onProjectNameChange = viewModel::setProjectName,
                    onExportCsv = { viewModel.shareCsv(state.points, state.settings.projectName) },
                    onExportGeoJson = { viewModel.shareGeoJson(state.points, state.settings.projectName) },
                    onLoadSample = viewModel::loadSample,
                    onClearAll = viewModel::clearAll,
                )
            }

            composable(
                route = Route.PointDetail,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                PointDetailScreen(
                    point = state.points.firstOrNull { it.id == id },
                    allPoints = state.points,
                    strings = strings,
                    onEdit = { target ->
                        editorTargetId = target.id
                        editorOpen = true
                    },
                    onDelete = { targetId ->
                        viewModel.deletePoint(targetId)
                        navController.popBackStack()
                    },
                )
            }
        }
    }

    if (editorOpen) {
        PointEditorSheet(
            initial = state.points.firstOrNull { it.id == editorTargetId },
            strings = strings,
            onDismiss = {
                editorOpen = false
                editorTargetId = null
            },
            onSave = { draft ->
                val existing = state.points.firstOrNull { it.id == editorTargetId }
                if (existing != null) {
                    viewModel.updatePoint(
                        existing.copy(
                            code = draft.code,
                            easting = draft.easting,
                            northing = draft.northing,
                            elevation = draft.elevation,
                            category = draft.category,
                            description = draft.description,
                            accuracy = draft.accuracy,
                        )
                    )
                } else {
                    viewModel.addPoint(
                        code = draft.code,
                        easting = draft.easting,
                        northing = draft.northing,
                        elevation = draft.elevation,
                        category = draft.category,
                        description = draft.description,
                        accuracy = draft.accuracy,
                    )
                }
                editorOpen = false
                editorTargetId = null
            },
        )
    }
}

private object Route {
    const val Home = "home"
    const val Points = "points"
    const val Traverse = "traverse"
    const val Settings = "settings"
    const val PointDetail = "point/{id}"
}

private fun shareText(context: Context, subject: String, body: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(intent, subject))
    }
}
