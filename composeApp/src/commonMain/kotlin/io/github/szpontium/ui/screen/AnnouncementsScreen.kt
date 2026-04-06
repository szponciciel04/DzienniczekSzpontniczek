package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.api.hebe.models.Announcement
import io.github.szpontium.viewmodel.AnnouncementsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnnouncementsScreen(viewModel: AnnouncementsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> ErrorScreen(state.error!!, onRetry = { viewModel.load() })
        state.announcements.isEmpty() -> EmptyScreen("Brak ogłoszeń")
        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.announcements) { announcement ->
                AnnouncementCard(announcement)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AnnouncementCard(announcement: Announcement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Od: ${announcement.dateFrom.day}.${announcement.dateFrom.monthNumber}.${announcement.dateFrom.year} · ${announcement.sender.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (announcement.content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
                Text(
                    text = announcement.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
