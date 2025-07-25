package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.AccountConfig
import hnau.pinfin.data.AccountId
import hnau.pinfin.data.Amount
import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(
    val id: AccountId,
    val amount: Amount,
    val title: String,
    val hideIfAmountIsZero: Boolean,
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
    )

    val visible: Boolean
        get() = !hideIfAmountIsZero || amount.value != 0

    override fun compareTo(
        other: AccountInfo,
    ): Int = title.compareTo(
        other = other.title,
    )
}