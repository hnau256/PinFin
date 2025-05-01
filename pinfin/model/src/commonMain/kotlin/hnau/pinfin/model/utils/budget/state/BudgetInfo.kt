package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.data.BudgetId
import kotlinx.serialization.Serializable

@Serializable
data class BudgetInfo(
    val title: String,
) {

    companion object {

        fun create(
            id: BudgetId,
            config: BudgetConfig,
        ): BudgetInfo = BudgetInfo(
            title = config.title ?: "Budget title placeholder" /*id.id.toString()*/,
        )
    }
}