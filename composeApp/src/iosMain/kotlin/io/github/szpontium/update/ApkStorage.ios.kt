package io.github.szpontium.update

actual class ApkStorage {
    actual fun getApkPath(): String = ""
    actual fun appendBytes(path: String, bytes: ByteArray, length: Int, clearFirst: Boolean) {}
}
