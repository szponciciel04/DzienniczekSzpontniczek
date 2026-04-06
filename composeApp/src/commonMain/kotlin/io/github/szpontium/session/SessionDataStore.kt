package io.github.szpontium.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal const val SESSION_DATASTORE_FILE = "session.preferences_pb"

internal fun createSessionDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

expect fun createSessionDataStore(): DataStore<Preferences>
