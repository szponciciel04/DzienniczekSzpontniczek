package io.github.szpontium.ui.screen

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
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import io.github.szpontium.api.hebe.models.Exam
import io.github.szpontium.viewmodel.ExamsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExamsScreen(viewModel: ExamsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> ErrorScreen(state.error!!, onRetry = { viewModel.load() })
        state.exams.isEmpty() -> EmptyScreen("Brak nadchodzących sprawdzianów")
        else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.exams) { exam ->
                ExamCard(exam)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ExamCard(exam: Exam) {
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
