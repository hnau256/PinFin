package hnau.common.compose.uikit.backbutton

import androidx.compose.runtime.State

interface BackButtonWidthProvider {

    val backButtonWidthFraction: State<BackButtonWidthFraction>
}