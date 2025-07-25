package hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class CategoryConfig(
    val title: String? = null,
) {

    operator fun plus(
        other: CategoryConfig,
    ): CategoryConfig = CategoryConfig(
        title = other.title ?: title,
    )

    companion object {

        val empty = CategoryConfig()
    }
}

