package hnau.common.compose.uikit.progressindicator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.Separator
import hnau.common.compose.uikit.utils.Dimens

@Composable
fun ProgressIndicatorPanel(
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) = Column(
    modifier = modifier
        .fillMaxSize()
        .clickable { }
        .padding(Dimens.separation),
    verticalArrangement = Arrangement.spacedBy(
        space = Dimens.smallSeparation,
        alignment = Alignment.CenterVertically,
    ),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    CircularProgressIndicator()
    content?.let { contentNotNull ->
        Separator()
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
        ) {
            contentNotNull()
        }
    }
}
