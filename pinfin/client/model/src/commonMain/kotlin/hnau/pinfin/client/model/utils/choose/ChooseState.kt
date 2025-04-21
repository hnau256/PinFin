package hnau.pinfin.client.model.utils.choose

import arrow.core.Option
import hnau.common.app.EditingString
import hnau.common.kotlin.coroutines.combineStateWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ChooseState<T>(
    val state: StateFlow<ChooseStateSnapshot<T>>,
    val onReady: () -> Unit,
    val query: MutableStateFlow<EditingString>,
    val updateSelected: (T) -> Unit,
) {

    constructor(
        scope: CoroutineScope,
        selected: StateFlow<Option<T>>,
        updateSelected: (T) -> Unit,
        variants: StateFlow<List<T>>,
        extractId: (T) -> String,
        extractAdditionalFields: (T) -> List<String>,
        createPossibleNewVariantsByQuery: (String) -> List<T>,
        query: MutableStateFlow<EditingString>,
        onReady: () -> Unit,
    ) : this(
        updateSelected = updateSelected,
        state = variants.combineStateWith(
            scope = scope,
            other = query.combineStateWith(
                scope = scope,
                other = selected,
            ) { query, selected ->
                query to selected
            },
        ) { variants, (query, selected) ->
            ChooseStateSnapshot.create(
                variants = variants,
                query = query,
                extractId = extractId,
                extractAdditionalFields = extractAdditionalFields,
                createPossibleNewVariantsByQuery = createPossibleNewVariantsByQuery,
                selectedId = selected.map { item -> extractId(item) },
            )
        },
        onReady = onReady,
        query = query,
    )
}