package hnau.pinfin.model.budget.config

import hnau.common.model.EditingString
import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.model.toEditingString
import hnau.common.kotlin.coroutines.InProgressRegistry
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.model.manage.BudgetsListOpener
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class BudgetConfigModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetsListOpener: BudgetsListOpener

        val repository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val editName: MutableStateFlow<MutableStateFlow<EditingString>?> =
            null.toMutableStateFlowAsInitial(),
        val removeDialogVisible: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val removeDialogVisible: MutableStateFlow<Boolean>
        get() = skeleton.removeDialogVisible

    private val inProgressRegistry = InProgressRegistry()

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
        .scopedInState(scope)
        .flatMapState(scope) { (stateScope, editNameSkeletonOrNull) ->
            editNameSkeletonOrNull.foldNullable(
                ifNull = {
                    dependencies
                        .repository
                        .state
                        .mapState(stateScope) {
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
                            save = actionOrNullIfExecuting(stateScope) {
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

    override val goBackHandler: GoBackHandler = nameOrEdit.mapState(scope) { nameOrEditModel ->
        when (nameOrEditModel) {
            is NameOrEdit.Edit -> nameOrEditModel.cancel
            is NameOrEdit.Name -> null
        }
    }
}