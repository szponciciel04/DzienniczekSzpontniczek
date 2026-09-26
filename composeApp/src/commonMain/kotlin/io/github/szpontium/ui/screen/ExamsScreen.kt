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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.api.hebe.models.Exam
import io.github.szpontium.theme.expressiveGroupShape
import io.github.szpontium.viewmodel.ExamsViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
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
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = { viewModel.previousWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Poprzedni tydzień")
                }
                val weekEnd = state.weekStart.plus(6, DateTimeUnit.DAY)
                Text(
                    text = "${state.weekStart.day}.${state.weekStart.monthNumber} – ${weekEnd.day}.${weekEnd.monthNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
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
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    (0..6).forEach { dayOffset ->
                        val date = state.weekStart.plus(dayOffset, DateTimeUnit.DAY)
                        val examsForDay = byDay[date] ?: emptyList()
                        if (examsForDay.isNotEmpty()) {
                            item {
                                DayHeader(date)
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    examsForDay.forEachIndexed { index, exam ->
                                        ExamCard(
                                            exam = exam,
                                            shape = expressiveGroupShape(index = index, count = examsForDay.size)
                                        )
                                        if (index < examsForDay.size - 1) {
                                            Spacer(Modifier.height(3.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
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
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "${POLISH_DAYS[date.dayOfWeek] ?: date.dayOfWeek.name}, ${date.day}.${date.monthNumber}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ExamCard(exam: Exam, shape: Shape = MaterialTheme.shapes.medium) {
    val isTest = exam.type.lowercase().contains("kartkówka") || exam.typeId == 2

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exam.subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (isTest)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = exam.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isTest)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (exam.content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = exam.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(6.dp))
            val deadlineDate = exam.deadline
            Text(
                text = "Termin: ${deadlineDate.day}.${deadlineDate.monthNumber}.${deadlineDate.year} · ${exam.creator.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
