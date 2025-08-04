@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part

import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction.part.page.TimePageModel
import hnau.pinfin.model.transaction.part.page.PageModel
import hnau.pinfin.model.transaction.utils.NavAction
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val requestFocus: () -> Unit,
    val isFocused: StateFlow<Boolean>,
) : PartModel {

    @Pipe
    interface Dependencies {

        fun page(): TimePageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val time: MutableStateFlow<LocalTime>,
        var page: TimePageModel.Skeleton? = null,
    ) : PartModel.Skeleton {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                time = Clock
                    .System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .time
                    .toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                time: LocalTime,
            ): Skeleton = Skeleton(
                time = time.toMutableStateFlowAsInitial(),
            )
        }
    }

    val time: StateFlow<LocalTime>
        get() = skeleton.time

    override fun createPage(
        scope: CoroutineScope,
        navAction: NavAction
    ): PageModel = TimePageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { TimePageModel.Skeleton() },
        navAction = navAction,
        time = skeleton.time,
        onTimeChanged = { skeleton.time.value = it },
    )
}