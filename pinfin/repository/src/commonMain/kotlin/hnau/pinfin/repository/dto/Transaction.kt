package hnau.pinfin.repository.dto

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringToUUID
import hnau.common.kotlin.serialization.UUIDSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Transaction(
    @SerialName("timestamp")
    val timestamp: Instant,

    @SerialName("comment")
    val comment: Comment,

    @SerialName("type")
    val type: Type,
) {

    @Serializable
    @JvmInline
    value class Id(
        @Serializable(UUIDSerializer::class)
        val id: UUID,
    ) {

        companion object {

            fun new(): Id =
                Id(UUID.randomUUID())

            val uuidMapper: Mapper<UUID, Id> = Mapper(
                direct = ::Id,
                reverse = Id::id,
            )

            val stringMapper: Mapper<String, Id> =
                Mapper.stringToUUID + uuidMapper
        }
    }

    @Serializable
    sealed interface Type {

        @Serializable
        @SerialName("entry")
        data class Entry(

            @SerialName("account")
            val account: AccountId,

            @SerialName("records")
            @Serializable(NonEmptyListSerializer::class)
            val records: NonEmptyList<Record>,
        ) : Type

        @Serializable
        @SerialName("transfer")
        data class Transfer(

            @SerialName("from")
            val from: AccountId,

            @SerialName("to")
            val to: AccountId,

            @SerialName("amount")
            val amount: Amount,
        ) : Type
    }
}


