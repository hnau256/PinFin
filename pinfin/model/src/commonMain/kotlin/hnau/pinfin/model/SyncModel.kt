package hnau.pinfin.model

import hnau.common.app.goback.GoBackHandler
import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.model.mode.ManageOpener
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class SyncModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    skeleton: Skeleton,
) : GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

        val manageOpener: ManageOpener
    }

    fun manageOpen() {
        dependencies.manageOpener.openManage()
    }

    @Serializable
    /*data*/ class Skeleton

    override val goBackHandler: GoBackHandler = TODO()
}