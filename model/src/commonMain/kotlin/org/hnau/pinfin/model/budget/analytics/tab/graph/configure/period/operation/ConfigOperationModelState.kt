package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period.operation

import kotlinx.datetime.DatePeriod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsPageConfig

@Serializable
sealed interface ConfigOperationModelState<out T> {

    @Serializable
    @SerialName("sum")
    data object Sum : ConfigOperationModelState<Nothing>

    @Serializable
    @SerialName("average")
    data class Average<out T>(
        val subperiod: T,
    ) : ConfigOperationModelState<T>
}

inline fun <I, O> ConfigOperationModelState<I>.fold(
    ifSum: () -> O,
    ifAverage: (I) -> O,
): O = when (this) {
    is ConfigOperationModelState.Average -> ifAverage(subperiod)
    ConfigOperationModelState.Sum -> ifSum()
}

inline fun <I, O> ConfigOperationModelState<I>.flatMap(
    transform: (I) -> ConfigOperationModelState<O>,
): ConfigOperationModelState<O> = fold(
    ifSum = { ConfigOperationModelState.Sum },
    ifAverage = transform
)

inline fun <I, O> ConfigOperationModelState<I>.map(
    transform: (I) -> O,
): ConfigOperationModelState<O> = flatMap { period ->
    val transformedPeriod = transform(period)
    ConfigOperationModelState.Average(transformedPeriod)
}

val ConfigOperationModelState<DatePeriod>.operation: AnalyticsPageConfig.Operation
    get() = fold(
        ifSum = { AnalyticsPageConfig.Operation.Sum },
        ifAverage = { subperiod ->
            AnalyticsPageConfig.Operation.Average(
                subperiod = subperiod,
            )
        },
    )

val <T> ConfigOperationModelState<T>.tab: ConfigOperationModel.Tab
    get() = fold(
        ifSum = { ConfigOperationModel.Tab.Sum },
        ifAverage = { ConfigOperationModel.Tab.Average },
    )