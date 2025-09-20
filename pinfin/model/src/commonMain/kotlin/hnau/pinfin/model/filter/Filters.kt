package hnau.pinfin.model.filter

import hnau.pinfin.data.CategoryId
import kotlinx.serialization.Serializable

@Serializable
data class Filters(
    val selectedCategories: List<CategoryId>,
) {

    companion object {

        val empty = Filters(
            selectedCategories = emptyList(),
        )
    }
}