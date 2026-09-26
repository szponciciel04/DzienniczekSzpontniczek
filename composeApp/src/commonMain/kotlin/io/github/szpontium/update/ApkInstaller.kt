package io.github.szpontium.update

expect class ApkInstaller {
    fun install(apkFilePath: String)
    fun canRequestPackageInstalls(): Boolean
    fun openInstallPermissionSettings()
}
