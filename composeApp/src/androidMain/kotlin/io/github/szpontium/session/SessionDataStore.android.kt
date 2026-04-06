package io.github.szpontium.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

private var applicationContext: Context? = null

internal fun initAndroidDataStoreContext(context: Context) {
    applicationContext = context.applicationContext
}

actual fun createSessionDataStore(): DataStore<Preferences> =
    createSessionDataStore {
        requireNotNull(applicationContext) { "Call initAndroidDataStoreContext() before using DataStore" }
            .filesDir.resolve(SESSION_DATASTORE_FILE).absolutePath
    }
