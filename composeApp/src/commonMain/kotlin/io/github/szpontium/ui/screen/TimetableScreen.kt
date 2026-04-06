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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import io.github.szpontium.api.hebe.models.Schedule
import io.github.szpontium.viewmodel.TimetableViewModel
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

private val substitutionContainerLight = Color(0xFFFFF3CD)
private val substitutionContainerDark = Color(0xFF5C4A00)
private val substitutionOnContainerLight = Color(0xFF4A3A00)
private val substitutionOnContainerDark = Color(0xFFFFE08A)

@Composable
fun TimetableScreen(viewModel: TimetableViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Week navigation header
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
                val weekEnd = state.weekStart.plus(4, DateTimeUnit.DAY)
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
            state.error != null -> ErrorScreen(state.error!!, onRetry = { viewModel.nextWeek() })
            state.schedule.isEmpty() -> EmptyScreen("Brak lekcji w tym tygodniu")
            else -> {
                val byDay = state.schedule.groupBy { it.date }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    (0..4).forEach { dayOffset ->
                        val date = state.weekStart.plus(dayOffset, DateTimeUnit.DAY)
                        val lessons = byDay[date] ?: emptyList()
                        if (lessons.isNotEmpty()) {
                            item {
                                DayHeader(date)
                                Column {
                                    lessons.forEach { lesson ->
                                        LessonCard(lesson)
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
private fun LessonCard(lesson: Schedule) {
    val isSubstitution = lesson.substitution != null
    val isCancelled = lesson.substitution?.change?.type == 2
    val darkTheme = isSystemInDarkTheme()
    val substitutionContainerColor = if (!darkTheme) {
        substitutionContainerLight
    } else {
        substitutionContainerDark
    }
    val substitutionOnContainerColor = if (!darkTheme) {
        substitutionOnContainerLight
    } else {
        substitutionOnContainerDark
    }

    val containerColor = when {
        isCancelled -> MaterialTheme.colorScheme.errorContainer
        isSubstitution -> substitutionContainerColor
        else -> MaterialTheme.colorScheme.surface
    }

    val onContainerColor = when {
        isCancelled -> MaterialTheme.colorScheme.onErrorContainer
        isSubstitution -> substitutionOnContainerColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${lesson.timeSlot.position}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.substitution?.subject?.name ?: lesson.subject?.name ?: "Brak nazwy",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onContainerColor
                )
                val teacher = lesson.substitution?.teacherPrimary?.displayName
                    ?: lesson.teacherPrimary?.displayName
                if (teacher != null) {
                    Text(
                        text = teacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.88f)
                    )
                }
                val room = lesson.substitution?.room?.code ?: lesson.room?.code
                if (room != null) {
                    Text(
                        text = "Sala: $room",
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.88f)
                    )
                }
                if (isCancelled) {
                    Text(
                        text = "ODWOŁANA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (isSubstitution) {
                    Text(
                        text = "Zastępstwo",
                        style = MaterialTheme.typography.labelSmall,
                        color = substitutionOnContainerColor
                    )
                }
            }
            Text(
                text = "${lesson.timeSlot.start} – ${lesson.timeSlot.end}",
                style = MaterialTheme.typography.bodySmall,
                color = onContainerColor.copy(alpha = 0.88f)
            )
        }
    }
}
