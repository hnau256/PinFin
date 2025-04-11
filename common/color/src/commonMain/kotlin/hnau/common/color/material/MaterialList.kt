package hnau.common.color.material

import hnau.common.color.RGBABytes

class MaterialList(
    val hue: MaterialHue,
    vararg values: Pair<MaterialLightness, Int>,
) {

    val values: Map<MaterialLightness, RGBABytes> =
        values.associate { (lightness, colorCode) ->
            lightness to RGBABytes.rgb(colorCode)
        }

    operator fun get(
        lightness: MaterialLightness,
    ): RGBABytes = values[lightness]
        ?: throw IllegalArgumentException("There is no $lightness lightness for $hue hue")

    val v50 get() = get(MaterialLightness.V50)
    val v100 get() = get(MaterialLightness.V100)
    val v200 get() = get(MaterialLightness.V200)
    val v300 get() = get(MaterialLightness.V300)
    val v400 get() = get(MaterialLightness.V400)
    val v500 get() = get(MaterialLightness.V500)
    val v600 get() = get(MaterialLightness.V600)
    val v700 get() = get(MaterialLightness.V700)
    val v800 get() = get(MaterialLightness.V800)
    val v900 get() = get(MaterialLightness.V900)
    val a100 get() = get(MaterialLightness.A100)
    val a200 get() = get(MaterialLightness.A200)
    val a400 get() = get(MaterialLightness.A400)
    val a700 get() = get(MaterialLightness.A700)
}