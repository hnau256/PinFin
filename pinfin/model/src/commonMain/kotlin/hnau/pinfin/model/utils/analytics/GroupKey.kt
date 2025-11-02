package hnau.pinfin.model.utils.analytics

import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.CategoryInfo

sealed interface GroupKey {

    data class Category(
        val category: CategoryInfo?,
    ): GroupKey

    data class Account(
        val account: AccountInfo,
    ): GroupKey
}