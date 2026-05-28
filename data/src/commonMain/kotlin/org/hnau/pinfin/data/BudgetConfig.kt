package org.hnau.pinfin.data

import kotlinx.serialization.Serializable
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

@Serializable
data class BudgetConfig(
    val title: String?,
    val currency: Currency?,
    val sync: Sync,
) {

    @Serializable
    data class Sync(
        val scheme: HttpScheme?,
        val host: ServerHost?,
        val onLaunch: Boolean?,
        val onUpdate: Boolean?,
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

            val empty = Sync(
                scheme = null,
                host = null,
                onLaunch = null,
                onUpdate = null,
            )
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

        val empty = BudgetConfig(
            title = null,
            currency = null,
            sync = Sync.empty,
        )
    }
}

