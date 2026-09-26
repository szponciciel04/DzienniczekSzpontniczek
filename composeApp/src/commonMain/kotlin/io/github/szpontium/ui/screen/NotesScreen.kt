package io.github.szpontium.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
        else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
            items(state.notes, key = { it.id }) { note ->
                NoteCard(note)
                Spacer(Modifier.height(8.dp))
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

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (note.positive) "👍" else "⚠",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    val category = note.category
                    if (category != null) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
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
                    Surface(
                        color = onContainerColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${if (it > 0) "+" else ""}$it pkt",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = onContainerColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = onContainerColor
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Nauczyciel: ${note.creator.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = onContainerColor.copy(alpha = 0.85f)
            )
        }
    }
}
