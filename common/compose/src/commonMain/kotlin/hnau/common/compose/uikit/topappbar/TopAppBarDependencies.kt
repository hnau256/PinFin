package hnau.common.compose.uikit.topappbar

import hnau.common.compose.uikit.backbutton.BackButtonSpaceDependencies
import hnau.common.compose.uikit.backbutton.BackButtonWidthProvider
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface TopAppBarDependencies {

    val backButtonWidthProvider: BackButtonWidthProvider

    fun backButtonSpaceDependencies(): BackButtonSpaceDependencies
}