package org.hnau.pinfin.model.utils.budget.state

import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.UpdateType
import org.hnau.upchain.core.Update
import org.hnau.upchain.core.calcNext

operator fun BudgetStatePrototype.plus(
    update: Update,
): BudgetStatePrototype {
    val updateType = UpdateType.updateTypeMapper.direct(update)
    val transactions = transactions.toMutableMap()
    val accountsConfigs = accountsConfigs.toMutableMap()
    val categoriesConfigs = categoriesConfigs.toMutableMap()
    var info = config
    when (updateType) {
        is UpdateType.RemoveTransaction ->
            transactions -= updateType.id

        is UpdateType.Transaction ->
            transactions += (updateType.id to updateType.transaction)

        is UpdateType.Config ->
            info += updateType.config

        is UpdateType.AccountConfig -> accountsConfigs[updateType.id] =
            accountsConfigs.getOrElse(updateType.id) { AccountConfig.empty } + updateType.config

        is UpdateType.CategoryConfig -> categoriesConfigs[updateType.id] =
            categoriesConfigs.getOrElse(updateType.id) { CategoryConfig.empty } + updateType.config
    }
    return BudgetStatePrototype(
        hash = hash.calcNext(
            update = update,
        ),
        transactions = transactions,
        accountsConfigs = accountsConfigs,
        categoriesConfigs = categoriesConfigs,
        config = info,
    )
}