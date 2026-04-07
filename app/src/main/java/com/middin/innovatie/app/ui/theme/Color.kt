package com.middin.innovatie.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Middin Innovatie design tokens (CSS variable names preserved for parity with web).
 * Primary light: solid equivalent of rgba(0, 26, 158, ~0.051) on white → #F2F3FA.
 */
object MiddinBrandColors {
    val Primary = Color(0xFF001A9E)
    val PrimaryLight = Color(0xFFF2F3FA)
    /** 5.1% primary on white for overlays / scrims */
    val PrimaryLightAlpha = Color(0x0D001A9E)

    val Secondary = Color(0xFFF9F2C8)
    val SecondaryLight = Color(0xFFB4CCD0)
    val SecondaryDark = Color(0xFFF2E491)

    val Green = Color(0xFF76E1CA)
    val Yellow = Color(0xFFF2E591)
    val Orange = Color(0xFFFF9E8C)
    val OrangeText = Color(0xFFFF9E8C)

    val Text = Color(0xFF001A9E)
    val TextLight = Color(0xFF777777)

    val Background = Color(0xFFFFFFFF)
    val BackgroundLight = Color(0xFFF2F2F2)

    val Border = Color(0xFFCCCCCC)
    val BorderInput = Color(0xFF999999)

    val White = Color(0xFFFFFFFF)
}

/** @deprecated Use [MiddinBrandColors.Primary] */
val MiddinPrimary = MiddinBrandColors.Primary

/** @deprecated Use [MiddinBrandColors.SecondaryLight] for accents or [MiddinBrandColors.Green] */
val MiddinSecondary = MiddinBrandColors.Green

/** @deprecated Use [MiddinBrandColors.Primary] for surface tint */
val MiddinSurfaceTint = MiddinBrandColors.Primary
