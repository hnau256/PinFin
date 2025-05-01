package hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class BudgetConfig(
    val title: String? = null,
) {

    operator fun plus(
        other: BudgetConfig,
    ): BudgetConfig = BudgetConfig(
        title = other.title ?: title,
    )

    companion object {

        val empty = BudgetConfig()
    }
}

