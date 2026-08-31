package org.hnau.pinfin.model.utils.budget.state.prototype

import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.UpdateType
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.model.utils.budget.state.updateTypeMapper
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
    updateType.fold(
        ifRemoveTransaction = { id ->
            transactions -= id
        },
        ifTransaction = { id, transaction ->
            transactions += (id to transaction)
        },
        ifConfig = { config ->
            info += config
        },
        ifAccountConfig = { id, config ->
            accountsConfigs[id] =
                accountsConfigs.getOrElse(id) { AccountConfig.empty } + config
        },
        ifCategoryConfig = { id, config ->
            categoriesConfigs[id] =
                categoriesConfigs.getOrElse(id) { CategoryConfig.empty } + config
        },
    )
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