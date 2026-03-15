package com.fourthshelfmedia.sleepanchor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SleepAnchorColors = darkColorScheme(
    primary = AnchorAccent,
    onPrimary = AnchorBlack,
    secondary = AnchorTeal,
    onSecondary = AnchorTextPrimary,
    background = AnchorBlack,
    onBackground = AnchorTextPrimary,
    surface = AnchorSurface,
    onSurface = AnchorTextPrimary,
    surfaceVariant = AnchorCard,
    onSurfaceVariant = AnchorTextSecondary,
    outline = AnchorDivider,
)

private val SleepAnchorTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 28.sp,
        color = AnchorTextPrimary,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize = 22.sp,
        color = AnchorTextPrimary,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        color = AnchorTextPrimary,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = AnchorTextPrimary,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        color = AnchorTextPrimary,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        color = AnchorTextSecondary,
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = AnchorTextDim,
    ),
)

@Composable
fun SleepAnchorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SleepAnchorColors,
        typography = SleepAnchorTypography,
        content = content,
    )
}
