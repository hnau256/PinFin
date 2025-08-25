package hnau.pinfin.projector.utils.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.pinfin.model.utils.choose.ChooseState
import hnau.pinfin.projector.transaction.utils.ChooseOrCreateMessages

@Composable
fun <T> ChooseState<T>.Content(
    messages: ChooseOrCreateMessages,
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