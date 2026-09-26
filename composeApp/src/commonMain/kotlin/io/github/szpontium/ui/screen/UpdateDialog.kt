package io.github.szpontium.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.szpontium.update.UpdateStatus
import io.github.szpontium.update.getAppVersion
import io.github.szpontium.viewmodel.UpdateViewModel

@Composable
fun UpdateDialog(
    status: UpdateStatus,
    viewModel: UpdateViewModel
) {
    val currentVersion = getAppVersion()

    when (status) {
        is UpdateStatus.Idle -> { /* Do nothing */ }
        is UpdateStatus.Checking -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                },
                title = { Text("Sprawdzanie aktualizacji...") },
                text = { Text("Pobieranie informacji o najnowszej wersji z serwisu GitHub.") },
                confirmButton = {}
            )
        }
        is UpdateStatus.UpToDate -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Aplikacja jest aktualna") },
                text = { Text("Posiadasz najnowszą wersję Szpontium (v$currentVersion).") },
                confirmButton = {
                    Button(onClick = { viewModel.dismiss() }) {
                        Text("OK")
                    }
                }
            )
        }
        is UpdateStatus.Available -> {
            val info = status.info
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Dostępna nowa wersja (v${info.version})") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Obecna wersja: v$currentVersion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (info.apkSize > 0) {
                            val sizeMb = info.apkSize / (1024f * 1024f)
                            Text(
                                text = "Rozmiar pliku: ${roundOneDecimal(sizeMb)} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Co nowego:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = info.releaseNotes,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.startDownloadAndInstall(info) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Pobierz i zainstaluj")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismiss() }) {
                        Text("Później")
                    }
                }
            )
        }
        is UpdateStatus.Downloading -> {
            val info = status.info
            val progress = status.progress
            val percent = (progress * 100).toInt()

            AlertDialog(
                onDismissRequest = { /* Prevent dismiss while downloading */ },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Pobieranie v${info.version}") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Pobieranie pakietu instalacyjnego APK...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }
        is UpdateStatus.ReadyToInstall -> {
            val apkPath = status.apkFilePath
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.SystemUpdate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Pobieranie zakończone") },
                text = { Text("Pakiet APK jest gotowy. Kliknij poniżej, aby uruchomić instalator systemu Android.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.installDownloadedApk(apkPath) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Zainstaluj teraz")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.dismiss() }) {
                        Text("Zamknij")
                    }
                }
            )
        }
        is UpdateStatus.Error -> {
            val msg = status.message
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = { Text("Błąd aktualizacji") },
                text = { Text(msg) },
                confirmButton = {
                    FilledTonalButton(onClick = { viewModel.dismiss() }) {
                        Text("Zamknij")
                    }
                }
            )
        }
    }
}

private fun roundOneDecimal(valMb: Float): String {
    val x = (valMb * 10).toInt() / 10f
    return "$x"
}
