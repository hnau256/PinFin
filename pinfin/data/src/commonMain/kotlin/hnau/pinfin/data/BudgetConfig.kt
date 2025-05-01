package hnau.pinfin.data

import kotlinx.serialization.Serializable

@Serializable
data class BudgetConfig(
    val title: String? = null,
) {

    companion object
}