package hnau.common.kotlin.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

fun <T, E : T> StateFlow<T?>.isSelected(
    scope: CoroutineScope,
    element: E,
): StateFlow<Boolean> = mapState(
    scope = scope,
) { value ->
    value == element
}

fun <T, E : T> MutableStateFlow<T?>.isSelectedSeter(
    element: E,
): (Boolean) -> Unit = { isSelected ->
    update { value ->
        when (isSelected) {
            true -> element
            false -> value.takeIf { it != element }
        }
    }
}

fun <T, E : T> MutableStateFlow<T?>.isSelectedSwitcher(
    element: E,
): () -> Unit = {
    update { value ->
        element.takeIf { it != value }
    }
}
