package org.hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class AccountConfig(
    val title: String?,
    val hue: Hue?,
    val hideIfAmountIsZero: Boolean?,
    val icon: Icon?,
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

        val empty = AccountConfig(
            title = null,
            hue = null,
            hideIfAmountIsZero = null,
            icon = null
        )
    }
}
