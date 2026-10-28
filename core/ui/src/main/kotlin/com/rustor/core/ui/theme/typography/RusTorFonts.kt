package com.rustor.core.ui.theme.typography

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.rustor.core.ui.R

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val onest = GoogleFont("Onest")

internal val OnestFontFamily = FontFamily(
    Font(
        googleFont = onest,
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = onest,
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = onest,
        fontProvider = googleFontProvider,
        weight = FontWeight.SemiBold,
        style = FontStyle.Normal,
    ),
    Font(
        googleFont = onest,
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold,
        style = FontStyle.Normal,
    ),
)
