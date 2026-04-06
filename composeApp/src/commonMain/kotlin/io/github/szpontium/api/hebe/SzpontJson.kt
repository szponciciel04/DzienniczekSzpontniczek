package io.github.szpontium.api.hebe

import kotlinx.serialization.json.Json

internal val szpontJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
