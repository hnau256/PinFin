package hnau.common.compose.uikit.backbutton

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hnau.common.compose.uikit.utils.Dimens

object BackButtonConstants {

    val startSeparation: Dp
        get() = Dimens.separation

    val size: Dp
        get() = 56.dp

    val maxWidth: Dp
        get() = startSeparation + size
}