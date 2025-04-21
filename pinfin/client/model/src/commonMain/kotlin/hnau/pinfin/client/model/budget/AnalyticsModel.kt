package hnau.pinfin.client.model.budget

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.client.data.budget.BudgetRepository
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class AnalyticsModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val budgetsRepository: BudgetRepository
    }

    @Serializable
    /*data*/ class Skeleton


}