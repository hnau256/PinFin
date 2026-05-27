@file:UseSerializers(
    NonEmptySetSerializer::class,
)

package org.hnau.pinfin.model.utils.analytics

import arrow.core.NonEmptySet
import arrow.core.serialization.NonEmptySetSerializer
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

data class AnalyticsPage(
    val period: LocalDateRange,
    val items: List<Item>,
) {

    @Serializable
    data class Item(
        val key: Key?,
        val constraints: Constraints,
    ) {

        @Serializable
        sealed interface Key {

            @Serializable
            @SerialName("category")
            data class Category(
                val idWithCategory: KeyValue<CategoryId, CategoryInfo>?,
            ): Key

            @Serializable
            @SerialName("account")
            data class Account(
                val idWithAccount: KeyValue<AccountId, AccountInfo>,
            ): Key
        }

        @Serializable
        data class Constraints(
            val categories: NonEmptySet<CategoryId?>?,
            val accounts: NonEmptySet<AccountId>?,
        )
    }
}