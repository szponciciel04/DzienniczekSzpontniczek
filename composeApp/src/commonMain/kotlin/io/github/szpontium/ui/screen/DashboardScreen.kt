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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

private enum class DashboardTab(val label: String) {
    GRADES("Oceny"),
    TIMETABLE("Plan"),
    EXAMS("Sprawdziany"),
    HOMEWORK("Zadania"),
    MORE("Więcej")
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

    var selectedTab by remember { mutableIntStateOf(0) }
    val currentTabEnum = DashboardTab.entries.getOrNull(selectedTab) ?: DashboardTab.GRADES

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
                                imageVector = when (tab) {
                                    DashboardTab.GRADES -> Icons.Outlined.Grade
                                    DashboardTab.TIMETABLE -> Icons.Outlined.CalendarMonth
                                    DashboardTab.EXAMS -> Icons.Outlined.Quiz
                                    DashboardTab.HOMEWORK -> Icons.Outlined.CheckBox
                                    DashboardTab.MORE -> Icons.Outlined.MoreHoriz
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTabEnum) {
                DashboardTab.GRADES -> GradesScreen()
                DashboardTab.TIMETABLE -> TimetableScreen()
                DashboardTab.EXAMS -> ExamsScreen()
                DashboardTab.HOMEWORK -> HomeworkScreen()
                DashboardTab.MORE -> MoreScreen(onLogout = onLogout)
            }
        }
    }
}
