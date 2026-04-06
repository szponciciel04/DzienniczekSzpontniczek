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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.szpontium.api.hebe.models.Grade
import io.github.szpontium.viewmodel.GradesViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.round

@Composable
fun GradesScreen(viewModel: GradesViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> ErrorScreen(
            message = state.error!!,
            onRetry = { viewModel.load() }
        )
        state.grades.isEmpty() -> EmptyScreen("Brak ocen w bieżącym okresie")
        else -> {
            val bySubject = state.grades.groupBy { it.column.subject.name }
            val averageMap = state.averages.associate { it.subject.name to it.average }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                bySubject.forEach { (subject, grades) ->
                    item {
                        SubjectGradesSection(
                            subject = subject,
                            grades = grades,
                            average = averageMap[subject]
                        )
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubjectGradesSection(
    subject: String,
    grades: List<Grade>,
    average: String?
) {
    val eligibleGrades = grades.filter {
        val w = it.column.weight
        val v = it.value
        w > 0.0 && v != null && v >= 1.0 && v <= 6.0
    }
    val totalWeight = eligibleGrades.sumOf { it.column.weight }
    val calculatedAverage = if (totalWeight > 0.0) {
        val sum = eligibleGrades.sumOf { it.value!! * it.column.weight }
        round((sum / totalWeight) * 100) / 100.0
    } else null

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    if (average != null && average.isNotBlank()) {
                        Text(
                            text = "Śr: $average",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (calculatedAverage != null) {
                        Text(
                            text = "Śr. arytm: $calculatedAverage",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                grades.forEach { grade ->
                    GradeChip(grade)
                }
            }
        }
    }
}

@Composable
fun GradeChip(grade: Grade) {
    val color = when {
        (grade.value ?: 0.0) >= 4.5 -> MaterialTheme.colorScheme.primaryContainer
        (grade.value ?: 0.0) >= 3.0 -> MaterialTheme.colorScheme.secondaryContainer
        grade.value != null -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        (grade.value ?: 0.0) >= 4.5 -> MaterialTheme.colorScheme.onPrimaryContainer
        (grade.value ?: 0.0) >= 3.0 -> MaterialTheme.colorScheme.onSecondaryContainer
        grade.value != null -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = grade.content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (grade.column.weight > 0) {
                Text(
                    text = "waga: ${grade.column.weight}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            val d = grade.createdAt.date
            Text(
                text = "${d.dayOfMonth.toString().padStart(2, '0')}.${d.monthNumber.toString().padStart(2, '0')}.${d.year}",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}
