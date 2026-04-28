package org.hnau.pinfin.model.utils

import org.hnau.pinfin.data.Hue
import org.hnau.commons.app.model.theme.color.Hue as ModelHue

val Hue.model: ModelHue
    get() = ModelHue(
        degrees = degrees,
    )