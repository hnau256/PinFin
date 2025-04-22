package hnau.pinfin.data.repository.budget

import hnau.common.kotlin.castOrNull
import hnau.pinfin.data.dto.SignedAmount
import hnau.pinfin.data.dto.AccountId
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