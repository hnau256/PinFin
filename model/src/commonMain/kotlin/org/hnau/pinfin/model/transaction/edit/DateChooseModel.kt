@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import kotlin.time.Clock

class DateChooseModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    val navigateContext: EditNavigateContext,
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

    fun updateDate(
        date: LocalDate,
    ) {
        val oldDate = skeleton.date.getAndUpdate { date  }
        if (oldDate != date) {
            navigateContext.goForward()
        }
    }

    val dateEditable: StateFlow<Editable.Value<LocalDate>> = Editable.Value.create(
        scope = scope,
        value = skeleton.date,
        initialValueOrNone = skeleton.initialDate.toOption(),
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}