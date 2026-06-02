@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.manage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.EditingString
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.BudgetSyncDelegate
import org.hnau.pinfin.model.budgetstack.BudgetStackOpener
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository

class BudgetManageModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val repository: BudgetRepository

        val budgetStackOpener: BudgetStackOpener

        val sync: BudgetSyncDelegate

        fun remove(): BudgetManageRemoveModel.Dependencies

        fun share(): BudgetManageShareModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val editName: MutableStateFlow<MutableStateFlow<EditingString>?> =
            null.toMutableStateFlowAsInitial(),
        val remove: BudgetManageRemoveModel.Skeleton = BudgetManageRemoveModel.Skeleton(),
        val share: BudgetManageShareModel.Skeleton = BudgetManageShareModel.Skeleton(),
    )

    val sync: BudgetSyncDelegate
        get() = dependencies.sync

    val remove = BudgetManageRemoveModel(
        scope = scope,
        skeleton = skeleton.remove,
        dependencies = dependencies.remove(),
    )

    val share = BudgetManageShareModel(
        scope = scope,
        skeleton = skeleton.share,
        dependencies = dependencies.share(),
    )

    fun openCategories() {
        dependencies
            .budgetStackOpener
            .openCategories()
    }

    fun openSettings() {
        dependencies
            .budgetStackOpener
            .openSettings()
    }

    fun openCreateBudget() {
        dependencies
            .budgetStackOpener
            .openCreateBudget()
    }

    fun openSwitchBudget() {
        dependencies
            .budgetStackOpener
            .openSwitchBudget()
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}