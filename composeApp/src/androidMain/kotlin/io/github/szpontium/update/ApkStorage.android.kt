package io.github.szpontium.update

import android.content.Context
import io.github.szpontium.platform.appContext
import java.io.File
import java.io.FileOutputStream

actual class ApkStorage(private val context: Context? = appContext) {

    actual fun getApkPath(): String {
        val ctx = context ?: error("AppContext is null in ApkStorage")
        return File(ctx.cacheDir, "szpontium-update.apk").absolutePath
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
