package hnau.pinfin.model.sync.client.list

import hnau.pinfin.model.utils.budget.upchain.UpchainHash

data class ServerBudgetPeekHash(
    val peekHash: UpchainHash?,
)