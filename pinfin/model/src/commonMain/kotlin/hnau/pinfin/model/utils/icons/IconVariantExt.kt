//This file is generated

package hnau.pinfin.model.utils.icons

import hnau.pinfin.data.Icon

val IconVariant.icon: Icon
    get() = Icon(name)

private val variantsByIcons: Map<Icon, IconVariant> =
    IconVariant.entries.associateBy { Icon(key = it.name) }

val Icon.variant: IconVariant?
    get() = variantsByIcons.getValue(this)