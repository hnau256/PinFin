package hnau.common.app

fun String.toEditingString(): EditingString =
    EditingString(text = this)
