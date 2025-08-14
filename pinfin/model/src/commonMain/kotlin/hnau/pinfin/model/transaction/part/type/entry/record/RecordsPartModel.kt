@file:UseSerializers(
    NonEmptyListSerializer::class,
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part.type.entry.record

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.coroutines.mapNonEmptyListReusable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction.page.type.entry.RecordsPageModel
import hnau.pinfin.model.transaction.page.type.entry.EntryPagePageModel
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class RecordsPartModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val requestFocus: () -> Unit,
    val isFocused: StateFlow<Boolean>,
) {

    @Pipe
    interface Dependencies {

        fun record(): RecordModel.Dependencies

        fun page(): RecordsPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val records: MutableStateFlow<NonEmptyList<Pair<RecordId, RecordModel.Skeleton>>>,
        var page: RecordsPageModel.Skeleton? = null,
    ) {

        companion object {

            fun createForNew(): Skeleton = create(
                records = nonEmptyListOf(RecordModel.Skeleton.createForNew()),
            )

            fun createForEdit(
                records: NonEmptyList<TransactionInfo.Type.Entry.Record>,
            ): Skeleton = create(
                records = records
                    .map(RecordModel.Skeleton::createForEdit)
            )

            private fun create(
                records: NonEmptyList<RecordModel.Skeleton>,
            ): Skeleton = Skeleton(
                records = records
                    .map { record -> RecordId.new() to record }
                    .toMutableStateFlowAsInitial()
            )
        }
    }

    val records: StateFlow<NonEmptyList<Pair<RecordId, RecordModel>>> = skeleton
        .records
        .mapNonEmptyListReusable(
            scope = scope,
            extractKey = Pair<RecordId, *>::first,
        ) { recordScope, (id, skeleton) ->
            val model = RecordModel(
                scope = recordScope,
                skeleton = skeleton,
                dependencies = dependencies.record(),
            )
            id to model
        }

    fun createPage(
        scope: CoroutineScope,
    ): EntryPagePageModel = RecordsPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { RecordsPageModel.Skeleton() },
        all = records.mapState(scope) { records ->
            records.map { (id, model) ->
                id to model.info
            }
        },
        addNew = {
            skeleton.records.update { records ->
                val id = RecordId.new()
                val skeleton = RecordModel.Skeleton.createForNew()
                records + (id to skeleton)
            }
        },
    )
}