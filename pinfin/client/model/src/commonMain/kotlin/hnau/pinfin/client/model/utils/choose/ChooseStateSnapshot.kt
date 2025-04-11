package hnau.pinfin.client.model.utils.choose

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import hnau.common.app.EditingString

data class ChooseStateSnapshot<T>(
    val visibleVariants: List<Pair<T, Boolean>>,
    val possibleVariantsToAdd: List<T>,
) {

    companion object {

        fun <T> create(
            variants: List<T>,
            selectedId: Option<String>,
            query: EditingString,
            extractId: T.() -> String,
            extractAdditionalFields: T.() -> List<String>,
            createPossibleNewVariantsByQuery: (String) -> List<T>,
        ): ChooseStateSnapshot<T> {
            val variantsWithIdsAndFields = variants.map { item ->
                Triple(
                    item,
                    item.extractId(),
                    item.extractAdditionalFields(),
                )
            }
            val ids = variantsWithIdsAndFields
                .map { it.second }
                .toSet()
            return query
                .text
                .trim()
                .takeIf(String::isNotEmpty)
                .let { nonEmptyQuery ->
                    ChooseStateSnapshot(
                        visibleVariants = when (nonEmptyQuery) {
                            null -> variantsWithIdsAndFields
                            else -> variantsWithIdsAndFields.filter { (_, id, fields) ->
                                buildList {
                                    add(id)
                                    addAll(fields)
                                }
                                    .map(String::trim)
                                    .any { field ->
                                        field.contains(
                                            other = nonEmptyQuery,
                                            ignoreCase = true,
                                        )
                                    }
                            }
                        }
                            .map { (item, id) ->
                                val isSelected = when (selectedId) {
                                    None -> false
                                    is Some<*> -> selectedId.value == id
                                }
                                item to isSelected
                            },
                        possibleVariantsToAdd = when (nonEmptyQuery) {
                            null -> emptyList()
                            else -> createPossibleNewVariantsByQuery(nonEmptyQuery)
                                .filter { it.extractId() !in ids }
                        },
                    )
                }
        }
    }
}