package io.github.szpontium

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform