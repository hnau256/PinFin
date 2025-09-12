@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.onSet
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.ifNull
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
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

    val time: StateFlow<LocalTime>
        get() = skeleton.time

    val isChanged: StateFlow<Boolean> = skeleton.initialTime.foldNullable(
        ifNull = { true.toMutableStateFlowAsInitial() },
        ifNotNull = { initial ->
            time.mapState(scope) { current -> current != initial }
        }
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}