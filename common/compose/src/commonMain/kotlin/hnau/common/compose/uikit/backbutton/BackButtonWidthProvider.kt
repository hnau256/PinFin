package hnau.common.compose.uikit.backbutton

import androidx.compose.runtime.State
import androidx.compose.ui.unit.Dp
import hnau.common.compose.uikit.utils.Dimens

interface BackButtonWidthProvider {

    val backButtonWidth: State<Dp>

    companion object {

        val maxBackButtonSize: Dp
            get() = Dimens.rowHeight
    }
}