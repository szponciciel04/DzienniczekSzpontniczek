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

@Composable
private fun SubjectGradesSection(
    subject: String,
    grades: List<Grade>,
    average: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (average != null) {
                    Text(
                        text = "Śr: $average",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
private fun GradeChip(grade: Grade) {
    val color = when {
        (grade.value ?: 0.0) >= 4.5 -> MaterialTheme.colorScheme.primaryContainer
        (grade.value ?: 0.0) >= 3.0 -> MaterialTheme.colorScheme.secondaryContainer
        grade.value != null -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(2.dp)
    ) {
        Card(
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = color)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = grade.content,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (grade.column.weight > 0) {
                    Text(
                        text = "w=${grade.column.weight}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
