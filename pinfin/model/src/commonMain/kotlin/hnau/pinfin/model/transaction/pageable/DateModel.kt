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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Clock

class DateModel(
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
        val initialDate: LocalDate?,
        val date: MutableStateFlow<LocalDate> = initialDate
            .ifNull {
                Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
            }
            .toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialDate = null,
            )

            fun createForEdit(
                date: LocalDate,
            ): Skeleton = Skeleton(
                initialDate = date,
            )
        }
    }

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val date: MutableStateFlow<LocalDate>,
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
        date = skeleton.date,
    )

    init {
        scope.launch {
            var cache = skeleton.date.value
            skeleton.date.collect { newTime ->
                val localCache = cache
                cache = newTime
                if (newTime != localCache) {
                    goForward()
                }
            }
        }
    }

    internal val dateEditable: StateFlow<Editable.Value<LocalDate>> = Editable.Value.create(
        scope = scope,
        value = skeleton.date,
        initialValueOrNone = skeleton.initialDate.toOption(),
    )

    val date: StateFlow<LocalDate> = dateEditable
        .mapState(scope, Editable.Value<LocalDate>::value)

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}