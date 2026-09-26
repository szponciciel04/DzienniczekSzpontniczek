package io.github.szpontium.update

expect class ApkStorage {
    fun getApkPath(): String
    fun appendBytes(path: String, bytes: ByteArray, length: Int, clearFirst: Boolean)
}
