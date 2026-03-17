package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.split

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ConfigSplitPeriodModelState<out T> {

    @Serializable
    @SerialName("inclusive")
    data object Inclusive : ConfigSplitPeriodModelState<Nothing>

    @Serializable
    @SerialName("fixed")
    data class Fixed<out T>(
        val period: T,
    ) : ConfigSplitPeriodModelState<T>
}

inline fun <I, O> ConfigSplitPeriodModelState<I>.fold(
    ifInclusive: () -> O,
    ifFixed: (I) -> O,
): O = when (this) {
    is ConfigSplitPeriodModelState.Fixed -> ifFixed(period)
    ConfigSplitPeriodModelState.Inclusive -> ifInclusive()
}

inline fun <I, O> ConfigSplitPeriodModelState<I>.flatMap(
    transform: (I) -> ConfigSplitPeriodModelState<O>,
): ConfigSplitPeriodModelState<O> = fold(
    ifInclusive = { ConfigSplitPeriodModelState.Inclusive },
    ifFixed = transform
)

inline fun <I, O> ConfigSplitPeriodModelState<I>.map(
    transform: (I) -> O,
): ConfigSplitPeriodModelState<O> = flatMap { period ->
    val transformedPeriod = transform(period)
    ConfigSplitPeriodModelState.Fixed(transformedPeriod)
}

val <T> ConfigSplitPeriodModelState<T>.tab: ConfigSplitPeriodModel.Tab
    get() = fold(
        ifInclusive = { ConfigSplitPeriodModel.Tab.Inclusive },
        ifFixed = { ConfigSplitPeriodModel.Tab.Fixed },
    )