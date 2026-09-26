package io.github.szpontium.update

import platform.Foundation.NSBundle

actual fun getAppVersion(): String {
    return (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String) ?: "0.2.0"
}
