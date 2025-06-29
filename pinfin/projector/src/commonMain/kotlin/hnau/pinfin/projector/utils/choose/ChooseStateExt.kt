package hnau.pinfin.projector.utils.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.pinfin.model.utils.choose.ChooseState

@Composable
fun <T> ChooseState<T>.Content(
    messages: ChooseMessages,
    item: @Composable (value: T, selected: Boolean, onClick: () -> Unit) -> Unit,
) {
    state.collectAsState().value.Content(
        query = query,
        onReady = onReady,
        itemContent = item,
        messages = messages,
        updateSelected = updateSelected,
    )
}