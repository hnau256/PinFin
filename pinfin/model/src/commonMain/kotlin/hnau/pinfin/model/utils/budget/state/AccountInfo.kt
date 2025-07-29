package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.AccountConfig
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pinfin.model.utils.icons.variant
import kotlinx.serialization.Serializable

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
        hue = config?.hue ?: Hue.calcDefault(id.id.hashCode()),
        icon = config?.icon?.variant,
    )

    val visible: Boolean
        get() = !hideIfAmountIsZero || amount.value != 0

    override fun compareTo(
        other: AccountInfo,
    ): Int = title.compareTo(
        other = other.title,
    )
}