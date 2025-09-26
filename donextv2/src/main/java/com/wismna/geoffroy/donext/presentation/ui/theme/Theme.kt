package com.wismna.geoffroy.donext.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun DoNextTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = Purple40,
            onPrimary = LightSurfaceContainer,
            primaryContainer = Purple40Container,
            onPrimaryContainer = LightSurfaceContainer,
            secondary = PurpleGrey40,
            onSecondary = LightSurfaceContainer,
            secondaryContainer = PurpleGrey40Container,
            onSecondaryContainer = LightSurfaceContainer,
            tertiary = Pink80,
            onTertiary = DarkSurfaceContainer,
            tertiaryContainer = Pink40Container,
            onTertiaryContainer = LightSurfaceContainer,
            background = Color(0xFF121212),
            onBackground = LightSurfaceContainer,
            surface = Color(0xFF121212),
            onSurface = LightSurfaceContainer,
            surfaceVariant = DarkSurfaceContainer,
            onSurfaceVariant = LightSurfaceContainer,
            error = Color(0xFFCF6679),
            onError = DarkSurfaceContainer
        )

        else -> lightColorScheme(
            primary = Purple80,
            onPrimary = LightSurfaceContainer,
            primaryContainer = Purple80Container,
            onPrimaryContainer = DarkSurfaceContainer,
            secondary = PurpleGrey80,
            onSecondary = DarkSurfaceContainer,
            secondaryContainer = PurpleGrey80Container,
            onSecondaryContainer = DarkSurfaceContainer,
            tertiary = Pink40,
            onTertiary = LightSurfaceContainer,
            tertiaryContainer = Pink80Container,
            onTertiaryContainer = DarkSurfaceContainer,
            background = Color(0xFFFFFBFE),
            onBackground = DarkSurfaceContainer,
            surface = Color(0xFFFFFBFE),
            onSurface = DarkSurfaceContainer,
            surfaceVariant = LightSurfaceContainer,
            onSurfaceVariant = DarkSurfaceContainer,
            error = Color(0xFFB00020),
            onError = LightSurfaceContainer
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
