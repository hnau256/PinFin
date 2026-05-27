package org.hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class BudgetConfig(
    val title: String? = null,
    val currency: Currency? = null,
) {

    operator fun plus(
        other: BudgetConfig,
    ): BudgetConfig = BudgetConfig(
        title = other.title ?: title,
        currency = other.currency ?: currency,
    )

    companion object {

        val empty = BudgetConfig()
    }
}

