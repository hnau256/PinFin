package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.Serializable
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.model.utils.icons.IconVariant
import org.hnau.pinfin.model.utils.icons.variant
import org.hnau.commons.app.model.utils.Hue as ModelHue

@Serializable
data class AccountInfo(
    val id: AccountId,
    val amount: Amount,
    val title: String,
    val hideIfAmountIsZero: Boolean,
    val hue: Hue,
    val icon: IconVariant?,
) : Comparable<AccountInfo> {

    constructor(
        id: AccountId,
        amount: Amount,
        config: AccountConfig?,
    ) : this(
        id = id,
        amount = amount,
        title = config?.title ?: id.id,
        hideIfAmountIsZero = config?.hideIfAmountIsZero == true,
        hue = config?.hue ?: ModelHue.calcDefault(id.id.hashCode()).degrees.let(::Hue),
        icon = config?.icon?.variant,
    )

    val visible: Boolean
        get() = !hideIfAmountIsZero || amount != Amount.zero

    override fun compareTo(
        other: AccountInfo,
    ): Int = title.compareTo(
        other = other.title,
    )
}