package hnau.common.compose.uikit.topappbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.lerp
import hnau.common.compose.uikit.backbutton.BackButtonConstants
import hnau.common.compose.uikit.backbutton.Space
import hnau.common.compose.uikit.table.Table
import hnau.common.compose.uikit.table.TableOrientation
import hnau.common.compose.uikit.table.TableScope
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.horizontalDisplayPadding

@Composable
fun TopAppBar(
    dependencies: TopAppBarDependencies,
    padding: PaddingValues,
    content: @Composable TopAppBarScope.() -> Unit,
) {
    val buttonWidthFraction by dependencies.backButtonWidthProvider.backButtonWidthFraction
    val startSeparation = lerp(
        start = Dimens.horizontalDisplayPadding,
        stop = Dimens.smallSeparation,
        fraction = buttonWidthFraction.fraction,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
            .padding(
                start = startSeparation,
                end = Dimens.horizontalDisplayPadding,
            )
            .height(BackButtonConstants.size),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.primary,
            LocalTextStyle provides MaterialTheme.typography.titleMedium,
        ) {
            dependencies.backButtonWidthProvider.Space(
                dependencies = remember(dependencies) { dependencies.backButtonSpaceDependencies() },
            )
            Table(
                orientation = TableOrientation.Horizontal,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                val tableScope: TableScope = this
                val scope = remember(tableScope) {
                    TopAppBarScopeImpl(
                        parent = tableScope,
                    )
                }
                scope.content()
            }
        }
    }
}