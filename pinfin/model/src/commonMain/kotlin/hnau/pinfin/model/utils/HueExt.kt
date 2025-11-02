package hnau.pinfin.model.utils

import hnau.pinfin.data.Hue
import hnau.common.app.model.utils.Hue as ModelHue

val Hue.model: ModelHue
    get() = ModelHue(
        degrees = degrees,
    )