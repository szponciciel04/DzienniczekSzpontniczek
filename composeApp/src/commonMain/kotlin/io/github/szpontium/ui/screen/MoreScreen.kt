package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.navigation.Route
import io.github.szpontium.session.StudentSession
import io.github.szpontium.theme.expressiveGroupShape
import io.github.szpontium.update.getAppVersion
import io.github.szpontium.viewmodel.AccountViewModel
import io.github.szpontium.viewmodel.UpdateViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MoreScreen(onNavigate: (Route) -> Unit) {
    val items = listOf(
        Triple(Icons.Outlined.EmojiEvents, "Uwagi i osiągnięcia", Route.Notes),
        Triple(Icons.AutoMirrored.Outlined.Announcement, "Ogłoszenia", Route.Announcements),
        Triple(Icons.AutoMirrored.Outlined.Message, "Wiadomości", Route.Messages),
        Triple(Icons.Outlined.Book, "Zadania domowe", Route.Homework),
        Triple(Icons.Outlined.Person, "Konto i uczniowie", Route.Account)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items.forEachIndexed { index, (icon, title, route) ->
            MoreItem(
                icon = icon,
                title = title,
                shape = expressiveGroupShape(index = index, count = items.size),
                onClick = { onNavigate(route) }
            )
            if (index < items.size - 1) {
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun MoreItem(icon: ImageVector, title: String, shape: Shape, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = shape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun AccountScreen(
    onLogout: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
    updateViewModel: UpdateViewModel = koinViewModel()
) {
    val studentSessions by viewModel.studentSessions.collectAsStateWithLifecycle()
    val activeStudent by viewModel.activeStudent.collectAsStateWithLifecycle()
    val updateStatus by updateViewModel.status.collectAsStateWithLifecycle()

    UpdateDialog(
        status = updateStatus,
        viewModel = updateViewModel
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Konto i zarządzanie uczniami",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Przełączaj widoczność uczniów lub usuwaj niepotrzebne profile:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(studentSessions, key = { _, s -> s.id }) { index, student ->
                StudentAccountCard(
                    student = student,
                    isActive = student.id == activeStudent?.id,
                    shape = expressiveGroupShape(index = index, count = studentSessions.size),
                    onToggleEnabled = { enabled ->
                        viewModel.toggleStudentEnabled(student.id, enabled)
                    },
                    onRemove = {
                        viewModel.removeStudent(student.id, onLogout)
                    }
                )
                if (index < studentSessions.size - 1) {
                    Spacer(Modifier.height(3.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { updateViewModel.checkForUpdates() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sprawdź aktualizacje (v${getAppVersion()})")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onNavigateToAddAccount,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Dodaj kolejne konto EduVulcan")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.logoutAll(onLogout) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Wyloguj ze wszystkich kont",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun StudentAccountCard(
    student: StudentSession,
    isActive: Boolean,
    shape: Shape,
    onToggleEnabled: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    ElevatedCard(
        shape = shape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${student.account.pupil.firstName} ${student.account.pupil.surname}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (isActive) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = "Aktywny",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (!student.account.classDisplay.isNullOrBlank()) {
                        Text(
                            text = "Klasa: ${student.account.classDisplay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = student.account.unit.displayName.ifBlank { student.account.unit.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = student.isEnabled,
                    onCheckedChange = onToggleEnabled
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Usuń profil",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
