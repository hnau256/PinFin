package hnau.pinfin.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CategoryDirection {

    @SerialName("credit")
    Credit,

    @SerialName("debit")
    Debit,
    ;

    companion object {

        val default: CategoryDirection = Debit
    }
}

@Serializable
data class CategoryDirectionValues<T>(
    val credit: T,
    val debit: T,
) {

    operator fun get(
        direction: CategoryDirection,
    ): T = when (direction) {
        CategoryDirection.Credit -> credit
        CategoryDirection.Debit -> debit
    }
}