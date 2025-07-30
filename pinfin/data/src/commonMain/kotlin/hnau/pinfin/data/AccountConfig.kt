package hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class AccountConfig(
    val title: String? = null,
    val hue: Hue? = null,
    val hideIfAmountIsZero: Boolean? = null,
    val icon: Icon? = null,
) {

    operator fun plus(
        other: AccountConfig,
    ): AccountConfig = AccountConfig(
        title = other.title ?: title,
        hue = other.hue ?: hue,
        hideIfAmountIsZero = other.hideIfAmountIsZero ?: hideIfAmountIsZero,
        icon = other.icon ?: icon,
    )

    companion object {

        val empty = AccountConfig()
    }
}
