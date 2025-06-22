package hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class AccountConfig(
    val title: String? = null,
    val hideIfAmountIsZero: Boolean? = null,
) {

    operator fun plus(
        other: AccountConfig,
    ): AccountConfig = AccountConfig(
        title = other.title ?: title,
        hideIfAmountIsZero = other.hideIfAmountIsZero ?: hideIfAmountIsZero,
    )

    companion object {

        val empty = AccountConfig()
    }
}
