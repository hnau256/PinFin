package org.hnau.pinfin.model.utils.budget

import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.data.BudgetId
import org.hnau.upchain.core.UpchainId

private val budgetIdUpchainIdMapper = Mapper<UpchainId, BudgetId>(
    direct = { upchainId -> upchainId.id.let(::BudgetId) },
    reverse = { budgetId -> budgetId.id.let(::UpchainId) },
)

val BudgetId.Companion.upchainUIdMapper: Mapper<UpchainId, BudgetId>
    get() = budgetIdUpchainIdMapper