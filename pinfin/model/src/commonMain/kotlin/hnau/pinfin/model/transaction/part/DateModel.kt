@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction.page.DatePageModel
import hnau.pinfin.model.transaction.page.PageModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val requestFocus: () -> Unit,
    val isFocused: StateFlow<Boolean>,
) {

    @Pipe
    interface Dependencies {

        fun page(): DatePageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val date: MutableStateFlow<LocalDate>,
        var page: DatePageModel.Skeleton? = null,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                date = Clock
                    .System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                date: LocalDate,
            ): Skeleton = Skeleton(
                date = date.toMutableStateFlowAsInitial(),
            )
        }
    }

    val date: StateFlow<LocalDate>
        get() = skeleton.date

    fun createPage(
        scope: CoroutineScope,
    ): PageModel = PageModel.Date(
        model = DatePageModel(
            scope = scope,
            dependencies = dependencies.page(),
            skeleton = skeleton::page
                .toAccessor()
                .getOrInit { DatePageModel.Skeleton() },
            date = skeleton.date,
            onDateChanged = { skeleton.date.value = it },
        )
    )
}