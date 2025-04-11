package hnau.pinfin.client.data.budget

import hnau.pinfin.scheme.CategoryId

data class CategoryInfo(
    val title: String,
)

interface CategoryInfoResolver {

    operator fun get(
        categoryId: CategoryId,
    ): CategoryInfo
}