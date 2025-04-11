package hnau.pinfin.client.data.budget

import hnau.pinfin.scheme.AccountId

data class AccountInfo(
    val title: String,
)

interface AccountInfoResolver {

    operator fun get(
        accountId: AccountId,
    ): AccountInfo
}