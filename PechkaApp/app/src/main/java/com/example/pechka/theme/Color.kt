package com.example.pechka.theme

import androidx.compose.ui.graphics.Color

// Artisanal Transit palette — bakery-inspired wheat & crust tones.
// Variable names are kept (Green, Gray) for backward-compat with the rest
// of the codebase, but the values are now the warm bakery palette.

// Primary "wheat / crust" — used for headlines, icons, active accents
val PrimaryGreen = Color(0xFF7D562D)            // primary
val PrimaryContainerGreen = Color(0xFF5B3912)   // on-primary-container — deep crust for solid buttons
val OnPrimaryGreen = Color(0xFFFFFFFF)

// Cream / white surfaces
val BackgroundGray = Color(0xFFFFF8F4)          // background / surface — soft cream
val SurfaceWhite = Color(0xFFFFFFFF)            // surface-container-lowest — pure white cards
val SurfaceContainer = Color(0xFFFBEBDB)        // surface-container — warm tonal
val SurfaceContainerLow = Color(0xFFFFF1E5)     // surface-container-low
val SurfaceContainerHigh = Color(0xFFF6E6D6)    // surface-container-high
val SurfaceContainerHighest = Color(0xFFF0E0D0) // surface-container-highest

// Outlines
val OutlineGray = Color(0xFF82756A)             // outline
val OutlineVariant = Color(0xFFE9D5C3)          // outline-variant — light wheat hairline

val ErrorRed = Color(0xFFBA1A1A)
val ErrorContainerRed = Color(0xFFFFDAD6)
val OnErrorContainerRed = Color(0xFF93000A)

// "Warning" / in-progress accent — light wheat amber, matches "В работе" pill
val WarningOrange = Color(0xFFFDC085)

// On-surface-variant / muted text
val SecondaryGray = Color(0xFF50453B)
val SecondaryContainerGray = Color(0xFFFDC085)  // secondary-container — warm pill background
val OnSecondaryContainerGray = Color(0xFF784C1C)
