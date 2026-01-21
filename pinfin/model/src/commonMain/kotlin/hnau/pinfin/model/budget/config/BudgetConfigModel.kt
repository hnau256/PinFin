@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.budget.config

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.flow.state.flatMapWithScope
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.model.budgetstack.BudgetStackOpener
import hnau.pinfin.model.manage.BudgetsListOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class BudgetConfigModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val budgetsListOpener: BudgetsListOpener

        val repository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener
    }

    @Serializable
    data class Skeleton(
        val editName: MutableStateFlow<MutableStateFlow<EditingString>?> =
            null.toMutableStateFlowAsInitial(),
        val removeDialogVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    fun openCategories() {
        dependencies
            .budgetStackOpener
            .openCategories()
    }

    val removeDialogVisible: MutableStateFlow<Boolean>
        get() = skeleton.removeDialogVisible

    private val inProgressRegistry = InProgressRegistry(
        scope = scope,
    )

    val inProgress: StateFlow<Boolean>
        get() = inProgressRegistry.inProgress

    fun removeClick() {
        skeleton.removeDialogVisible.value = true
    }

    fun removeConfirm() {
        removeCancel()
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies
                    .repository
                    .remove()
            }
        }
    }

    fun removeCancel() {
        skeleton.removeDialogVisible.value = false
    }

    fun openBudgetsList() {
        scope.launch {
            inProgressRegistry.executeRegistered {
                dependencies.budgetsListOpener.openBudgetsList()
            }
        }
    }

    sealed interface NameOrEdit {

        data class Name(
            val name: String,
            val edit: () -> Unit,
        ) : NameOrEdit

        data class Edit(
            val input: MutableStateFlow<EditingString>,
            val save: StateFlow<(() -> Unit)?>,
            val cancel: () -> Unit,
        ) : NameOrEdit
    }

    val nameOrEdit: StateFlow<NameOrEdit> = skeleton
        .editName
        .flatMapWithScope(scope) { scope, editNameSkeletonOrNull ->
            editNameSkeletonOrNull.foldNullable(
                ifNull = {
                    dependencies
                        .repository
                        .state
                        .mapState(scope) {
                            NameOrEdit.Name(
                                name = it.info.title,
                                edit = {
                                    skeleton.editName.value = dependencies
                                        .repository
                                        .state
                                        .value
                                        .info
                                        .title
                                        .toEditingString()
                                        .toMutableStateFlowAsInitial()
                                }
                            )
                        }

                },
                ifNotNull = { nameEditStringState ->
                    val cancel = { skeleton.editName.value = null }
                    NameOrEdit
                        .Edit(
                            input = nameEditStringState,
                            save = actionOrNullIfExecuting(scope) {
                                inProgressRegistry.executeRegistered {
                                    dependencies
                                        .repository
                                        .config(
                                            config = BudgetConfig(
                                                title = nameEditStringState.value.text.trim()
                                            )
                                        )
                                    cancel()
                                }
                            },
                            cancel = cancel,
                        )
                        .toMutableStateFlowAsInitial()
                }
            )
        }

    val goBackHandler: GoBackHandler = nameOrEdit.mapState(scope) { nameOrEditModel ->
        when (nameOrEditModel) {
            is NameOrEdit.Edit -> nameOrEditModel.cancel
            is NameOrEdit.Name -> null
        }
    }
}