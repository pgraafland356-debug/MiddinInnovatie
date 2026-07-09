package com.middin.innovatie.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Scherm-afhankelijke maten voor kleine telefoons en hoge systeem-lettergroottes.
 */
object MiddinDimens {
    /** Window width in dp from [LocalWindowInfo] (preferred over [Configuration.screenWidthDp] for Compose). */
    @Composable
    fun windowContainerWidthDp(): Int {
        val s = LocalWindowInfo.current.containerSize
        return (s.width / LocalDensity.current.density).roundToInt().coerceAtLeast(1)
    }

    /** Window height in dp from [LocalWindowInfo]. */
    @Composable
    fun windowContainerHeightDp(): Int {
        val s = LocalWindowInfo.current.containerSize
        return (s.height / LocalDensity.current.density).roundToInt().coerceAtLeast(1)
    }

    /** Horizontale padding voor hoofd-content (smallere schermen = iets minder rand). */
    @Composable
    fun screenHorizontalPadding(): Dp {
        val w = windowContainerWidthDp()
        return when {
            w < 340 -> 12.dp
            w < 400 -> 16.dp
            else -> 20.dp
        }
    }

    @Composable
    fun screenVerticalPadding(): Dp {
        val h = windowContainerHeightDp()
        val fs = LocalDensity.current.fontScale
        return when {
            fs >= 1.3f && h < 720 -> 8.dp
            else -> 12.dp
        }
    }

    /** Breedte voor product-highlight kaarten op de home (niet breder dan scherm minus padding). */
    @Composable
    fun productHighlightCardWidth(outerHorizontalPadding: Dp): Dp {
        val w = windowContainerWidthDp()
        val totalHPad = outerHorizontalPadding.value * 2
        val avail = (w - totalHPad - 24).coerceAtLeast(180f)
        return min(280f, avail).dp
    }

    /**
     * Op smalle schermen of bij grotere systeem-lettergroottes alleen pictogrammen in de bottom nav,
     * om overlapping / afkapping van labels te voorkomen.
     */
    @Composable
    fun navigationBarAlwaysShowLabels(): Boolean {
        val w = windowContainerWidthDp()
        val fs = LocalDensity.current.fontScale
        return w >= 400 && fs < 1.2f
    }

    /** Iets kleinere merk-mark in een zeer smalle top bar. */
    @Composable
    fun topBarLogoSize(): Dp {
        val w = windowContainerWidthDp()
        return when {
            w < 340 -> 28.dp
            w < 380 -> 32.dp
            else -> 40.dp
        }
    }
}
