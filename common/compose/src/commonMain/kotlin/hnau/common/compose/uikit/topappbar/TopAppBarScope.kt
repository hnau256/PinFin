package hnau.common.compose.uikit.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

interface TopAppBarScope : RowScope {

    @Composable
    fun Title(
        text: String,
    )

    @Composable
    fun Action(
        onClick: (() -> Unit)?,
        content: @Composable RowScope.() -> Unit,
    )
}