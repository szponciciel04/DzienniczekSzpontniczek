package io.github.szpontium.update

actual class ApkInstaller {
    actual fun canRequestPackageInstalls(): Boolean = false
    actual fun openInstallPermissionSettings() {}
    actual fun install(apkFilePath: String) {}
}
