package io.github.szpontium.update

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/szponciciel04/DzienniczekSzpontniczek/releases/latest"
private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class GithubAsset(
    val name: String = "",
    @SerialName("browser_download_url") val browserDownloadUrl: String = "",
    val size: Long = 0L
)

@Serializable
data class GithubRelease(
    @SerialName("tag_name") val tagName: String = "",
    val name: String = "",
    val body: String = "",
    val assets: List<GithubAsset> = emptyList()
)

data class UpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val apkSize: Long
)

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data object UpToDate : UpdateStatus
    data class Available(val info: UpdateInfo) : UpdateStatus
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateStatus
    data class ReadyToInstall(val info: UpdateInfo, val apkFilePath: String) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

fun isNewerVersion(current: String, latest: String): Boolean {
    val cleanCurrent = current.removePrefix("v").trim()
    val cleanLatest = latest.removePrefix("v").trim()

    val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
    val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

    val length = maxOf(currentParts.size, latestParts.size)
    for (i in 0 until length) {
        val curr = currentParts.getOrElse(i) { 0 }
        val lat = latestParts.getOrElse(i) { 0 }
        if (lat > curr) return true
        if (lat < curr) return false
    }
    return false
}

class UpdateManager(
    private val httpClient: HttpClient,
    val apkInstaller: ApkInstaller
) {
    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    suspend fun checkForUpdates(currentVersion: String = getAppVersion()) {
        _status.value = UpdateStatus.Checking
        try {
            val response = httpClient.get(GITHUB_RELEASES_URL) {
                header(HttpHeaders.UserAgent, "SzpontiumApp/$currentVersion")
            }

            if (!response.status.isSuccess()) {
                _status.value = UpdateStatus.Error("Błąd serwera GitHub (${response.status.value})")
                return
            }

            val bodyText = response.bodyAsText()
            val release = json.decodeFromString<GithubRelease>(bodyText)
            val latestVersion = release.tagName.removePrefix("v").trim()

            if (isNewerVersion(currentVersion, latestVersion)) {
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                    ?: release.assets.firstOrNull()

                if (apkAsset == null) {
                    _status.value = UpdateStatus.Error("Brak pliku APK w najnowszym wydaniu GitHub")
                    return
                }

                val info = UpdateInfo(
                    version = latestVersion,
                    releaseNotes = release.body.ifBlank { release.name },
                    downloadUrl = apkAsset.browserDownloadUrl,
                    apkSize = apkAsset.size
                )
                _status.value = UpdateStatus.Available(info)
            } else {
                _status.value = UpdateStatus.UpToDate
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _status.value = UpdateStatus.Error(e.message ?: "Błąd sprawdzania aktualizacji")
        }
    }

    suspend fun downloadAndInstallApk(
        info: UpdateInfo,
        saveFilePath: String,
        writeBytes: suspend (ByteArray, Int) -> Unit
    ) {
        _status.value = UpdateStatus.Downloading(info, 0f)
        try {
            val response = httpClient.get(info.downloadUrl) {
                header(HttpHeaders.UserAgent, "SzpontiumApp/${getAppVersion()}")
            }

            if (!response.status.isSuccess()) {
                _status.value = UpdateStatus.Error("Błąd pobierania APK (${response.status.value})")
                return
            }

            val channel: ByteReadChannel = response.bodyAsChannel()
            val totalBytes = info.apkSize.takeIf { it > 0 } ?: response.headers[HttpHeaders.ContentLength]?.toLongOrNull() ?: -1L
            var downloadedBytes = 0L

            val buffer = ByteArray(8192)
            while (!channel.isClosedForRead) {
                val read = channel.readAvailable(buffer, 0, buffer.size)
                if (read <= 0) break

                writeBytes(buffer, read)
                downloadedBytes += read

                val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0.5f
                _status.value = UpdateStatus.Downloading(info, progress)
            }

            _status.value = UpdateStatus.ReadyToInstall(info, saveFilePath)
            
            if (apkInstaller.canRequestPackageInstalls()) {
                apkInstaller.install(saveFilePath)
            } else {
                apkInstaller.openInstallPermissionSettings()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _status.value = UpdateStatus.Error(e.message ?: "Błąd pobierania pliku APK")
        }
    }

    fun installDownloadedApk(saveFilePath: String) {
        if (apkInstaller.canRequestPackageInstalls()) {
            apkInstaller.install(saveFilePath)
        } else {
            apkInstaller.openInstallPermissionSettings()
        }
    }

    fun dismiss() {
        _status.value = UpdateStatus.Idle
    }
}
