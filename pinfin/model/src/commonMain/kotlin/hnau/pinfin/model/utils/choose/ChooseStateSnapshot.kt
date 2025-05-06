package hnau.pinfin.model.utils.choose

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.EditingString
import hnau.common.kotlin.foldNullable

data class ChooseStateSnapshot<out T>(
    val visibleVariants: VisibleVariants<T>,
    val possibleVariantsToAdd: NonEmptyList<T>?,
) {

    sealed interface VisibleVariants<out T> {

        data object Empty : VisibleVariants<Nothing>

        data object InputToCreateNewMessage : VisibleVariants<Nothing>

        data object NotFound : VisibleVariants<Nothing>

        data class List<out T>(
            val list: NonEmptyList<Pair<T, Boolean>>,
        ) : VisibleVariants<T>
    }

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
            val nonEmptyQuery = query
                .text
                .trim()
                .takeIf(String::isNotEmpty)
            val possibleVariantsToAdd: NonEmptyList<T>? = when (nonEmptyQuery) {
                null -> null
                else -> createPossibleNewVariantsByQuery(nonEmptyQuery)
                    .filter { it.extractId() !in ids }
                    .toNonEmptyListOrNull()
            }
            return ChooseStateSnapshot(
                visibleVariants = variantsWithIdsAndFields
                    .toNonEmptyListOrNull()
                    .foldNullable(
                        ifNull = {
                            possibleVariantsToAdd.foldNullable(
                                ifNull = { VisibleVariants.InputToCreateNewMessage },
                                ifNotNull = { VisibleVariants.Empty },
                            )
                        },
                        ifNotNull = { variants ->
                            nonEmptyQuery
                                .foldNullable(
                                    ifNull = { variants },
                                    ifNotNull = { query ->
                                        variants.filter { (_, id, fields) ->
                                            buildList {
                                                add(id)
                                                addAll(fields)
                                            }
                                                .map(String::trim)
                                                .any { field ->
                                                    field.contains(
                                                        other = query,
                                                        ignoreCase = true,
                                                    )
                                                }
                                        }
                                    }
                                )
                                .map { (item, id) ->
                                    val isSelected = when (selectedId) {
                                        None -> false
                                        is Some<*> -> selectedId.value == id
                                    }
                                    item to isSelected
                                }
                                .toNonEmptyListOrNull()
                                .foldNullable(
                                    ifNull = { VisibleVariants.NotFound },
                                    ifNotNull = { VisibleVariants.List(it) }
                                )
                        }
                    ),
                possibleVariantsToAdd = possibleVariantsToAdd,
            )
        }
    }
}