package com.noisevisionsoftware.fitapplication.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Podstawowe kolory
val Green80 = Color(0xFF4CAF50)
val Green40 = Color(0xFF8BC34A)
val Green20 = Color(0xFFCCE5CC)

val Mint80 = Color(0xFF00BFA5)
val Mint40 = Color(0xFF64FFDA)

val Beige80 = Color(0xFFF5F5DC)
val Beige40 = Color(0xFFF8F8F2)

// Kolory interfejsu
val Grey80 = Color(0xFF333333)
val Grey40 = Color(0xFF666666)
val Grey20 = Color(0xFF999999)

val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = Mint80,
    tertiary = Beige80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Grey80,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Green20,        // Dodane - dla wariantów powierzchni
    onSurfaceVariant = Grey40,       // Dodane - tekst na wariantach powierzchni
    outline = Grey40,                // Dodane - dla obramowań
    outlineVariant = Grey20,         // Dodane - dla alternatywnych obramowań
)

val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = Mint40,
    tertiary = Beige40,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Grey80,
    onBackground = Color.Black,
    onSurface = Color.Black,
    surfaceVariant = Green20,        // Dodane - dla wariantów powierzchni
    onSurfaceVariant = Grey40,       // Dodane - tekst na wariantach powierzchni
    outline = Grey40,                // Dodane - dla obramowań
    outlineVariant = Grey20,         // Dodane - dla alternatywnych obramowań
)