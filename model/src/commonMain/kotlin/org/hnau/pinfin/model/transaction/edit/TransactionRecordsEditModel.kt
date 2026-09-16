@file:UseSerializers(
    NonEmptyListSerializer::class,
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.serialization.NonEmptyListSerializer
import arrow.core.toNonEmptyListOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapNonEmptyListReusable
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.model.transaction.utils.RecordId
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class TransactionRecordsEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        fun record(): TransactionRecordEditModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val records: MutableStateFlow<NonEmptyList<KeyValue<RecordId, TransactionRecordEditModel.Skeleton>>>,
    ) {

        companion object {

            fun createForNew(): Skeleton = create(
                records = nonEmptyListOf(
                    TransactionRecordEditModel.Skeleton.createForNew()
                )
            )

            fun create(
                records: NonEmptyList<Record<KeyValue<CategoryId, CategoryInfo>>>,
            ): Skeleton = create(
                records = records.map(TransactionRecordEditModel.Skeleton::create)
            )

            private fun create(
                records: NonEmptyList<TransactionRecordEditModel.Skeleton>,
            ): Skeleton = Skeleton(
                records = records.map { record ->
                    KeyValue(
                        key = RecordId.createNew(),
                        value = record,
                    )
                }.toMutableStateFlowAsInitial()
            )
        }
    }

    val records: StateFlow<NonEmptyList<TransactionRecordEditModel>> =
        skeleton.records.mapNonEmptyListReusable(
            scope = scope,
            extractKey = { idWithValue -> idWithValue.key },
            transform = { scope, (id, recordSkeleton) ->
                TransactionRecordEditModel(
                    scope = scope,
                    dependencies = dependencies.record(),
                    skeleton = recordSkeleton,
                    remove = skeleton.records.mapState(scope) { records ->
                        records
                            .filter { it.key != id }
                            .toNonEmptyListOrNull()
                            ?.let { newRecords ->
                                { skeleton.records.value = newRecords }
                            }
                    }
                )
            }
        )

    val recordsEditable: StateFlow<Editable<NonEmptyList<Record<KeyValue<CategoryId, CategoryInfo>>>>> =
        derivedStateFlowOf(scope) {
            editable {
                records.state.map { record ->
                    record.recordEditable.state.bind()
                }
            }
        }

    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        records
            .state
            .last()
            .goBackHandler
            .state
    }
}