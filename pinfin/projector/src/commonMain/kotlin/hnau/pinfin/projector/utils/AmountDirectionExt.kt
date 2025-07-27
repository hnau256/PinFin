package hnau.pinfin.projector.utils

import hnau.common.app.model.color.material.MaterialHue
import hnau.pinfin.data.AmountDirection

val AmountDirection.hue: MaterialHue
    get() = when (this) {
        AmountDirection.Credit -> MaterialHue.Green
        AmountDirection.Debit -> MaterialHue.Orange
    }