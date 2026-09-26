package io.github.szpontium.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.api.hebe.models.Schedule
import io.github.szpontium.api.hebe.models.effectiveNoteOrReason
import io.github.szpontium.api.hebe.models.effectivePosition
import io.github.szpontium.api.hebe.models.effectiveRoom
import io.github.szpontium.api.hebe.models.effectiveSubject
import io.github.szpontium.api.hebe.models.effectiveTeacher
import io.github.szpontium.api.hebe.models.effectiveTimeSlot
import io.github.szpontium.api.hebe.models.isCanceled
import io.github.szpontium.api.hebe.models.isMerge
import io.github.szpontium.api.hebe.models.isRescheduled
import io.github.szpontium.theme.expressiveGroupShape
import io.github.szpontium.viewmodel.TimetableViewModel
import io.github.szpontium.viewmodel.mondayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
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

private val POLISH_SHORT_DAYS = listOf("Pn", "Wt", "Śr", "Cz", "Pt")

private val substitutionContainerLight = Color(0xFFFFF3CD)
private val substitutionContainerDark = Color(0xFF5C4A00)
private val substitutionOnContainerLight = Color(0xFF4A3A00)
private val substitutionOnContainerDark = Color(0xFFFFE08A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(viewModel: TimetableViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedDate = state.selectedDate
    val weekStart = mondayOfWeek(selectedDate)

    Column(modifier = Modifier.fillMaxSize()) {
        // Day navigation bar
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    IconButton(onClick = { viewModel.previousDay() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Poprzedni dzień")
                    }
                    Text(
                        text = "${POLISH_DAYS[selectedDate.dayOfWeek] ?: selectedDate.dayOfWeek.name}, ${selectedDate.day}.${selectedDate.monthNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.nextDay() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Następny dzień")
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Monday - Friday Day Selector Row
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    (0..4).forEach { dayOffset ->
                        val dayDate = weekStart.plus(dayOffset, DateTimeUnit.DAY)
                        val isSelected = dayDate == selectedDate
                        val dayLabel = "${POLISH_SHORT_DAYS[dayOffset]} ${dayDate.day}"

                        SegmentedButton(
                            selected = isSelected,
                            onClick = { viewModel.selectDate(dayDate) },
                            shape = SegmentedButtonDefaults.itemShape(index = dayOffset, count = 5),
                            label = {
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> ErrorScreen(state.error!!, onRetry = { viewModel.selectDate(selectedDate) })
            state.dayLessons.isEmpty() -> EmptyScreen("Brak lekcji w tym dniu")
            else -> {
                val dayLessons = state.dayLessons
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            dayLessons.forEachIndexed { index, lesson ->
                                LessonCard(
                                    lesson = lesson,
                                    shape = expressiveGroupShape(index = index, count = dayLessons.size)
                                )
                                if (index < dayLessons.size - 1) {
                                    Spacer(Modifier.height(3.dp))
                                }
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
private fun LessonCard(lesson: Schedule, shape: Shape) {
    val isCanceled = lesson.isCanceled
    val isSubstitution = lesson.substitution != null
    val isMerge = lesson.isMerge
    val isRescheduled = lesson.isRescheduled

    val darkTheme = isSystemInDarkTheme()
    val substitutionContainerColor = if (!darkTheme) substitutionContainerLight else substitutionContainerDark
    val substitutionOnContainerColor = if (!darkTheme) substitutionOnContainerLight else substitutionOnContainerDark

    val containerColor = when {
        isCanceled -> MaterialTheme.colorScheme.errorContainer
        isSubstitution -> substitutionContainerColor
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val onContainerColor = when {
        isCanceled -> MaterialTheme.colorScheme.onErrorContainer
        isSubstitution -> substitutionOnContainerColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    val effectiveTimeSlot = lesson.effectiveTimeSlot

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${lesson.effectivePosition}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.effectiveSubject?.name ?: "Brak nazwy",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onContainerColor
                )

                val teacher = lesson.effectiveTeacher?.displayName
                if (teacher != null) {
                    Text(
                        text = teacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.88f)
                    )
                }

                val room = lesson.effectiveRoom?.code
                if (room != null) {
                    Text(
                        text = "Sala: $room",
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.88f)
                    )
                }

                val note = lesson.effectiveNoteOrReason
                if (!note.isNullOrBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = onContainerColor.copy(alpha = 0.95f)
                    )
                }

                when {
                    isCanceled -> {
                        Text(
                            text = "ODWOŁANA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    isMerge -> {
                        Text(
                            text = "Połączone grupy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = substitutionOnContainerColor
                        )
                    }
                    isRescheduled -> {
                        val originDate = lesson.date
                        val subDate = lesson.substitution?.date
                        val rescheduledLabel = if (subDate != null && subDate != originDate) {
                            "Przeniesiona z ${originDate.day}.${originDate.monthNumber} (lekcja ${lesson.timeSlot.position})"
                        } else {
                            "Przeniesiona z lekcji ${lesson.timeSlot.position}"
                        }
                        Text(
                            text = rescheduledLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = substitutionOnContainerColor
                        )
                    }
                    isSubstitution -> {
                        Text(
                            text = "Zastępstwo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = substitutionOnContainerColor
                        )
                    }
                }
            }
            Text(
                text = "${effectiveTimeSlot.start} – ${effectiveTimeSlot.end}",
                style = MaterialTheme.typography.bodySmall,
                color = onContainerColor.copy(alpha = 0.88f)
            )
        }
    }
}
