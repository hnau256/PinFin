package org.hnau.pinfin.model.utils

import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.pinfin.data.Hue
import org.hnau.commons.app.model.theme.color.Hue as ModelHue

private val modelHueToHueMapper: Mapper<ModelHue, Hue> =
    Mapper(ModelHue::degrees, ::ModelHue) +
            Mapper(::Hue, Hue::degrees)

val Mapper.Companion.modelHueToHue: Mapper<ModelHue, Hue>
    get() = modelHueToHueMapper