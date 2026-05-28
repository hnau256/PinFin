package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.Serializable
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
    ) {

        operator fun plus(
            config: BudgetConfig.Sync,
        ): Sync = Sync(
            scheme = config.scheme ?: scheme,
            host = config.host ?: host,
        )

        companion object {

            fun create(
                config: BudgetConfig.Sync,
            ): Sync = Sync(
                scheme = config.scheme ?: HttpScheme.default,
                host = config.host ?: ServerHost.createOrNull("upchain.hnau.org")!!,
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

    companion object {

        fun create(
            id: BudgetId,
            config: BudgetConfig,
        ): BudgetInfo = BudgetInfo(
            title = config.title ?: id.id.toString(),
            currency = config.currency ?: Currency.default,
            sync = Sync.create(
                config = config.sync,
            )
        )
    }
}