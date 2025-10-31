package hnau.pinfin.model.utils.analytics

import hnau.pinfin.data.AccountId
import hnau.pinfin.data.CategoryId

sealed interface GroupKey {

    data class Category(
        val id: CategoryId?,
    ): GroupKey

    data class Account(
        val id: AccountId,
    ): GroupKey
}