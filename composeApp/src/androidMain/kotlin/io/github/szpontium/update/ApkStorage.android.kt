package io.github.szpontium.update

import android.content.Context
import java.io.File
import java.io.FileOutputStream

actual class ApkStorage(private val context: Context) {

    actual fun getApkPath(): String {
        return File(context.cacheDir, "szpontium-update.apk").absolutePath
    }

    actual fun appendBytes(path: String, bytes: ByteArray, length: Int, clearFirst: Boolean) {
        val file = File(path)
        if (clearFirst && file.exists()) {
            file.delete()
        }
        FileOutputStream(file, true).use { fos ->
            fos.write(bytes, 0, length)
        }
    }
}
