@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.page.type.entry

import arrow.core.NonEmptyList
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.page.type.entry.record.RecordPageModel
import hnau.pinfin.model.transaction.part.type.entry.record.RecordId
import hnau.pinfin.model.transaction.part.type.entry.record.RecordInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordsPageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    all: StateFlow<NonEmptyList<Pair<RecordId, RecordInfo>>>,
    val addNew: () -> Unit,
) : EntryPagePageModel {

    @Pipe
    interface Dependencies {

        fun record(): RecordPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val selected: MutableStateFlow<RecordId?> = null.toMutableStateFlowAsInitial(),
        var records: Map<RecordId, RecordPageModel.Skeleton> = emptyMap(),
    )

    data class Tab(
        val id: RecordId,
        val info: RecordInfo,
        val selectIfNotSelected: StateFlow<(() -> Unit)?>,
    )

    val tabs: StateFlow<NonEmptyList<Tab>> = all.mapWithScope(scope) { allScope, all ->
        all.map { (id, info) ->
            Tab(
                id = id,
                info = info,
                selectIfNotSelected = skeleton
                    .selected
                    .mapState(allScope) { selected ->
                        (id == selected).foldBoolean(
                            ifTrue = { null },
                            ifFalse = { { skeleton.selected.value = id } }
                        )
                    }
            )
        }
    }

    data class Record(
        val index: Int,
        val id: RecordId,
        val model: RecordPageModel,
    )

    val record: StateFlow<Record> = combineState(
        scope = scope,
        a = skeleton.selected,
        b = all,
    ) { selectedOrNull, records ->
        selectedOrNull to records
    }.mapReusable<_, RecordId, RecordPageModel, _>(
        scope = scope,
    ) { (selectedIdOrNull, records) ->

        val allIds = records
            .map(Pair<RecordId, *>::first)
            .toSet()

        val recordsSkeletons = skeleton
            .records
            .filter { it.key in allIds }
            .also { clearedRecordsSkeletons ->
                skeleton.records = clearedRecordsSkeletons
            }

        val (index, selectedId, info) = selectedIdOrNull.foldNullable(
            ifNull = {
                val (selectedId, info) = records.head
                Triple(
                    0,
                    selectedId,
                    info
                )
            },
            ifNotNull = { selectedId ->
                val (index, idWithInfo) = records
                    .asSequence()
                    .withIndex()
                    .first { (_, idWithInfo) ->
                        val (id) = idWithInfo
                        id == selectedId
                    }
                val (_, info) = idWithInfo
                Triple(
                    index,
                    selectedId,
                    info,
                )
            }
        )

        val recordModel = getOrPutItem(
            key = selectedId,
        ) { recordScope ->
            RecordPageModel(
                scope = recordScope,
                dependencies = dependencies.record(),
                skeleton = run {
                    var result = recordsSkeletons[selectedId]
                    if (result == null) {
                        result = RecordPageModel.Skeleton()
                        skeleton.records = recordsSkeletons + (selectedId to result)
                    }
                    result
                },
                info = info,
            )
        }

        Record(
            index = index,
            id = selectedId,
            model = recordModel,
        )
    }

    override val goBackHandler: GoBackHandler = record
        .scopedInState(scope)
        .flatMapState(scope) { (recordScope, record) ->
            val (index, _, model) = record
            model
                .goBackHandler
                .scopedInState(recordScope)
                .flatMapState(recordScope) { (selectedGoBackScope, selectedGoBack) ->
                    selectedGoBack.foldNullable(
                        ifNotNull = { it.toMutableStateFlowAsInitial() },
                        ifNull = {
                            all.mapState(selectedGoBackScope) { all ->
                                index
                                    .minus(1)
                                    .takeIf { it >= 0 }
                                    ?.let(all::getOrNull)
                                    ?.first
                                    ?.let { newId ->
                                        { skeleton.selected.value = newId }
                                    }
                            }
                        }
                    )
                }
        }
}