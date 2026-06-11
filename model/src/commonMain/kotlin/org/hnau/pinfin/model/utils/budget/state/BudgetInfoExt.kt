package org.hnau.pinfin.model.utils.budget.state

import org.hnau.pinfin.data.BudgetConfig

fun BudgetInfo.toConfig(): BudgetConfig = BudgetConfig(
    title = title,
    currency = currency,
    sync = BudgetConfig.Sync(
        scheme = sync.scheme,
        host = sync.host,
        onLaunch = sync.onLaunch,
        onUpdate = sync.onUpdate,
    )
)