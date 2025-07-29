package hnau.pinfin.projector.utils

import dynamiccolor.Variant
import hnau.common.app.projector.utils.theme.DynamicSchemeConfig

private val DynamicSchemeConfigForHue = DynamicSchemeConfig(
    variant = Variant.FIDELITY,
)

val DynamicSchemeConfig.Companion.forHue: DynamicSchemeConfig
    get() = DynamicSchemeConfigForHue