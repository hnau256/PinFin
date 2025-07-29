package hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class CategoryConfig(
    val title: String? = null,
    val hue: Hue? = null,
    val icon: Icon? = null,
) {

    operator fun plus(
        other: CategoryConfig,
    ): CategoryConfig = CategoryConfig(
        title = other.title ?: title,
        hue = other.hue ?: hue,
        icon = other.icon ?: icon,
    )

    companion object {

        val empty = CategoryConfig()
    }
}

