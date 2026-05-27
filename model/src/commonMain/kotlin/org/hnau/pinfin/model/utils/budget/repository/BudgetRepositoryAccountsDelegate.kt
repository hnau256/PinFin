package org.hnau.pinfin.model.utils.budget.repository

import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.UpdateType

class BudgetRepositoryAccountsDelegate(
    private val addUpdate: suspend (UpdateType) -> Unit,
) {

    suspend fun addConfig(
        id: AccountId,
        config: AccountConfig,
    ) {
        addUpdate(
            UpdateType.AccountConfig(
                id = id,
                config = config,
            )
        )
    }
}