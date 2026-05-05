package io.github.szpontium.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
actual fun SzpontTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit)
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme else lightScheme,
        content = content
    )
}