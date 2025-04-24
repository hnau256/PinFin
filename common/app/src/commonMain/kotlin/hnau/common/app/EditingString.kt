package hnau.common.app

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable(EditingString.Serializer::class)
data class EditingString(
    val text: String = "",
    val selection: IntRange = text.defaultSelection,
) {

    object Serializer : MappingKSerializer<String, EditingString>(
        base = String.serializer(),
        mapper = stringMapper,
    )

    companion object {

        val empty: EditingString = EditingString()

        private const val selectionTextSeparator = '|'
        private const val selectionPartsSeparator = '-'

        private fun IntRange(
            index: Int,
        ): IntRange = IntRange(
            start = index,
            endInclusive = index,
        )

        private val String.defaultSelectionIndex
            get() = length

        private val String.defaultSelection
            get() = IntRange(defaultSelectionIndex)

        private val stringMapper = Mapper<String, EditingString>(
            direct = { string ->
                val textSelectionSeparatorIndex = string.indexOf(selectionTextSeparator)
                val text = string.substring(textSelectionSeparatorIndex + 1)
                EditingString(
                    text = text,
                    selection = string
                        .substring(
                            startIndex = 0,
                            endIndex = textSelectionSeparatorIndex.coerceAtLeast(0),
                        )
                        .split(selectionPartsSeparator)
                        .filter(String::isNotEmpty)
                        .map(String::toInt)
                        .let { selectionParts ->
                            when (selectionParts.size) {
                                0 -> text.defaultSelection
                                1 -> IntRange(selectionParts[0])
                                2 -> IntRange(selectionParts[0], selectionParts[1])
                                else -> error("EditingString.selection must contains at most 2 numbers. Got $selectionParts")
                            }
                        },
                )
            },
            reverse = { (text, selection) ->
                val selectionEncoded = run {
                    val first = selection.first
                    val last = selection.last
                    val parts = when {
                        first != last -> listOf(first, last)
                        first != text.defaultSelectionIndex -> listOf(first)
                        else -> emptyList()
                    }
                    parts.joinToString(separator = selectionPartsSeparator.toString())
                }
                "$selectionEncoded$selectionTextSeparator$text"
            },
        )
    }
}
