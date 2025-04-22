package hnau.pinfin.data.dto

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class AccountId(
    val id: String,
): Comparable<AccountId> {

    override fun compareTo(other: AccountId): Int =
        id.compareTo(other.id)

    companion object {

        val stringMapper: Mapper<String, AccountId> = Mapper(
            direct = ::AccountId,
            reverse = AccountId::id,
        )
    }
}