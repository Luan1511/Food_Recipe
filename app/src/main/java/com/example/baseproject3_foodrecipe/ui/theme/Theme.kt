package com.example.baseproject3_foodrecipe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF9800),            // Màu chính: Cam
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color.Black,

    secondary = Color(0xFF8BC34A),          // Accent: Xanh rau củ
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC5E1A5),
    onSecondaryContainer = Color.Black,

    background = Color(0xFFFDFDFD),
    onBackground = Color(0xFF333333),

    surface = Color.White,
    onSurface = Color(0xFF212121),

    error = Color(0xFFB00020),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF424242),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFFAED581),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF33691E),
    onSecondaryContainer = Color.White,

    background = Color(0xFF121212),
    onBackground = Color(0xFFECECEC),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),

    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun FoodRecipeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FoodRecipeTypography,
        shapes = FoodRecipeShapes,
        content = content
    )
}
