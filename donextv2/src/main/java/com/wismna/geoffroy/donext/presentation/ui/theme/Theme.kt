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
            onPrimary = Color.White,
            primaryContainer = Purple40Container,
            onPrimaryContainer = Color.White,
            secondary = PurpleGrey40,
            onSecondary = Color.White,
            secondaryContainer = PurpleGrey40Container,
            onSecondaryContainer = Color.White,
            tertiary = Pink40,
            onTertiary = Color.White,
            tertiaryContainer = Pink40Container,
            onTertiaryContainer = Color.White,
            background = Color(0xFF121212),
            onBackground = Color.White,
            surface = Color(0xFF121212),
            onSurface = Color.White,
            surfaceVariant = DarkSurfaceContainer,
            onSurfaceVariant = Color.White,
            error = Color(0xFFCF6679),
            onError = Color.Black
        )

        else -> lightColorScheme(
            primary = Purple80,
            onPrimary = Color.Black,
            primaryContainer = Purple80Container,
            onPrimaryContainer = Color.Black,
            secondary = PurpleGrey80,
            onSecondary = Color.Black,
            secondaryContainer = PurpleGrey80Container,
            onSecondaryContainer = Color.Black,
            tertiary = Pink80,
            onTertiary = Color.Black,
            tertiaryContainer = Pink80Container,
            onTertiaryContainer = Color.Black,
            background = Color(0xFFFFFBFE),
            onBackground = Color.Black,
            surface = Color(0xFFFFFBFE),
            onSurface = Color.Black,
            surfaceVariant = LightSurfaceContainer,
            onSurfaceVariant = Color.Black,
            error = Color(0xFFB00020),
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
