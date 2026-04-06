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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import io.github.szpontium.api.hebe.models.Exam
import io.github.szpontium.viewmodel.ExamsViewModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
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
fun ExamsScreen(viewModel: ExamsViewModel = koinViewModel()) {
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
                    text = "${state.weekStart.day}.${state.weekStart.monthNumber} – ${weekEnd.day}.${weekEnd.monthNumber}",
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
            state.exams.isEmpty() -> EmptyScreen("Brak sprawdzianów w tym tygodniu")
            else -> {
                val byDay = state.exams.groupBy { it.deadline }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    (0..6).forEach { dayOffset ->
                        val date = state.weekStart.plus(dayOffset, DateTimeUnit.DAY)
                        val examsForDay = byDay[date] ?: emptyList()
                        if (examsForDay.isNotEmpty()) {
                            item {
                                DayHeader(date)
                                Column {
                                    examsForDay.forEach { exam ->
                                        ExamCard(exam)
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
            text = "${POLISH_DAYS[date.dayOfWeek] ?: date.dayOfWeek.name}, ${date.day}.${date.monthNumber}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ExamCard(exam: Exam) {
    val isTest = exam.type.lowercase().contains("kartkówka") || exam.typeId == 2

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isTest)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer
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
                    text = exam.subject.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Badge(
                    containerColor = if (isTest)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                ) {
                    Text(exam.type, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(4.dp))
            if (exam.content.isNotBlank()) {
                Text(
                    text = exam.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
            }
            val deadlineDate = exam.deadline
            Text(
                text = "Termin: ${deadlineDate.day}.${deadlineDate.monthNumber}.${deadlineDate.year}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Nauczyciel: ${exam.creator.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
