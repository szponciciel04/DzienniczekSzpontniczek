package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.api.hebe.models.Homework
import io.github.szpontium.viewmodel.HomeworkViewModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel

private val POLISH_DAYS = mapOf(
    DayOfWeek.MONDAY to "Poniedziałek",
    DayOfWeek.TUESDAY to "Wtorek",
    DayOfWeek.WEDNESDAY to "Środa",
    DayOfWeek.THURSDAY to "Czwartek",
    DayOfWeek.FRIDAY to "Piątek",
    DayOfWeek.SATURDAY to "Sobota",
    DayOfWeek.SUNDAY to "Niedziela"
)

@Composable
fun HomeworkScreen(viewModel: HomeworkViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = { viewModel.previousWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Poprzedni tydzień")
                }
                val weekEnd = state.weekStart.plus(6, DateTimeUnit.DAY)
                Text(
                    text = "${state.weekStart.day}.${state.weekStart.month.number} – ${weekEnd.day}.${weekEnd.month.number}",
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(onClick = { viewModel.nextWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Następny tydzień")
                }
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> ErrorScreen(state.error!!, onRetry = { viewModel.load() })
            state.homework.isEmpty() -> EmptyScreen("Brak zadań w tym tygodniu")
            else -> {
                val byDay = state.homework.groupBy { it.deadline }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    (0..6).forEach { dayOffset ->
                        val date = state.weekStart.plus(dayOffset, DateTimeUnit.DAY)
                        val hwForDay = byDay[date] ?: emptyList()
                        if (hwForDay.isNotEmpty()) {
                            item {
                                DayHeader(date)
                                Column {
                                    hwForDay.forEach { hw ->
                                        HomeworkCard(hw)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${POLISH_DAYS[date.dayOfWeek] ?: date.dayOfWeek.name}, ${date.day}.${date.month.number}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun HomeworkCard(hw: Homework) {
    Card(
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
                    text = hw.subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "do ${hw.deadline.day}.${hw.deadline.month.number}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = hw.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Nauczyciel: ${hw.creator.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hw.isAnswerRequired) {
                Text(
                    text = "⚠ Wymagana odpowiedź",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
