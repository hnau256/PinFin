package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.Serializable
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.pinfin.data.AccountConfig
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.Hue
import org.hnau.pinfin.model.utils.icons.IconVariant
import org.hnau.pinfin.model.utils.icons.icon
import org.hnau.pinfin.model.utils.icons.variant
import org.hnau.pinfin.model.utils.modelHueToHue
import org.hnau.commons.app.model.theme.color.Hue as ModelHue

@Serializable
data class AccountInfo(
    val amount: Amount,
    val title: String,
    val hideIfAmountIsZero: Boolean,
    val hue: Hue,
    val icon: IconVariant?,
) : Comparable<AccountInfo> {

    val visible: Boolean
        get() = !hideIfAmountIsZero || amount != Amount.zero

    override fun compareTo(
        other: AccountInfo,
    ): Int = title.compareTo(
        other = other.title,
    )

    operator fun plus(
        config: AccountConfig,
    ): AccountInfo = AccountInfo(
        amount = amount,
        title = config.title ?: title,
        hideIfAmountIsZero = config.hideIfAmountIsZero ?: hideIfAmountIsZero,
        hue = config.hue ?: hue,
        icon = config.icon?.variant ?: icon,
    )

    operator fun minus(
        other: AccountInfo,
    ): AccountConfig = AccountConfig(
        title = title.takeIf { it != other.title },
        hideIfAmountIsZero = hideIfAmountIsZero.takeIf { it != other.hideIfAmountIsZero },
        hue = hue.takeIf { it != other.hue },
        icon = icon.takeIf { it != other.icon }?.icon,
    )

    companion object {

        fun create(
            id: AccountId,
            config: AccountConfig?,
            amount: Amount,
        ): AccountInfo = createDefault(
            id = id,
            amount = amount,
        ).let { default ->
            config.foldNullable(
                ifNull = { default },
                ifNotNull = { configNotNull -> default + configNotNull },
            )
        }

        fun createDefault(
            id: AccountId,
            amount: Amount,
        ): AccountInfo = AccountInfo(
            amount = amount,
            title = id.id,
            hideIfAmountIsZero = true,
            hue = ModelHue
                .calcDefault(id.id.hashCode())
                .let(Mapper.modelHueToHue.direct),
            icon = null,
        )
    }
}