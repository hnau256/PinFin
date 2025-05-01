package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.BudgetConfig

private val emptyBudgetConfig = BudgetConfig()

val BudgetConfig.Companion.empty: BudgetConfig
    get() = emptyBudgetConfig

operator fun BudgetConfig.plus(
    other: BudgetConfig
): BudgetConfig = BudgetConfig(
    title = other.title ?: title,
)