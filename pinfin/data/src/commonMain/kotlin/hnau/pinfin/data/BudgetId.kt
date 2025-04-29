package hnau.pinfin.data

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.stringToUUID
import hnau.common.kotlin.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class BudgetId(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
) : Comparable<BudgetId> {

    override fun compareTo(
        other: BudgetId,
    ): Int = id.compareTo(
        other = other.id,
    )

    companion object {

        fun new(): BudgetId =
            BudgetId(UUID.randomUUID())

        val uuidMapper: Mapper<UUID, BudgetId> = Mapper(
            direct = ::BudgetId,
            reverse = BudgetId::id,
        )

        val stringMapper: Mapper<String, BudgetId> =
            Mapper.Companion.stringToUUID + uuidMapper
    }
}