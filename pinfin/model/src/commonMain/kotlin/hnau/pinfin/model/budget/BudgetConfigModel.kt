package hnau.pinfin.model.budget

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.app.goback.NeverGoBackHandler
import hnau.pinfin.model.manage.BudgetsListOpener
import hnau.pinfin.model.mode.SyncOpener
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class BudgetConfigModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val syncOpener: SyncOpener

        val budgetsListOpener: BudgetsListOpener
    }

    @Serializable
    /*data*/ class Skeleton

    fun openSync() {
        dependencies.syncOpener.openSync()
    }

    fun openBudgetsList() {
        dependencies.budgetsListOpener.openBudgetsList()
    }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}