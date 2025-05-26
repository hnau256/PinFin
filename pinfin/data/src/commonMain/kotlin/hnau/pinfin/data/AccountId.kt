package hnau.pinfin.data

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

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