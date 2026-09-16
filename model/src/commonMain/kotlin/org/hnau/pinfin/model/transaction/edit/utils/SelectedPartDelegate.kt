package org.hnau.pinfin.model.transaction.edit.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.foldNullable
import kotlin.enums.enumEntries

internal class SelectedPartDelegate<P : Enum<P>> @PublishedApi internal constructor(
    private val scope: CoroutineScope,
    private val entries: List<P>,
    private val selectedPart: StateFlow<P>,
    private val onPartChanged: (P) -> Unit,
    private val navigateContext: EditNavigateContext,
) {

    fun createPartNavigateContext(
        part: P,
    ): EditNavigateContext = EditNavigateContext(
        isFocused = derivedStateFlowOf(scope) {
            navigateContext.isFocused.state && selectedPart.state == part
        },
        requestFocus = {
            onPartChanged(part)
            navigateContext.requestFocus()
        },
        goForward = entries
            .getOrNull(part.ordinal + 1)
            .foldNullable(
                ifNull = { navigateContext.goForward },
                ifNotNull = { nextPart ->
                    { onPartChanged(nextPart) }
                },
            )
    )

    companion object {

        inline fun <reified P : Enum<P>> create(
            scope: CoroutineScope,
            selectedPart: StateFlow<P>,
            noinline onPartChanged: (P) -> Unit,
            navigateContext: EditNavigateContext,
        ): SelectedPartDelegate<P> = SelectedPartDelegate(
            scope = scope,
            entries = enumEntries<P>(),
            selectedPart = selectedPart,
            onPartChanged = onPartChanged,
            navigateContext = navigateContext,
        )
    }
}