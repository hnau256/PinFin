package hnau.pinfin.compose

import hnau.common.model.goback.GlobalGoBackHandler
import hnau.common.projector.utils.DynamicColorsGenerator
import hnau.pinfin.projector.RootProjector
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface AppContentDependencies {

    fun root(
        globalGoBackHandler: GlobalGoBackHandler,
    ): RootProjector.Dependencies

    companion object
}