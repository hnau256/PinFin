package org.hnau.pinfin.data

import kotlinx.serialization.Serializable
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

@Serializable
data class BudgetConfig(
    val title: String? = null,
    val currency: Currency? = null,
    val sync: Sync = Sync.empty,
) {

    @Serializable
    data class Sync(
        val scheme: HttpScheme? = null,
        val host: ServerHost? = null,
        val onLaunch: Boolean? = null,
        val onUpdate: Boolean? = null,
    ) {

        operator fun plus(
            other: Sync,
        ): Sync = Sync(
            scheme = other.scheme ?: scheme,
            host = other.host ?: host,
            onLaunch = other.onLaunch ?: onLaunch,
            onUpdate = other.onUpdate ?: onUpdate,
        )

        companion object {

            val empty = Sync()
        }
    }


    operator fun plus(
        other: BudgetConfig,
    ): BudgetConfig = BudgetConfig(
        title = other.title ?: title,
        currency = other.currency ?: currency,
        sync = sync + other.sync,
    )

    companion object {

        val empty = BudgetConfig()
    }
}

