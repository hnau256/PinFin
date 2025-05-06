package hnau.common.compose.uikit.progressindicator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ProgressIndicatorInBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
            )
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = {},
            ),
        contentAlignment = contentAlignment,
    ) {
        CircularProgressIndicator()
    }
}