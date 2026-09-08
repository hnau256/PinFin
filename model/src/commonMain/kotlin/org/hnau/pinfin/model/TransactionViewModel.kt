package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifTrue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.records.FilteredRecords
import org.hnau.pinfin.model.budgetstack.BudgetStackOpener
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

class TransactionViewModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val onReady: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener
    }

    @Serializable
    data class Skeleton(
        val id: Transaction.Id,
        val transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, FilteredRecords<KeyValue<CategoryId, CategoryInfo>>>,
        val removeDialogIsVisible: MutableStateFlow<Boolean> = false.toMutableStateFlowAsInitial()
    )

    val transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, FilteredRecords<KeyValue<CategoryId, CategoryInfo>>>
        get() = skeleton.transaction

    fun edit() {
        dependencies
            .budgetStackOpener
            .openEditTransaction(
                id = skeleton.id,
                info = skeleton.transaction,
            )
    }

    fun remove() {
        skeleton.removeDialogIsVisible.value = true
    }

    data class RemoveDialog(
        val remove: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>>,
        val cancel: () -> Unit,
    )

    val removeDialog: StateFlow<RemoveDialog?> = skeleton
        .removeDialogIsVisible
        .mapWithScope(scope) { scope, visible ->
            visible.ifTrue {
                RemoveDialog(
                    remove = actionOrCancelIfExecuting(scope) {
                        dependencies.budgetRepository.transactions.remove(skeleton.id)
                        onReady()
                    },
                    cancel = { skeleton.removeDialogIsVisible.value = false }
                )
            }
        }

    val goBackHandler: GoBackHandler = skeleton
        .removeDialogIsVisible
        .mapState(scope) { visible ->
            visible.ifTrue { { skeleton.removeDialogIsVisible.value = false } }
        }
}