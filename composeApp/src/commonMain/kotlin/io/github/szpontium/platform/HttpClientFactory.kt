package io.github.szpontium.platform

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
