package io.github.szpontium.theme

import androidx.compose.runtime.Composable

@Composable
actual fun SzpontTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit)
) {
}