package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.api.hebe.models.Note
import io.github.szpontium.viewmodel.NotesViewModel
import org.koin.compose.viewmodel.koinViewModel

private val noteNeutralContainerLight = Color(0xFFFFF4E5)
private val noteNeutralContainerDark = Color(0xFF5A3D10)
private val noteNeutralOnContainerLight = Color(0xFF4A2E00)
private val noteNeutralOnContainerDark = Color(0xFFFFD9A8)

@Composable
fun NotesScreen(viewModel: NotesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> ErrorScreen(state.error!!, onRetry = { viewModel.load() })
        state.notes.isEmpty() -> EmptyScreen("Brak uwag")
        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.notes) { note ->
                NoteCard(note)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun NoteCard(note: Note) {
    val darkTheme = isSystemInDarkTheme()
    val negativeContainerColor = if (!darkTheme) noteNeutralContainerLight else noteNeutralContainerDark
    val negativeOnContainerColor = if (!darkTheme) noteNeutralOnContainerLight else noteNeutralOnContainerDark
    val containerColor = if (note.positive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        negativeContainerColor
    }
    val onContainerColor = if (note.positive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        negativeOnContainerColor
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (note.positive) "👍" else "⚠",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    val category = note.category
                    if (category != null) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = onContainerColor
                        )
                    }
                    Text(
                        text = "${note.dateValid.day}.${note.dateValid.monthNumber}.${note.dateValid.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.85f)
                    )
                }
                note.points?.let {
                    Text(
                        text = "${if (it > 0) "+" else ""}$it pkt",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = onContainerColor
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = onContainerColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Nauczyciel: ${note.creator.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = onContainerColor.copy(alpha = 0.85f)
            )
        }
    }
}
