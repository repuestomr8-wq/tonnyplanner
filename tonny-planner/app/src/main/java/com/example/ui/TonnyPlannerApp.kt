package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.CreateItemBottomSheet
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FocusModeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.TonnySpaceScreen
import com.example.ui.screens.UserScreen
import com.example.ui.theme.TonnyAmber
import com.example.ui.theme.TonnyAmberSoft
import com.example.ui.theme.TonnyBg
import com.example.ui.theme.TonnyMint
import com.example.ui.theme.TonnySurface
import com.example.ui.theme.TonnySurfaceElevated
import com.example.ui.theme.TonnyTextMuted
import com.example.ui.theme.TonnyTextPrimary
import com.example.ui.viewmodel.TonnyPlannerViewModel

object TonnyRoutes {
    const val TODAY = "today"
    const val CALENDAR = "calendar"
    const val TONNY = "tonny"
    const val PROGRESS = "progress"
    const val USER = "user"
}

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(
        route = TonnyRoutes.TODAY,
        label = "Hoy",
        selectedIcon = Icons.Rounded.Inbox,
        unselectedIcon = Icons.Outlined.Inbox
    ),
    BottomTab(
        route = TonnyRoutes.CALENDAR,
        label = "Calendario",
        selectedIcon = Icons.Rounded.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    BottomTab(
        route = TonnyRoutes.TONNY,
        label = "Tonny",
        selectedIcon = Icons.Rounded.Pets,
        unselectedIcon = Icons.Outlined.Pets
    ),
    BottomTab(
        route = TonnyRoutes.PROGRESS,
        label = "Progreso",
        selectedIcon = Icons.Rounded.Insights,
        unselectedIcon = Icons.Outlined.Insights
    ),
    BottomTab(
        route = TonnyRoutes.USER,
        label = "Usuario",
        selectedIcon = Icons.Rounded.Pending,
        unselectedIcon = Icons.Outlined.Pending
    )
)

@Composable
fun TonnyPlannerApp(
    viewModel: TonnyPlannerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: TonnyRoutes.TODAY
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackBanner) {
        val msg = uiState.feedbackBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedbackBanner()
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 700.dp
        val hideChrome = uiState.focusSession != null || uiState.isOnboardingVisible

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = TonnyBg,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = TonnySurfaceElevated,
                        contentColor = TonnyTextPrimary,
                        actionColor = TonnyMint
                    )
                }
            },
            bottomBar = {
                if (!useNavigationRail && !hideChrome) {
                    NavigationBar(
                        containerColor = TonnySurface,
                        windowInsets = WindowInsets.navigationBars,
                        tonalElevation = 0.dp
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                modifier = Modifier.testTag("nav_tab_${tab.route}"),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TonnyAmber,
                                    selectedTextColor = TonnyAmber,
                                    indicatorColor = TonnyAmberSoft,
                                    unselectedIconColor = TonnyTextMuted,
                                    unselectedTextColor = TonnyTextMuted
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail && !hideChrome) {
                    NavigationRail(
                        containerColor = TonnySurface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                modifier = Modifier.testTag("nav_rail_${tab.route}"),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = TonnyAmber,
                                    selectedTextColor = TonnyAmber,
                                    indicatorColor = TonnyAmberSoft,
                                    unselectedIconColor = TonnyTextMuted,
                                    unselectedTextColor = TonnyTextMuted
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = TonnyRoutes.TODAY,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp)
                    ) {
                        composable(TonnyRoutes.TODAY) {
                            TodayScreen(
                                uiState = uiState,
                                onSelectDate = viewModel::selectDate,
                                onNavigateToCalendar = { navigateToTab(TonnyRoutes.CALENDAR) },
                                onNavigateToTonny = { navigateToTab(TonnyRoutes.TONNY) },
                                onToggleItem = viewModel::toggleItemCompletion,
                                onEditItem = viewModel::openEditSheet,
                                onFocusItem = viewModel::openFocusMode,
                                onDeleteItem = viewModel::deleteItem,
                                onOpenCreateSheet = { viewModel.openCreateSheet(uiState.selectedDate, null) }
                            )
                        }
                        composable(TonnyRoutes.CALENDAR) {
                            CalendarScreen(
                                uiState = uiState,
                                onSelectDate = viewModel::selectDate,
                                onShiftWeek = viewModel::shiftCalendarWeek,
                                onJumpToToday = viewModel::jumpToToday,
                                onToggleItem = viewModel::toggleItemCompletion,
                                onEditItem = viewModel::openEditSheet,
                                onFocusItem = viewModel::openFocusMode,
                                onDeleteItem = viewModel::deleteItem,
                                onAddItemAtTime = { startTime -> viewModel.openCreateSheet(uiState.selectedDate, startTime) }
                            )
                        }
                        composable(TonnyRoutes.TONNY) {
                            TonnySpaceScreen(
                                uiState = uiState,
                                onListeningChanged = viewModel::setListening,
                                onVoiceError = viewModel::setVoiceError,
                                onTranscriptChanged = { viewModel.updateRawTranscript(it, triggerParse = true) },
                                onParseNow = viewModel::parseCurrentTranscriptNow,
                                onUpdateDraft = viewModel::updateVoiceDraft,
                                onConfirmDraft = viewModel::confirmVoiceDraft,
                                onClearDraft = viewModel::clearVoiceDraft
                            )
                        }
                        composable(TonnyRoutes.PROGRESS) {
                            ProgressScreen(uiState = uiState)
                        }
                        composable(TonnyRoutes.USER) {
                            UserScreen(
                                uiState = uiState,
                                onSignInOrCreateAccount = viewModel::signInOrCreateAccount,
                                onSignOut = viewModel::signOut,
                                onUpdatePreferences = viewModel::updateUserPreferences,
                                onReopenOnboarding = viewModel::reopenOnboarding
                            )
                        }
                    }
                }
            }
        }

        // Initial Questions Onboarding Flow (from Figma reference adapted to dark theme)
        if (uiState.isOnboardingVisible) {
            OnboardingScreen(
                initialAnswers = uiState.onboarding,
                onComplete = viewModel::completeOnboarding,
                onSkip = viewModel::skipOnboarding
            )
        }

        // Focus Mode Full-Screen Overlay
        val activeFocus = uiState.focusSession
        if (activeFocus != null) {
            FocusModeScreen(
                session = activeFocus,
                onSelectPreset = viewModel::selectFocusPreset,
                onToggleTimer = viewModel::toggleFocusTimer,
                onResetTimer = viewModel::resetFocusTimer,
                onCompleteTask = viewModel::completeActiveFocusTask,
                onCloseFocus = viewModel::closeFocusMode
            )
        }

        // Manual Create / Edit Bottom Sheet with Start & End Time Range
        if (uiState.showCreateSheet) {
            CreateItemBottomSheet(
                editingItem = uiState.editingItem,
                initialDate = uiState.createSheetDate,
                initialStartTime = uiState.createSheetStartTime,
                initialEndTime = uiState.createSheetEndTime,
                use24HourFormat = uiState.userProfile.preferences.use24HourFormat,
                onDismiss = viewModel::dismissCreateSheet,
                onConfirm = viewModel::saveManualItem
            )
        }
    }
}
