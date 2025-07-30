package hnau.pinfin.model.utils

import hnau.pinfin.data.Hue
import hnau.common.app.model.theme.Hue as ModelHue

val Hue.model: ModelHue
    get() = ModelHue(
        degrees = degrees.toDouble(),
    )