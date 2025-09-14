@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pipe.annotations.Pipe
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
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    private val goForward: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: Page.Skeleton? = null,
        val initialTime: LocalTime?,
        val time: MutableStateFlow<LocalTime> = initialTime
            .ifNull {
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .time
            }
            .toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialTime = null,
            )

            fun createForEdit(
                time: LocalTime,
            ): Skeleton = Skeleton(
                initialTime = time,
            )
        }
    }

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val time: MutableStateFlow<LocalTime>,
    ) {

        @Pipe
        interface Dependencies

        @Serializable
        /*data*/ class Skeleton

        val goBackHandler: GoBackHandler
            get() = NeverGoBackHandler
    }

    fun createPage(
        scope: CoroutineScope,
    ): Page = Page(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { Page.Skeleton() },
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

    internal val timeEditable: StateFlow<Editable.Value<LocalTime>> = Editable.Value.create(
        scope = scope,
        value = skeleton.time,
        initialValueOrNone = skeleton.initialTime.toOption(),
    )

    val time: StateFlow<LocalTime> = timeEditable
        .mapState(scope, Editable.Value<LocalTime>::value)

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}