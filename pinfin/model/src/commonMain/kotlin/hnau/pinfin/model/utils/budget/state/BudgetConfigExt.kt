package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.BudgetConfig

operator fun BudgetConfig.plus(
    other: BudgetConfig
): BudgetConfig = BudgetConfig(
    title = other.title ?: title,
)