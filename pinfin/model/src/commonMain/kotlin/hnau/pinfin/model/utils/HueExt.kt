package hnau.pinfin.model.utils

import hnau.pinfin.data.Hue
import org.hnau.commons.app.model.utils.Hue as ModelHue

val Hue.model: ModelHue
    get() = ModelHue(
        degrees = degrees,
    )