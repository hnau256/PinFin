package org.hnau.pinfin.model.transaction.edit.utils

import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial

data class EditNavigateContext(
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    val goForward: () -> Unit,
) {

    companion object {

        val root = EditNavigateContext(
            isFocused = true.toMutableStateFlowAsInitial(),
            requestFocus = {},
            goForward = {},
        )
    }
}