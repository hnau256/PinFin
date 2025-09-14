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
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.transaction.utils.RecordId
import hnau.pinfin.model.transaction.utils.map
import hnau.pinfin.model.transaction.utils.remove
import hnau.pinfin.model.utils.ZipList
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pinfin.model.utils.flatMapWithScope
import hnau.pinfin.model.utils.toZipListOrNull
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordsModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    private val goForward: () -> Unit,
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
                            .flatMapWithScope(recordScope) { scope, isFocused ->
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
                        },
                        goForward = {
                            skeleton
                                .records
                                .value
                                .forward()
                                .foldNullable(
                                    ifNull = ::addNewRecord,
                                    ifNotNull = skeleton.records::value::set,
                                )
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
        .flatMapWithScope(scope) { scope, items ->
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

    private val usedCategories: StateFlow<Set<CategoryInfo>> = items
        .flatMapWithScope(scope) { itemsScope, items ->
            items.fold(
                initial = MutableStateFlow(emptySet<CategoryInfo>()).asStateFlow(),
            ) { acc, item ->
                combineState(
                    scope = itemsScope,
                    a = acc,
                    b = item.model.category.categoryEditable,
                ) { acc, categoryOrIncorrect ->
                    when (categoryOrIncorrect) {
                        Editable.Incorrect -> acc
                        is Editable.Value<CategoryInfo> -> acc + categoryOrIncorrect.value
                    }
                }
            }
        }

    private fun addNewRecord() {
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
        addNewRecord = ::addNewRecord,
    )

    internal val records: StateFlow<Editable<NonEmptyList<TransactionInfo.Type.Entry.Record>>> =
        items.flatMapWithScope(scope) { scope, idWithRecords ->

            val records = idWithRecords
                .toNonEmptyList()
                .map(Item::model)

            records
                .head
                .record
                .mapState(scope) { recordOrIncorrect ->
                    recordOrIncorrect.map(::nonEmptyListOf)
                }
                .add(
                    scope = scope,
                    remaining = records.tail,
                )
        }

    private fun StateFlow<Editable<NonEmptyList<TransactionInfo.Type.Entry.Record>>>.add(
        scope: CoroutineScope,
        remaining: List<RecordModel>,
    ): StateFlow<Editable<NonEmptyList<TransactionInfo.Type.Entry.Record>>> = remaining
        .toNonEmptyListOrNull()
        .foldNullable(
            ifNull = { this },
            ifNotNull = { nonEmptyRemaining ->
                flatMapWithScope(scope) { recordsScope, recordsOrIncorrect ->
                        when (recordsOrIncorrect) {
                            Editable.Incorrect -> Editable.Incorrect.toMutableStateFlowAsInitial()
                            is Editable.Value<NonEmptyList<TransactionInfo.Type.Entry.Record>> -> nonEmptyRemaining
                                .head
                                .record
                                .mapState(recordsScope) { headRecordOrNull ->
                                    headRecordOrNull.map { headRecord ->
                                        recordsOrIncorrect.value + headRecord
                                    }
                                }
                                .add(
                                    scope = recordsScope,
                                    remaining = nonEmptyRemaining.tail,
                                )
                        }
                    }
            }
        )

    val goBackHandler: GoBackHandler = items.flatMapWithScope(scope) { itemsScope, items ->
            items
                .selected
                .model
                .goBackHandler
                .flatMapWithScope(itemsScope) { recordGoBackScope, recordGoBackOrNull ->
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