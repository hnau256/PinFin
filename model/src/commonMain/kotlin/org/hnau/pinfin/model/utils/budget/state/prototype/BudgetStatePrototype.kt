package org.hnau.pinfin.model.utils.budget.state.prototype

import org.hnau.commons.kotlin.castOrNull
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.CategoryConfig
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Transaction
import org.hnau.upchain.core.UpchainHash

data class BudgetStatePrototype(
    val hash: UpchainHash?,
    val config: BudgetConfig,
    val transactions: Map<Transaction.Id, Transaction>,
    val accountsConfigs: Map<AccountId, AccountConfig>,
    val categoriesConfigs: Map<CategoryId, CategoryConfig>,
) {

    override fun equals(
        other: Any?,
    ): Boolean = other
        ?.castOrNull<BudgetStatePrototype>()
        ?.takeIf { it.hash == hash } != null

    override fun hashCode(): Int = hash.hashCode()

    companion object {

        val empty: BudgetStatePrototype = BudgetStatePrototype(
            hash = null,
            config = BudgetConfig.empty,
            accountsConfigs = emptyMap(),
            transactions = emptyMap(),
            categoriesConfigs = emptyMap(),
        )
    }
}