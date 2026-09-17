@file:UseSerializers(
    NonEmptyListSerializer::class,
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.serialization.NonEmptyListSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.ZipList
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapZipListReusable
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.kotlin.toZipListOrNull
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import org.hnau.pinfin.model.transaction.utils.RecordId
import org.hnau.pinfin.model.transaction.utils.remove
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class TransactionRecordsEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    navigateContext: EditNavigateContext,
) {

    @Pipe
    interface Dependencies {

        fun record(): TransactionRecordEditModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val records: MutableStateFlow<ZipList<KeyValue<RecordId, TransactionRecordEditModel.Skeleton>>>,
    ) {

        companion object {

            fun createForNew(): Skeleton = createFromSkeletons(
                records = nonEmptyListOf(
                    TransactionRecordEditModel.Skeleton.createForNew()
                )
            )

            fun create(
                records: NonEmptyList<Record<KeyValue<CategoryId, CategoryInfo>>>,
            ): Skeleton = createFromSkeletons(
                records = records.map(TransactionRecordEditModel.Skeleton::create)
            )

            private fun createFromSkeletons(
                records: NonEmptyList<TransactionRecordEditModel.Skeleton>,
            ): Skeleton = Skeleton(
                records = records
                    .map { record ->
                        KeyValue(
                            key = RecordId.createNew(),
                            value = record,
                        )
                    }
                    .let { records ->
                        ZipList(
                            before = records.dropLast(1),
                            selected = records.last(),
                            after = emptyList(),
                        )
                    }
                    .toMutableStateFlowAsInitial(),
            )
        }
    }

    val records: StateFlow<ZipList<KeyValue<RecordId, TransactionRecordEditModel>>> =
        skeleton.records.mapZipListReusable(
            scope = scope,
            extractKey = { idWithValue -> idWithValue.key },
            transform = { scope, (id, recordSkeleton) ->
                KeyValue(
                    key = id,
                    value = TransactionRecordEditModel(
                        scope = scope,
                        dependencies = dependencies.record(),
                        skeleton = recordSkeleton,
                        remove = skeleton.records.mapState(scope) { records ->
                            records.remove { it.key == id }
                                ?.let { newRecords ->
                                    { skeleton.records.value = newRecords }
                                }
                        },
                        navigateContext = EditNavigateContext(
                            isFocused = derivedStateFlowOf(scope) {
                                navigateContext.isFocused.state && skeleton.records.state.selected.key == id
                            },
                            requestFocus = {
                                skeleton
                                    .records
                                    .update { records ->
                                        records
                                            .toZipListOrNull { it.key == id }
                                            ?: return@EditNavigateContext //TODO log error
                                    }
                                navigateContext.requestFocus()
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
                    ),
                )
            }
        )

    private fun addNewRecord() {
        skeleton.records.update { records ->
            ZipList(
                before = records,
                selected = KeyValue(
                    key = RecordId.createNew(),
                    value = TransactionRecordEditModel.Skeleton.createForNew(),
                ),
                after = emptyList(),
            )
        }
    }

    val recordsEditable: StateFlow<Editable<NonEmptyList<Record<KeyValue<CategoryId, CategoryInfo>>>>> =
        derivedStateFlowOf(scope) {
            editable {
                records.state.map { idWithRecord ->
                    idWithRecord.value.recordEditable.state.bind()
                }.toNonEmptyList()
            }
        }

    val goBackHandler: GoBackHandler = derivedStateFlowOf(scope) {
        records
            .state
            .selected
            .value
            .goBackHandler
            .state
            .ifNull {
                skeleton
                    .records
                    .state
                    .back()
                    ?.let { newRecords ->
                        { skeleton.records.value = newRecords }
                    }
            }
    }
}