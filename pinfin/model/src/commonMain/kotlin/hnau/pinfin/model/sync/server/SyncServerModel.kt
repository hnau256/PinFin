@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.server

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.mode.ManageOpener
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SyncServerModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    private val goBack: () -> Unit,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {
    }

    @Serializable
    data class Skeleton(
        val port: ServerPort,
        val stopServerDialogIsOpened: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    override val goBackHandler: GoBackHandler = {
        skeleton.stopServerDialogIsOpened.update { !it }
    }.toMutableStateFlowAsInitial()
}