package hnau.pinfin.client.compose

import hnau.common.compose.utils.DynamicColorsGenerator
import hnau.pinfin.client.projector.LoadBudgetProjector
import hnau.pinfin.client.projector.RootProjector
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface AppContentDependencies {

    val dynamicColorsGenerator: DynamicColorsGenerator?

    fun root(): RootProjector.Dependencies

    companion object
}