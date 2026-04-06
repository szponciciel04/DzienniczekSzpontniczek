package io.github.szpontium.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun SzpontTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit)
) {
    val context = LocalContext.current
    val supportsMonet = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = if (supportsMonet && dynamicColor) dynamicDarkColorScheme(context).takeIf { darkTheme }
        ?: dynamicLightColorScheme(context) else darkScheme.takeIf { darkTheme }
        ?: lightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}