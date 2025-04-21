package hnau.pinfin.client.projector.utils.choose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import hnau.pinfin.client.model.utils.choose.ChooseState

@Composable
fun <T> ChooseState<T>.Content(
    item: @Composable (value: T, selected: Boolean, onClick: () -> Unit) -> Unit,
) {
    state.collectAsState().value.Content(
        query = query,
        onReady = onReady,
        itemContent = item,
        updateSelected = updateSelected,
    )
}