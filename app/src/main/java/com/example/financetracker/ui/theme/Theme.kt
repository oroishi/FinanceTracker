package com.example.financetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryDark,
    secondary = BrandSecondaryDark,
    tertiary = BrandTertiaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandTertiary
)

data class FinanceColors(
    val income: Color,
    val expense: Color
)

val LocalFinanceColors = compositionLocalOf {
    FinanceColors(income = Income, expense = Expense)
}

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val financeColors = if (darkTheme) {
        FinanceColors(income = IncomeDark, expense = ExpenseDark)
    } else {
        FinanceColors(income = Income, expense = Expense)
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalFinanceColors provides financeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
