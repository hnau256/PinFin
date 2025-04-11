package hnau.pinfin.client.compose

import hnau.common.compose.utils.DynamicColorsGenerator
import hnau.pinfin.client.projector.InitProjector
import hnau.shuffler.annotations.Shuffle

@Shuffle
interface AppContentDependencies {

    val dynamicColorsGenerator: DynamicColorsGenerator?

    fun init(): InitProjector.Dependencies

    companion object
}