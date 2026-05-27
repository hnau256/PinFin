package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.Currency
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class BudgetInfo(
    val title: String,
    val currency: Currency,
) {

    operator fun plus(
        config: BudgetConfig,
    ): BudgetInfo = BudgetInfo(
        title = config.title ?: title,
        currency = config.currency ?: currency,
    )

    companion object {

        @OptIn(ExperimentalUuidApi::class)
        fun create(
            id: BudgetId,
            config: BudgetConfig,
        ): BudgetInfo = BudgetInfo(
            title = config.title ?: id.id.toString(),
            currency = config.currency ?: Currency.default,
        )
    }
}