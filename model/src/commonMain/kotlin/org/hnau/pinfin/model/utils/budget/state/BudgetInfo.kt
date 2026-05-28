package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.Serializable
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.data.Currency
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

@Serializable
data class BudgetInfo(
    val title: String,
    val currency: Currency,
    val sync: Sync,
) {

    @Serializable
    data class Sync(
        val scheme: HttpScheme,
        val host: ServerHost,
        val onLaunch: Boolean,
        val onUpdate: Boolean,
    ) {

        operator fun plus(
            config: BudgetConfig.Sync,
        ): Sync = Sync(
            scheme = config.scheme ?: scheme,
            host = config.host ?: host,
            onLaunch = config.onLaunch ?: onLaunch,
            onUpdate = config.onUpdate ?: onUpdate,
        )

        operator fun minus(
            other: Sync,
        ): BudgetConfig.Sync = BudgetConfig.Sync(
            scheme = scheme.takeIf { it != other.scheme },
            host = host.takeIf { it != other.host },
            onLaunch = onLaunch.takeIf { it != other.onLaunch },
            onUpdate = onUpdate.takeIf { it != other.onUpdate },
        )

        companion object {

            fun create(
                config: BudgetConfig.Sync,
            ): Sync = default + config

            val default = Sync(
                scheme = HttpScheme.default,
                host = ServerHost.createOrNull("upchain.hnau.org")!!,
                onLaunch = true,
                onUpdate = true,
            )
        }
    }

    operator fun plus(
        config: BudgetConfig,
    ): BudgetInfo = BudgetInfo(
        title = config.title ?: title,
        currency = config.currency ?: currency,
        sync = sync + config.sync,
    )

    operator fun minus(
        other: BudgetInfo,
    ): BudgetConfig = BudgetConfig(
        title = title.takeIf { it != other.title },
        currency = currency.takeIf { it != other.currency },
        sync = sync - other.sync,
    )

    companion object {

        fun create(
            id: BudgetId,
            config: BudgetConfig?,
        ): BudgetInfo = createDefault(
            id = id,
        ).let { default ->
            config.foldNullable(
                ifNull = { default },
                ifNotNull = { configNotNull -> default + configNotNull },
            )
        }

        private fun createDefault(
            id: BudgetId,
        ): BudgetInfo = BudgetInfo(
            title = id.id.toString(),
            currency = Currency.default,
            sync = Sync.default,
        )
    }
}