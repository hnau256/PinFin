package hnau.common.color.material

import hnau.common.color.RGBABytes

object MaterialColors

private val colors: Map<MaterialHue, MaterialList> = listOf(
    MaterialList(
        MaterialHue.Red,
        MaterialLightness.V50 to 0xffebee,
        MaterialLightness.V100 to 0xffcdd2,
        MaterialLightness.V200 to 0xef9a9a,
        MaterialLightness.V300 to 0xe57373,
        MaterialLightness.V400 to 0xef5350,
        MaterialLightness.V500 to 0xf44336,
        MaterialLightness.V600 to 0xe53935,
        MaterialLightness.V700 to 0xd32f2f,
        MaterialLightness.V800 to 0xc62828,
        MaterialLightness.V900 to 0xb71c1c,
        MaterialLightness.A100 to 0xff8a80,
        MaterialLightness.A200 to 0xff5252,
        MaterialLightness.A400 to 0xff1744,
        MaterialLightness.A700 to 0xd50000,
    ),

    MaterialList(
        MaterialHue.Pink,
        MaterialLightness.V50 to 0xfce4ec,
        MaterialLightness.V100 to 0xf8bbd0,
        MaterialLightness.V200 to 0xf48fb1,
        MaterialLightness.V300 to 0xf06292,
        MaterialLightness.V400 to 0xec407a,
        MaterialLightness.V500 to 0xe91e63,
        MaterialLightness.V600 to 0xd81b60,
        MaterialLightness.V700 to 0xc2185b,
        MaterialLightness.V800 to 0xad1457,
        MaterialLightness.V900 to 0x880e4f,
        MaterialLightness.A100 to 0xff80ab,
        MaterialLightness.A200 to 0xff4081,
        MaterialLightness.A400 to 0xf50057,
        MaterialLightness.A700 to 0xc51162,
    ),

    MaterialList(
        MaterialHue.Purple,
        MaterialLightness.V50 to 0xf3e5f5,
        MaterialLightness.V100 to 0xe1bee7,
        MaterialLightness.V200 to 0xce93d8,
        MaterialLightness.V300 to 0xba68c8,
        MaterialLightness.V400 to 0xab47bc,
        MaterialLightness.V500 to 0x9c27b0,
        MaterialLightness.V600 to 0x8e24aa,
        MaterialLightness.V700 to 0x7b1fa2,
        MaterialLightness.V800 to 0x6a1b9a,
        MaterialLightness.V900 to 0x4a148c,
        MaterialLightness.A100 to 0xea80fc,
        MaterialLightness.A200 to 0xe040fb,
        MaterialLightness.A400 to 0xd500f9,
        MaterialLightness.A700 to 0xaa00ff,
    ),

    MaterialList(
        MaterialHue.DeepPurple,
        MaterialLightness.V50 to 0xede7f6,
        MaterialLightness.V100 to 0xd1c4e9,
        MaterialLightness.V200 to 0xb39ddb,
        MaterialLightness.V300 to 0x9575cd,
        MaterialLightness.V400 to 0x7e57c2,
        MaterialLightness.V500 to 0x673ab7,
        MaterialLightness.V600 to 0x5e35b1,
        MaterialLightness.V700 to 0x512da8,
        MaterialLightness.V800 to 0x4527a0,
        MaterialLightness.V900 to 0x311b92,
        MaterialLightness.A100 to 0xb388ff,
        MaterialLightness.A200 to 0x7c4dff,
        MaterialLightness.A400 to 0x651fff,
        MaterialLightness.A700 to 0x6200ea,
    ),

    MaterialList(
        MaterialHue.Indigo,
        MaterialLightness.V50 to 0xe8eaf6,
        MaterialLightness.V100 to 0xc5cae9,
        MaterialLightness.V200 to 0x9fa8da,
        MaterialLightness.V300 to 0x7986cb,
        MaterialLightness.V400 to 0x5c6bc0,
        MaterialLightness.V500 to 0x3f51b5,
        MaterialLightness.V600 to 0x3949ab,
        MaterialLightness.V700 to 0x303f9f,
        MaterialLightness.V800 to 0x283593,
        MaterialLightness.V900 to 0x1a237e,
        MaterialLightness.A100 to 0x8c9eff,
        MaterialLightness.A200 to 0x536dfe,
        MaterialLightness.A400 to 0x3d5afe,
        MaterialLightness.A700 to 0x304ffe,
    ),

    MaterialList(
        MaterialHue.Blue,
        MaterialLightness.V50 to 0xe3f2fd,
        MaterialLightness.V100 to 0xbbdefb,
        MaterialLightness.V200 to 0x90caf9,
        MaterialLightness.V300 to 0x64b5f6,
        MaterialLightness.V400 to 0x42a5f5,
        MaterialLightness.V500 to 0x2196f3,
        MaterialLightness.V600 to 0x1e88e5,
        MaterialLightness.V700 to 0x1976d2,
        MaterialLightness.V800 to 0x1565c0,
        MaterialLightness.V900 to 0x0d47a1,
        MaterialLightness.A100 to 0x82b1ff,
        MaterialLightness.A200 to 0x448aff,
        MaterialLightness.A400 to 0x2979ff,
        MaterialLightness.A700 to 0x2962ff,
    ),

    MaterialList(
        MaterialHue.LightBlue,
        MaterialLightness.V50 to 0xe1f5fe,
        MaterialLightness.V100 to 0xb3e5fc,
        MaterialLightness.V200 to 0x81d4fa,
        MaterialLightness.V300 to 0x4fc3f7,
        MaterialLightness.V400 to 0x29b6f6,
        MaterialLightness.V500 to 0x03a9f4,
        MaterialLightness.V600 to 0x039be5,
        MaterialLightness.V700 to 0x0288d1,
        MaterialLightness.V800 to 0x0277bd,
        MaterialLightness.V900 to 0x01579b,
        MaterialLightness.A100 to 0x80d8ff,
        MaterialLightness.A200 to 0x40c4ff,
        MaterialLightness.A400 to 0x00b0ff,
        MaterialLightness.A700 to 0x0091ea,
    ),

    MaterialList(
        MaterialHue.Cyan,
        MaterialLightness.V50 to 0xe0f7fa,
        MaterialLightness.V100 to 0xb2ebf2,
        MaterialLightness.V200 to 0x80deea,
        MaterialLightness.V300 to 0x4dd0e1,
        MaterialLightness.V400 to 0x26c6da,
        MaterialLightness.V500 to 0x00bcd4,
        MaterialLightness.V600 to 0x00acc1,
        MaterialLightness.V700 to 0x0097a7,
        MaterialLightness.V800 to 0x00838f,
        MaterialLightness.V900 to 0x006064,
        MaterialLightness.A100 to 0x84ffff,
        MaterialLightness.A200 to 0x18ffff,
        MaterialLightness.A400 to 0x00e5ff,
        MaterialLightness.A700 to 0x00b8d4,
    ),

    MaterialList(
        MaterialHue.Teal,
        MaterialLightness.V50 to 0xe0f2f1,
        MaterialLightness.V100 to 0xb2dfdb,
        MaterialLightness.V200 to 0x80cbc4,
        MaterialLightness.V300 to 0x4db6ac,
        MaterialLightness.V400 to 0x26a69a,
        MaterialLightness.V500 to 0x009688,
        MaterialLightness.V600 to 0x00897b,
        MaterialLightness.V700 to 0x00796b,
        MaterialLightness.V800 to 0x00695c,
        MaterialLightness.V900 to 0x004d40,
        MaterialLightness.A100 to 0xa7ffeb,
        MaterialLightness.A200 to 0x64ffda,
        MaterialLightness.A400 to 0x1de9b6,
        MaterialLightness.A700 to 0x00bfa5,
    ),

    MaterialList(
        MaterialHue.Green,
        MaterialLightness.V50 to 0xe8f5e9,
        MaterialLightness.V100 to 0xc8e6c9,
        MaterialLightness.V200 to 0xa5d6a7,
        MaterialLightness.V300 to 0x81c784,
        MaterialLightness.V400 to 0x66bb6a,
        MaterialLightness.V500 to 0x4caf50,
        MaterialLightness.V600 to 0x43a047,
        MaterialLightness.V700 to 0x388e3c,
        MaterialLightness.V800 to 0x2e7d32,
        MaterialLightness.V900 to 0x1b5e20,
        MaterialLightness.A100 to 0xb9f6ca,
        MaterialLightness.A200 to 0x69f0ae,
        MaterialLightness.A400 to 0x00e676,
        MaterialLightness.A700 to 0x00c853,
    ),

    MaterialList(
        MaterialHue.LightGreen,
        MaterialLightness.V50 to 0xf1f8e9,
        MaterialLightness.V100 to 0xdcedc8,
        MaterialLightness.V200 to 0xc5e1a5,
        MaterialLightness.V300 to 0xaed581,
        MaterialLightness.V400 to 0x9ccc65,
        MaterialLightness.V500 to 0x8bc34a,
        MaterialLightness.V600 to 0x7cb342,
        MaterialLightness.V700 to 0x689f38,
        MaterialLightness.V800 to 0x558b2f,
        MaterialLightness.V900 to 0x33691e,
        MaterialLightness.A100 to 0xccff90,
        MaterialLightness.A200 to 0xb2ff59,
        MaterialLightness.A400 to 0x76ff03,
        MaterialLightness.A700 to 0x64dd17,
    ),

    MaterialList(
        MaterialHue.Lime,
        MaterialLightness.V50 to 0xf9fbe7,
        MaterialLightness.V100 to 0xf0f4c3,
        MaterialLightness.V200 to 0xe6ee9c,
        MaterialLightness.V300 to 0xdce775,
        MaterialLightness.V400 to 0xd4e157,
        MaterialLightness.V500 to 0xcddc39,
        MaterialLightness.V600 to 0xc0ca33,
        MaterialLightness.V700 to 0xafb42b,
        MaterialLightness.V800 to 0x9e9d24,
        MaterialLightness.V900 to 0x827717,
        MaterialLightness.A100 to 0xf4ff81,
        MaterialLightness.A200 to 0xeeff41,
        MaterialLightness.A400 to 0xc6ff00,
        MaterialLightness.A700 to 0xaeea00,
    ),

    MaterialList(
        MaterialHue.Yellow,
        MaterialLightness.V50 to 0xfffde7,
        MaterialLightness.V100 to 0xfff9c4,
        MaterialLightness.V200 to 0xfff59d,
        MaterialLightness.V300 to 0xfff176,
        MaterialLightness.V400 to 0xffee58,
        MaterialLightness.V500 to 0xffeb3b,
        MaterialLightness.V600 to 0xfdd835,
        MaterialLightness.V700 to 0xfbc02d,
        MaterialLightness.V800 to 0xf9a825,
        MaterialLightness.V900 to 0xf57f17,
        MaterialLightness.A100 to 0xffff8d,
        MaterialLightness.A200 to 0xffff00,
        MaterialLightness.A400 to 0xffea00,
        MaterialLightness.A700 to 0xffd600,
    ),

    MaterialList(
        MaterialHue.Amber,
        MaterialLightness.V50 to 0xfff8e1,
        MaterialLightness.V100 to 0xffecb3,
        MaterialLightness.V200 to 0xffe082,
        MaterialLightness.V300 to 0xffd54f,
        MaterialLightness.V400 to 0xffca28,
        MaterialLightness.V500 to 0xffc107,
        MaterialLightness.V600 to 0xffb300,
        MaterialLightness.V700 to 0xffa000,
        MaterialLightness.V800 to 0xff8f00,
        MaterialLightness.V900 to 0xff6f00,
        MaterialLightness.A100 to 0xffe57f,
        MaterialLightness.A200 to 0xffd740,
        MaterialLightness.A400 to 0xffc400,
        MaterialLightness.A700 to 0xffab00,
    ),

    MaterialList(
        MaterialHue.Orange,
        MaterialLightness.V50 to 0xfff3e0,
        MaterialLightness.V100 to 0xffe0b2,
        MaterialLightness.V200 to 0xffcc80,
        MaterialLightness.V300 to 0xffb74d,
        MaterialLightness.V400 to 0xffa726,
        MaterialLightness.V500 to 0xff9800,
        MaterialLightness.V600 to 0xfb8c00,
        MaterialLightness.V700 to 0xf57c00,
        MaterialLightness.V800 to 0xef6c00,
        MaterialLightness.V900 to 0xe65100,
        MaterialLightness.A100 to 0xffd180,
        MaterialLightness.A200 to 0xffab40,
        MaterialLightness.A400 to 0xff9100,
        MaterialLightness.A700 to 0xff6d00,
    ),

    MaterialList(
        MaterialHue.DeepOrange,
        MaterialLightness.V50 to 0xfbe9e7,
        MaterialLightness.V100 to 0xffccbc,
        MaterialLightness.V200 to 0xffab91,
        MaterialLightness.V300 to 0xff8a65,
        MaterialLightness.V400 to 0xff7043,
        MaterialLightness.V500 to 0xff5722,
        MaterialLightness.V600 to 0xf4511e,
        MaterialLightness.V700 to 0xe64a19,
        MaterialLightness.V800 to 0xd84315,
        MaterialLightness.V900 to 0xbf360c,
        MaterialLightness.A100 to 0xff9e80,
        MaterialLightness.A200 to 0xff6e40,
        MaterialLightness.A400 to 0xff3d00,
        MaterialLightness.A700 to 0xdd2c00,
    ),

    MaterialList(
        MaterialHue.Brown,
        MaterialLightness.V50 to 0xefebe9,
        MaterialLightness.V100 to 0xd7ccc8,
        MaterialLightness.V200 to 0xbcaaa4,
        MaterialLightness.V300 to 0xa1887f,
        MaterialLightness.V400 to 0x8d6e63,
        MaterialLightness.V500 to 0x795548,
        MaterialLightness.V600 to 0x6d4c41,
        MaterialLightness.V700 to 0x5d4037,
        MaterialLightness.V800 to 0x4e342e,
        MaterialLightness.V900 to 0x3e2723,
    ),

    MaterialList(
        MaterialHue.Grey,
        MaterialLightness.V50 to 0xfafafa,
        MaterialLightness.V100 to 0xf5f5f5,
        MaterialLightness.V200 to 0xeeeeee,
        MaterialLightness.V300 to 0xe0e0e0,
        MaterialLightness.V400 to 0xbdbdbd,
        MaterialLightness.V500 to 0x9e9e9e,
        MaterialLightness.V600 to 0x757575,
        MaterialLightness.V700 to 0x616161,
        MaterialLightness.V800 to 0x424242,
        MaterialLightness.V900 to 0x212121,
    ),

    MaterialList(
        MaterialHue.BlueGrey,
        MaterialLightness.V50 to 0xeceff1,
        MaterialLightness.V100 to 0xcfd8dc,
        MaterialLightness.V200 to 0xb0bec5,
        MaterialLightness.V300 to 0x90a4ae,
        MaterialLightness.V400 to 0x78909c,
        MaterialLightness.V500 to 0x607d8b,
        MaterialLightness.V600 to 0x546e7a,
        MaterialLightness.V700 to 0x455a64,
        MaterialLightness.V800 to 0x37474f,
        MaterialLightness.V900 to 0x263238,
    ),

    ).associateBy { materialList -> materialList.hue }


@Suppress("UnusedReceiverParameter")
val MaterialColors.values: Map<MaterialHue, MaterialList>
    get() = colors

operator fun MaterialColors.get(
    hue: MaterialHue,
): MaterialList = values.getValue(hue)


val MaterialColors.red: MaterialList
    get() = get(MaterialHue.Red)

val MaterialColors.pink: MaterialList
    get() = get(MaterialHue.Pink)

val MaterialColors.purple: MaterialList
    get() = get(MaterialHue.Purple)

val MaterialColors.deepPurple: MaterialList
    get() = get(MaterialHue.DeepPurple)

val MaterialColors.indigo: MaterialList
    get() = get(MaterialHue.Indigo)

val MaterialColors.blue: MaterialList
    get() = get(MaterialHue.Blue)

val MaterialColors.lightBlue: MaterialList
    get() = get(MaterialHue.LightBlue)

val MaterialColors.cyan: MaterialList
    get() = get(MaterialHue.Cyan)

val MaterialColors.teal: MaterialList
    get() = get(MaterialHue.Teal)

val MaterialColors.green: MaterialList
    get() = get(MaterialHue.Green)

val MaterialColors.lightGreen: MaterialList
    get() = get(MaterialHue.LightGreen)

val MaterialColors.lime: MaterialList
    get() = get(MaterialHue.Lime)

val MaterialColors.yellow: MaterialList
    get() = get(MaterialHue.Yellow)

val MaterialColors.amber: MaterialList
    get() = get(MaterialHue.Amber)

val MaterialColors.orange: MaterialList
    get() = get(MaterialHue.Orange)

val MaterialColors.deepOrange: MaterialList
    get() = get(MaterialHue.DeepOrange)

val MaterialColors.brown: MaterialList
    get() = get(MaterialHue.Brown)

val MaterialColors.grey: MaterialList
    get() = get(MaterialHue.Grey)

val MaterialColors.blueGrey: MaterialList
    get() = get(MaterialHue.BlueGrey)

val RGBABytes.Companion.material: MaterialColors
    get() = MaterialColors
