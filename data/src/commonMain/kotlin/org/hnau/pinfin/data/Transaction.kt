package org.hnau.pinfin.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.stringToUuid
import org.hnau.commons.kotlin.serialization.UuidSerializer
import org.hnau.pinfin.data.expression.AmountExpression
import org.hnau.pinfin.data.records.Records
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Transaction<out A, out C, out R : Records<C>>(
    @SerialName("timestamp")
    val timestamp: LocalDate,

    @SerialName("comment")
    val comment: Comment,

    @SerialName("type")
    val type: Type<A, C, R>,
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

    @Fold
    @Serializable
    sealed interface Type<out A, out C, out R : Records<C>> {

        @Serializable
        @SerialName("entry")
        data class Entry<out A, out C, out R : Records<C>>(

            @SerialName("account")
            val account: A,

            @SerialName("records")
            @Contextual
            val records: R,
        ) : Type<A, C, R>

        @Serializable
        @SerialName("transfer")
        data class Transfer<out A, out C>(

            @SerialName("from")
            val from: A,

            @SerialName("to")
            val to: A,

            @SerialName("amount")
            val amount: AmountExpression,
        ) : Type<A, C, Nothing>
    }
}