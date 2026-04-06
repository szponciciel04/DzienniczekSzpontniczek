package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Announcement
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.szpontium.navigation.Route

@Composable
fun MoreScreen(onNavigate: (Route) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MoreItem(
            icon = Icons.Outlined.EmojiEvents,
            title = "Uwagi i osiągnięcia",
            onClick = { onNavigate(Route.Notes) }
        )
        Spacer(Modifier.height(12.dp))
        MoreItem(
            icon = Icons.AutoMirrored.Outlined.Announcement,
            title = "Ogłoszenia",
            onClick = { onNavigate(Route.Announcements) }
        )
        Spacer(Modifier.height(12.dp))
        MoreItem(
            icon = Icons.Outlined.Book,
            title = "Zadania domowe",
            onClick = { onNavigate(Route.Homework) }
        )
        Spacer(Modifier.height(12.dp))
        MoreItem(
            icon = Icons.Outlined.Person,
            title = "Konto",
            onClick = { onNavigate(Route.Account) }
        )
    }
}

@Composable
private fun MoreItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
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
fun AccountScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Konto",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        HorizontalDivider()
        TextButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Wyloguj się",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
