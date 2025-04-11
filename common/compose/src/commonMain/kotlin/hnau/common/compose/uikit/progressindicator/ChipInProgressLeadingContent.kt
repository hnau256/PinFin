package hnau.common.compose.uikit.progressindicator

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun chipInProgressLeadingContent(
    inProgress: Boolean,
): (@Composable () -> Unit)? = when (inProgress) {
    false -> null
    true -> {
        { ChipInProgressLeadingContent() }
    }
}

@Composable
fun ChipInProgressLeadingContent() = CircularProgressIndicator(
    modifier = Modifier.size(32.dp),
    color = LocalContentColor.current,
)
