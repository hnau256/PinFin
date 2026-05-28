package org.hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class CategoryConfig(
    val title: String?,
    val hue: Hue?,
    val icon: Icon?,
) {

    operator fun plus(
        other: CategoryConfig,
    ): CategoryConfig = CategoryConfig(
        title = other.title ?: title,
        hue = other.hue ?: hue,
        icon = other.icon ?: icon,
    )

    companion object {

        val empty = CategoryConfig(
            title = null,
            hue = null,
            icon = null,
        )
    }
}

