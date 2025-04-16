package hnau.common.compose.uikit.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import hnau.common.compose.uikit.table.TableScope

interface TopAppBarScope : TableScope {

    @Composable
    fun Title(
        text: String,
    )

    @Composable
    fun Action(
        onClick: (() -> Unit)?,
        content: @Composable () -> Unit,
    )
}