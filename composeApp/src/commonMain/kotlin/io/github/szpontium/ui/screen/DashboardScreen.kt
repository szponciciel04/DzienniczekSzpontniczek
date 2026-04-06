package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Looks6
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Looks6
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.szpontium.navigation.Route

import io.github.szpontium.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

private enum class DashboardTab(
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val route: Route
) {
    START("Start", Icons.Outlined.Home, Icons.Filled.Home, Route.Start),
    GRADES("Oceny", Icons.Outlined.Looks6, Icons.Filled.Looks6, Route.Grades),
    TIMETABLE("Plan", Icons.Outlined.Backpack, Icons.Filled.Backpack, Route.Timetable),
    EXAMS("Sprawdziany", Icons.Outlined.CalendarToday, Icons.Filled.CalendarToday, Route.Exams),
    MORE("Więcej", Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz, Route.More)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedIndex.collectAsStateWithLifecycle()
    val luckyNumber by viewModel.luckyNumber.collectAsStateWithLifecycle()
    val currentAccount = accounts.getOrNull(selectedIndex)

    val backStack = remember { mutableStateListOf<Route>(Route.Start) }
    val currentRoute = backStack.lastOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentAccount?.let {
                                "${it.pupil.firstName} ${it.pupil.surname}"
                            } ?: "Brak konta",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = currentAccount?.unit?.name ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    luckyNumber?.let {
                        if (it.number != 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = "Szczęśliwy numerek",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "${it.number}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                DashboardTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == tab.route) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        selected = currentRoute == tab.route || (tab.route == Route.More && currentRoute in listOf(Route.Notes, Route.Announcements, Route.Account, Route.Homework)),
                        onClick = {
                            if (currentRoute != tab.route) {
                                backStack.clear()
                                backStack.add(tab.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            entryProvider = entryProvider {
                entry<Route.Start> { StartScreen(luckyNumber = luckyNumber, onNavigate = { backStack.add(it) }) }
                entry<Route.Grades> { GradesScreen() }
                entry<Route.Timetable> { TimetableScreen() }
                entry<Route.Exams> { ExamsScreen() }
                entry<Route.Homework> { HomeworkScreen() }
                entry<Route.More> { MoreScreen(onNavigate = { backStack.add(it) }) }
                entry<Route.Notes> { NotesScreen() }
                entry<Route.Announcements> { AnnouncementsScreen() }
                entry<Route.Account> { AccountScreen(onLogout = onLogout) }
            }
        )
    }
}
