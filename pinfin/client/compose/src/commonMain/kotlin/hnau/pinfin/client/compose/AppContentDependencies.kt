package hnau.pinfin.client.compose

import hnau.common.app.goback.GlobalGoBackHandler
import hnau.common.compose.utils.DynamicColorsGenerator
import hnau.pinfin.client.projector.RootProjector
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface AppContentDependencies {

    val dynamicColorsGenerator: DynamicColorsGenerator?

    fun root(
        globalGoBackHandler: GlobalGoBackHandler,
    ): RootProjector.Dependencies

    companion object
}