package hnau.pinfin.model.utils.budget.state

import hnau.pinfin.data.AccountConfig
import hnau.pinfin.data.AccountId
import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(
    val id: AccountId,
    val amount: SignedAmount,
    val title: String,
    val hideIfAmountIsZero: Boolean,
) : Comparable<AccountInfo> {

    constructor(
        id: AccountId,
        amount: SignedAmount,
        config: AccountConfig?,
    ) : this(
        id = id,
        amount = amount,
        title = config?.title ?: id.id,
        hideIfAmountIsZero = config?.hideIfAmountIsZero == true,
    )

    val visible: Boolean
        get() = !hideIfAmountIsZero || amount.amount.value > 0u

    override fun compareTo(
        other: AccountInfo,
    ): Int = title.compareTo(
        other = other.title,
    )
}