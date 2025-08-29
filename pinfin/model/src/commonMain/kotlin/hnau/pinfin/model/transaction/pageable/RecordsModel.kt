@file:UseSerializers(
    MutableStateFlowSerializer::class,
    NonEmptyListSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.serialization.NonEmptyListSerializer
import arrow.core.toNonEmptyListOrNull
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.Amount
import hnau.pinfin.model.transaction.utils.RecordId
import hnau.pinfin.model.transaction.utils.remove
import hnau.pinfin.model.utils.ZipList
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pinfin.model.utils.toZipListOrNull
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

class RecordsModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun record(): RecordModel.Dependencies

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: Page.Skeleton? = null,
        val records: MutableStateFlow<ZipList<Pair<RecordId, RecordModel.Skeleton>>>,
    ) {

        companion object {

            fun createForNew(): Skeleton = createInner(
                records = nonEmptyListOf(
                    RecordModel.Skeleton.createForNew(),
                )
            )

            fun createForEdit(
                records: NonEmptyList<TransactionInfo.Type.Entry.Record>
            ): Skeleton = createInner(
                records = records.map(RecordModel.Skeleton.Companion::createForEdit),
            )

            private fun createInner(
                records: NonEmptyList<RecordModel.Skeleton>,
            ): Skeleton = Skeleton(
                records = records
                    .map { record ->
                        val id = RecordId.createNew()
                        id to record
                    }
                    .let { records ->
                        ZipList(
                            before = records.dropLast(1),
                            selected = records.last(),
                            after = emptyList(),
                        )
                    }
                    .toMutableStateFlowAsInitial()
            )
        }
    }

    private fun selectRecord(
        id: RecordId,
    ) {
        skeleton
            .records
            .update { records ->
                records
                    .toZipListOrNull { it.first == id }
                    ?: return //TODO log error
            }
    }

    data class Item(
        val id: RecordId,
        val model: RecordModel,
    )

    val items: StateFlow<ZipList<Item>> = skeleton
        .records
        .mapReusable<_, RecordId, Item, _>(
            scope = scope,
        ) { records ->

            records.mapFull { _, _, (id, recordSkeleton) ->
                getOrPutItem(id) { recordScope ->

                    val model = RecordModel(
                        scope = recordScope,
                        dependencies = dependencies.record(),
                        skeleton = recordSkeleton,
                        remove = skeleton
                            .records
                            .mapState(recordScope) { records ->
                                records
                                    .remove { it.first == id }
                                    ?.let { recordsWithoutCurrent ->
                                        { skeleton.records.value = recordsWithoutCurrent }
                                    }
                            },
                        isFocused = isFocused
                            .scopedInState(recordScope)
                            .flatMapState(recordScope) { (scope, isFocused) ->
                                isFocused.foldBoolean(
                                    ifFalse = { false.toMutableStateFlowAsInitial() },
                                    ifTrue = {
                                        skeleton
                                            .records
                                            .mapState(scope) { records ->
                                                records.selected.first == id
                                            }
                                    }
                                )
                            },
                        requestFocus = {
                            selectRecord(id)
                            requestFocus()
                        }
                    )

                    Item(
                        id = id,
                        model = model,
                    )
                }
            }
        }

    val amountOrZero: StateFlow<Amount> = items
        .scopedInState(scope)
        .flatMapState(scope) { (scope, items) ->
            val nonEmptyItems = items.toNonEmptyList()
            nonEmptyItems
                .tail
                .fold<_, StateFlow<Amount>>(
                    initial = nonEmptyItems.head.model.amountOrZero,
                ) { acc, item ->
                    acc.combineStateWith(
                        scope = scope,
                        other = item.model.amountOrZero,
                    ) { previous, amountOrZero ->
                        previous + amountOrZero
                    }
                }
        }

    val categories: StateFlow<List<CategoryInfo>> = items
        .scopedInState(scope)
        .flatMapState(scope) { (scope, items) ->
            items
                .fold<_, StateFlow<List<CategoryInfo>>>(
                    initial = MutableStateFlow(emptyList()),
                ) { acc, item ->
                    acc.combineStateWith(
                        scope = scope,
                        other = item.model.category.category,
                    ) { previous, categoryOrNull ->
                        categoryOrNull.foldNullable(
                            ifNull = { previous },
                            ifNotNull = { category -> previous + category },
                        )
                    }
                }
                .mapState(scope, List<CategoryInfo>::distinct)
        }

    private val usedCategories: StateFlow<Set<CategoryInfo>> = items
        .scopedInState(scope)
        .flatMapState(scope) { (itemsScope, items) ->
            items.fold(
                initial = MutableStateFlow(emptySet<CategoryInfo>()).asStateFlow(),
            ) { acc, item ->
                combineState(
                    scope = itemsScope,
                    a = acc,
                    b = item.model.category.category,
                ) { acc, categoryOrNull ->
                    categoryOrNull.foldNullable(
                        ifNull = { acc },
                        ifNotNull = { category -> acc + category }
                    )
                }
            }
        }

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val items: StateFlow<ZipList<Item>>,
        val currentRecord: StateFlow<Triple<Int, RecordId, RecordModel.Page>>,
        val addNewRecord: () -> Unit,
    ) {

        @Pipe
        interface Dependencies

        @Serializable
        /*data*/ class Skeleton

        val goBackHandler: GoBackHandler =
            currentRecord.flatMapState(scope) { it.third.goBackHandler }
    }

    fun createPage(
        scope: CoroutineScope,
    ): Page = Page(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { Page.Skeleton() },
        items = items,
        currentRecord = items
            .mapReusable(
                scope = scope,
            ) { items ->
                val selected = items.selected
                val id = selected.id
                val page = getOrPutItem(id) { pageScope ->
                    selected.model.createPage(
                        scope = pageScope,
                        usedCategories = usedCategories,
                    )
                }
                val index = items.before.size
                Triple(index, id, page)
            },
        addNewRecord = {
            skeleton.records.update { records ->
                ZipList(
                    before = records,
                    selected = Pair(
                        first = RecordId.createNew(),
                        second = RecordModel.Skeleton.createForNew(),
                    ),
                    after = emptyList(),
                )
            }
        }
    )

    val records: StateFlow<NonEmptyList<TransactionInfo.Type.Entry.Record>?> = items
        .scopedInState(scope)
        .flatMapState(scope) { (recordsScope, idWithRecords) ->

            val records = idWithRecords
                .toNonEmptyList()
                .map(Item::model)

            records
                .head
                .record
                .mapState(recordsScope) { recordOrNull ->
                    recordOrNull?.let { record -> nonEmptyListOf(record) }
                }
                .add(
                    scope = recordsScope,
                    remaining = records.tail,
                )
        }

    private fun StateFlow<NonEmptyList<TransactionInfo.Type.Entry.Record>?>.add(
        scope: CoroutineScope,
        remaining: List<RecordModel>,
    ): StateFlow<NonEmptyList<TransactionInfo.Type.Entry.Record>?> = remaining
        .toNonEmptyListOrNull()
        .foldNullable(
            ifNull = { this },
            ifNotNull = { nonEmptyRemaining ->
                this
                    .scopedInState(scope)
                    .flatMapState(scope) { (recordsScope, recordsOrNull) ->
                        recordsOrNull.foldNullable(
                            ifNull = { null.toMutableStateFlowAsInitial() },
                            ifNotNull = { records ->
                                nonEmptyRemaining
                                    .head
                                    .record
                                    .mapState(recordsScope) { headRecordOrNull ->
                                        headRecordOrNull.foldNullable(
                                            ifNull = { null },
                                            ifNotNull = { headRecord ->
                                                records + headRecord
                                            }
                                        )
                                    }
                                    .add(
                                        scope = recordsScope,
                                        remaining = nonEmptyRemaining.tail,
                                    )
                            }
                        )
                    }
            }
        )

    val goBackHandler: GoBackHandler = items
        .scopedInState(scope)
        .flatMapState(scope) { (itemsScope, items) ->
            items
                .selected
                .model
                .goBackHandler
                .scopedInState(itemsScope)
                .flatMapState(itemsScope) { (recordGoBackScope, recordGoBackOrNull) ->
                    recordGoBackOrNull.foldNullable(
                        ifNotNull = { it.toMutableStateFlowAsInitial() },
                        ifNull = {
                            skeleton
                                .records
                                .mapState(recordGoBackScope) { records ->
                                    records
                                        .back()
                                        ?.let { newRecords ->
                                            { skeleton.records.value = newRecords }
                                        }
                                }
                        }
                    )
                }
        }
}