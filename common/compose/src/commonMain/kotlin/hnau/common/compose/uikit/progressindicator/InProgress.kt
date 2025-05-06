package hnau.common.compose.uikit.progressindicator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import hnau.common.compose.uikit.state.BooleanStateContent
import hnau.common.compose.uikit.state.TransitionSpec
import hnau.common.kotlin.foldBoolean
import kotlinx.coroutines.flow.StateFlow

@Composable
fun InProgress(
    inProgress: StateFlow<Boolean>,
    fillMaxSize: Boolean = true,
) {
    inProgress
        .collectAsState()
        .value
        .BooleanStateContent(
            modifier = fillMaxSize.foldBoolean(
                ifTrue = { Modifier.fillMaxSize() },
                ifFalse = { Modifier },
            ),
            transitionSpec = TransitionSpec.crossfade(),
            label = "InProgress",
        ) {
            fillMaxSize.foldBoolean(
                ifTrue = { ProgressIndicatorInBox() },
                ifFalse = { CircularProgressIndicator() }
            )
        }
}