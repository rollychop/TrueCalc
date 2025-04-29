package com.brohit.truecalc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.brohit.truecalc.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val fontName = GoogleFont("Roboto")

val fontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Black)
)


private val t = Typography()
val Typography = Typography(
    displayLarge = t.displayLarge.copy(fontFamily = fontFamily),
    displayMedium = t.displayMedium.copy(fontFamily = fontFamily),
    displaySmall = t.displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = t.headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = t.headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = t.headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = t.titleLarge.copy(fontFamily = fontFamily),
    titleMedium = t.titleMedium.copy(fontFamily = fontFamily),
    titleSmall = t.titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = t.bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = t.bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = t.bodySmall.copy(fontFamily = fontFamily),
    labelLarge = t.labelLarge.copy(fontFamily = fontFamily),
    labelMedium = t.labelMedium.copy(fontFamily = fontFamily),
    labelSmall = t.labelSmall.copy(fontFamily = fontFamily)
)