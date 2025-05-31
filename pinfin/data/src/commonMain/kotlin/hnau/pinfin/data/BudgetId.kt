package hnau.pinfin.data

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringToUuid
import hnau.common.kotlin.serialization.UuidSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
@JvmInline
value class BudgetId(
    @Serializable(UuidSerializer::class)
    val id: Uuid,
) : Comparable<BudgetId> {

    override fun compareTo(
        other: BudgetId,
    ): Int = id.compareTo(
        other = other.id,
    )

    companion object {

        fun new(): BudgetId =
            BudgetId(Uuid.random())

        val UuidMapper: Mapper<Uuid, BudgetId> = Mapper(
            direct = ::BudgetId,
            reverse = BudgetId::id,
        )

        val stringMapper: Mapper<String, BudgetId> =
            Mapper.Companion.stringToUuid + UuidMapper
    }
}