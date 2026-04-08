@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.commons.app.model.utils.Editable
import kotlin.time.Clock

class DateModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    private val goForward: () -> Unit,
) {

    @Serializable
    data class Skeleton(
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
        val date: MutableStateFlow<LocalDate>,
    ) {

        val goBackHandler: GoBackHandler
            get() = NeverGoBackHandler
    }

    fun createPage(): Page = Page(
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