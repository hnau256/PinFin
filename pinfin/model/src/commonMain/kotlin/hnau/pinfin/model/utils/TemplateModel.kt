@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.utils

import hnau.common.model.goback.GoBackHandler
import hnau.common.model.goback.GoBackHandlerProvider
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.shuffler.annotations.Shuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TemplateModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
): GoBackHandlerProvider {

    @Shuffle
    interface Dependencies {

    }

    @Serializable
    /*data*/ class Skeleton

    override val goBackHandler: GoBackHandler = TODO()
}