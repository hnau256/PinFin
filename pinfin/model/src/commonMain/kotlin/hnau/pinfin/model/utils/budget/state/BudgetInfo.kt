package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.BudgetConfig
import hnau.pinfin.data.BudgetId
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class BudgetInfo(
    val title: String,
) {

    operator fun plus(
        config: BudgetConfig,
    ): BudgetInfo = BudgetInfo(
        title = config.title ?: title,
    )

    companion object {

        @OptIn(ExperimentalUuidApi::class)
        fun create(
            id: BudgetId,
            config: BudgetConfig,
        ): BudgetInfo = BudgetInfo(
            title = config.title ?: id.id.toString(),
        )
    }
}