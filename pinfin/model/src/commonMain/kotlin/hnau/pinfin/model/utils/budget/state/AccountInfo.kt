package hnau.pinfin.model.utils.budget.state

import hnau.common.kotlin.castOrNull
import hnau.pinfin.data.AccountId
import kotlinx.serialization.Serializable

@Serializable
data class AccountInfo(
    val id: AccountId,
    val amount: SignedAmount,
    val title: String = id.id,
) : Comparable<AccountInfo> {

    override fun compareTo(
        other: AccountInfo,
    ): Int = title.compareTo(
        other = other.title,
    )

    override fun equals(
        other: Any?,
    ): Boolean = other
        .castOrNull<AccountInfo>()
        ?.id == id

    override fun hashCode(): Int = id.hashCode()
}