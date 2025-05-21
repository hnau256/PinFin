package hnau.pinfin.compose

import hnau.common.model.goback.GlobalGoBackHandler
import hnau.common.compose.utils.DynamicColorsGenerator
import hnau.pinfin.projector.RootProjector
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface AppContentDependencies {

    val dynamicColorsGenerator: DynamicColorsGenerator?

    fun root(
        globalGoBackHandler: GlobalGoBackHandler,
    ): RootProjector.Dependencies

    companion object
}