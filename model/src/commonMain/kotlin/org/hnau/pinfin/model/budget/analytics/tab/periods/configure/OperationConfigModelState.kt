package org.hnau.pinfin.model.budget.analytics.tab.periods.configure

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
@Serializable
sealed interface OperationConfigModelState<out T> {

    @Serializable
    @SerialName("sum")
    data object Sum : OperationConfigModelState<Nothing>

    @Serializable
    @SerialName("average")
    data class Average<out T>(
        val subperiod: T,
    ) : OperationConfigModelState<T>
}

inline fun <I, O> OperationConfigModelState<I>.flatMap(
    transform: (I) -> OperationConfigModelState<O>,
): OperationConfigModelState<O> = fold(
    ifSum = { OperationConfigModelState.Sum },
    ifAverage = transform,
)

inline fun <I, O> OperationConfigModelState<I>.map(
    transform: (I) -> O,
): OperationConfigModelState<O> = flatMap { period ->
    OperationConfigModelState.Average(transform(period))
}

val <T> OperationConfigModelState<T>.tab: OperationConfigModel.Tab
    get() = fold(
        ifSum = { OperationConfigModel.Tab.Sum },
        ifAverage = { OperationConfigModel.Tab.Average },
    )
