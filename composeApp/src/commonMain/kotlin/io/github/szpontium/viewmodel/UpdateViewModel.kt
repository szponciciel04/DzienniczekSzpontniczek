package io.github.szpontium.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.szpontium.update.ApkStorage
import io.github.szpontium.update.UpdateInfo
import io.github.szpontium.update.UpdateManager
import io.github.szpontium.update.UpdateStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(
    private val updateManager: UpdateManager,
    private val apkStorage: ApkStorage
) : ViewModel() {

    val status: StateFlow<UpdateStatus> = updateManager.status

    fun checkForUpdates() {
        viewModelScope.launch {
            updateManager.checkForUpdates()
        }
    }

    fun startDownloadAndInstall(info: UpdateInfo) {
        val apkPath = apkStorage.getApkPath()
        viewModelScope.launch {
            var isFirst = true
            updateManager.downloadAndInstallApk(
                info = info,
                saveFilePath = apkPath,
                writeBytes = { bytes, len ->
                    apkStorage.appendBytes(apkPath, bytes, len, clearFirst = isFirst)
                    isFirst = false
                }
            )
        }
    }

    fun installDownloadedApk(apkPath: String) {
        updateManager.installDownloadedApk(apkPath)
    }

    fun dismiss() {
        updateManager.dismiss()
    }
}
