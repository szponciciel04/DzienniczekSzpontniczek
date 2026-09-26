package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.navigation.Route
import io.github.szpontium.theme.expressiveGroupShape
import io.github.szpontium.ui.model.UiMessage
import io.github.szpontium.viewmodel.MessageTab
import io.github.szpontium.viewmodel.MessagesViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(onNavigate: (Route) -> Unit, viewModel: MessagesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val tabs = listOf(
        MessageTab.RECEIVED to "Odebrane",
        MessageTab.SENT to "Wysłane",
        MessageTab.DELETED to "Usunięte"
    )
    val selectedTabIndex = tabs.indexOfFirst { it.first == state.currentTab }.takeIf { it >= 0 } ?: 0

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, (tab, title) ->
                        SegmentedButton(
                            selected = selectedTabIndex == index,
                            onClick = { viewModel.setTab(tab) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                            label = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    ErrorScreen(state.error!!, onRetry = { viewModel.loadMessages() })
                }
                state.messages.isEmpty() -> {
                    EmptyScreen("Brak wiadomości w tym folderze")
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                        itemsIndexed(state.messages, key = { _, m -> m.id }) { index, message ->
                            MessageCard(
                                message = message,
                                shape = expressiveGroupShape(index = index, count = state.messages.size),
                                onClick = {
                                    onNavigate(Route.MessageDetails(id = message.id, isHebe = message.content != null, hebeContent = message.content))
                                }
                            )
                            if (index < state.messages.size - 1) {
                                Spacer(Modifier.height(3.dp))
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: UiMessage,
    shape: Shape,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (message.isUnread)
                MaterialTheme.colorScheme.surfaceContainerHigh
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                if (message.isUnread) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailUnread,
                        contentDescription = "Nieprzeczytana",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = message.title.ifBlank { "(brak tematu)" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (message.isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message.senderOrRecipient,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (message.isUnread) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                if (message.hasAttachments) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Attachment,
                        contentDescription = "Załącznik",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (message.date != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${message.date.dayOfMonth}.${message.date.monthNumber}.${message.date.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
