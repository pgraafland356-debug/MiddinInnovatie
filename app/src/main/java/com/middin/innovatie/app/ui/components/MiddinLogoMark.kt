package com.middin.innovatie.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.middin.innovatie.app.R

/**
 * In-app Middin wordmark logo. Uses [R.drawable.middin_wordmark] (PNG in `res/drawable-nodpi`) with no
 * tinting or color filters so pixels match the source file exactly.
 */
@Composable
fun MiddinLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val desc = stringResource(R.string.logo_content_description)
    Image(
        painter = painterResource(R.drawable.middin_wordmark),
        contentDescription = desc,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size),
    )
}
