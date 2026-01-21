@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.flow.state.mapState
import hnau.common.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.transaction.utils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Clock

class TimeModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    private val goForward: () -> Unit,
) {

    @Serializable
    data class Time(
        val hour: Int,
        val minute: Int,
    )

    @Serializable
    data class Skeleton(
        val initialTime: Time?,
        val time: MutableStateFlow<Time> = initialTime
            .ifNull {
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .time
                    .let(::createTime)
            }
            .toMutableStateFlowAsInitial(),
    ) {

        companion object {

            private fun createTime(
                localTime: LocalTime,
            ): Time = Time(
                hour = localTime.hour,
                minute = localTime.minute,
            )

            fun createForNew(): Skeleton = Skeleton(
                initialTime = null,
            )

            fun createForEdit(
                time: LocalTime,
            ): Skeleton = Skeleton(
                initialTime = time.let(::createTime),
            )
        }
    }

    class Page(
        val time: MutableStateFlow<Time>,
    ) {

        val goBackHandler: GoBackHandler
            get() = NeverGoBackHandler
    }

    fun createPage(): Page = Page(
        time = skeleton.time,
    )

    init {
        scope.launch {
            var cache = skeleton.time.value
            skeleton.time.collect { newTime ->
                val localCache = cache
                cache = newTime
                if (newTime.minute != localCache.minute) {
                    goForward()
                }
            }
        }
    }

    internal val timeEditable: StateFlow<Editable.Value<LocalTime>> = Editable.Value
        .create(
            scope = scope,
            value = skeleton.time,
            initialValueOrNone = skeleton.initialTime.toOption(),
        )
        .mapState(scope) { timeEditableValue ->
            timeEditableValue.map { time ->
                LocalTime(
                    hour = time.hour,
                    minute = time.minute,
                    second = 0,
                    nanosecond = 0,
                )
            }
        }

    val time: StateFlow<LocalTime> = timeEditable
        .mapState(scope, Editable.Value<LocalTime>::value)

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}