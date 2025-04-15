package hnau.common.compose.uikit.topappbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import hnau.common.compose.uikit.backbutton.BackButtonWidthProvider
import hnau.common.compose.uikit.backbutton.Space
import hnau.common.compose.uikit.utils.Dimens

@Composable
fun TopAppBar(
    dependencies: TopAppBarDependencies,
    padding: PaddingValues,
    content: @Composable TopAppBarScope.() -> Unit,
) {
    val buttonWidth by dependencies.backButtonWidthProvider.backButtonWidth
    val startContentPadding = remember(buttonWidth) {
        lerp(
            start = Dimens.separation,
            stop = 0.dp,
            fraction = buttonWidth / BackButtonWidthProvider.maxBackButtonSize,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f))
            .padding(padding)
            .padding(
                end = Dimens.separation,
                start = startContentPadding,
            )
            .height(Dimens.rowHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.separation),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.primary,
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
        ) {
            dependencies.backButtonWidthProvider.Space(
                dependencies = remember(dependencies) { dependencies.backButtonSpaceDependencies() },
            )
            val scope: TopAppBarScope = remember(this) {
                TopAppBarScopeImpl(this)
            }
            scope.content()
        }
    }
}