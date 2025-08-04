package hnau.pinfin.model.transaction.utils

import kotlinx.coroutines.flow.StateFlow

data class NavAction(
    val type: Type,
    val onClick: StateFlow<(suspend () -> Unit)?>,
) {

    enum class Type {
        Next, Done,
    }
}