@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifTrue
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.records.FilteredRecord
import org.hnau.pinfin.model.budgetstack.BudgetStackOpener
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.AccountIdWithInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryIdWithInfo

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
        val transaction: Transaction<AccountIdWithInfo, CategoryIdWithInfo, FilteredRecord<CategoryIdWithInfo>>,
        val removeDialogIsVisible: MutableStateFlow<Boolean> = false.toMutableStateFlowAsInitial()
    )

    val transaction: Transaction<AccountIdWithInfo, CategoryIdWithInfo, FilteredRecord<CategoryIdWithInfo>>
        get() = skeleton.transaction

    val currency: StateFlow<Currency> = dependencies
        .budgetRepository
        .state
        .mapState(scope) { it.info.currency }

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