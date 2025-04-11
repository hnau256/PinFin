package hnau.pinfin.client.model.init

import hnau.common.app.goback.GoBackHandler
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable

class InitModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    private val dependencies: InitModel.Dependencies,
) {

    @Serializable
    data class Skeleton(
        val a: Int = 0,
    )

    @Shuffle
    interface Dependencies

    val goBackHandler: GoBackHandler
        get() = null.toMutableStateFlowAsInitial()
}