package hnau.pinfin.projector.utils

import hnau.common.app.model.color.material.MaterialHue
import hnau.common.app.projector.uikit.ContainerStyle
import hnau.pinfin.data.AmountDirection

val AmountDirection.hue: MaterialHue
    get() = when (this) {
        AmountDirection.Credit -> MaterialHue.LightGreen
        AmountDirection.Debit -> MaterialHue.DeepOrange
    }

private val containerStyles: MutableMap<AmountDirection, ContainerStyle> = HashMap()

val AmountDirection.containerStyle: ContainerStyle
    get() = containerStyles.getOrPut(this) {
        ContainerStyle.create(
            hue = hue,
        )
    }