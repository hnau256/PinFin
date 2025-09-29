@file:UseSerializers(
    NonEmptyListSerializer::class,
)

package hnau.pinfin.model.filter

import arrow.core.NonEmptyList
import arrow.core.serialization.NonEmptyListSerializer
import hnau.pinfin.data.CategoryId
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Filters(
    val selectedCategories: NonEmptyList<CategoryId>?,
) {

    val any: Boolean
        get() = selectedCategories != null



    companion object {

        val empty = Filters(
            selectedCategories = null,
        )
    }
}