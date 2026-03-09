package hnau.pinfin.data

import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.stringToUuid
import org.hnau.commons.kotlin.serialization.UuidSerializer
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