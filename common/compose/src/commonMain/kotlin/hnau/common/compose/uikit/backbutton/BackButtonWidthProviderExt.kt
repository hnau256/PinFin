package hnau.common.compose.uikit.backbutton

import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import hnau.common.compose.utils.AppInsets
import hnau.shuffler.annotations.Shuffle
import hnau.common.compose.uikit.Separator as UiKitSpace

@Shuffle
interface BackButtonSpaceDependencies {

    val appInsets: AppInsets
}

@Composable
fun BackButtonWidthProvider.Space(
    dependencies: BackButtonSpaceDependencies,
) {
    val width by backButtonWidth
    val startPadding = dependencies
        .appInsets
        .insets
        .calculateStartPadding(LocalLayoutDirection.current)
    UiKitSpace(size = width + startPadding)
}