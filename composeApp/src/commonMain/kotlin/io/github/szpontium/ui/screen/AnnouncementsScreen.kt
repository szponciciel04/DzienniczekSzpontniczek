package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.api.hebe.models.Announcement
import io.github.szpontium.theme.expressiveGroupShape
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
        else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            itemsIndexed(state.announcements, key = { _, a -> a.id }) { index, announcement ->
                AnnouncementCard(
                    announcement = announcement,
                    shape = expressiveGroupShape(index = index, count = state.announcements.size)
                )
                if (index < state.announcements.size - 1) {
                    Spacer(Modifier.height(3.dp))
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AnnouncementCard(announcement: Announcement, shape: Shape) {
    ElevatedCard(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Od: ${announcement.dateFrom.day}.${announcement.dateFrom.monthNumber}.${announcement.dateFrom.year} · ${announcement.sender.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (announcement.content.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    text = announcement.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
