package hnau.pinfin.data

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringToUuid
import hnau.common.kotlin.serialization.UuidSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
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
        @Serializable(UuidSerializer::class)
        val id: Uuid,
    ) {

        companion object {

            fun new(): Id =
                Id(Uuid.random())

            val UuidMapper: Mapper<Uuid, Id> = Mapper(
                direct = ::Id,
                reverse = Id::id,
            )

            val stringMapper: Mapper<String, Id> =
                Mapper.stringToUuid + UuidMapper
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


