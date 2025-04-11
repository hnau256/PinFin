package hnau.common.compose.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MappingKSerializer
import kotlinx.serialization.Serializable

object TextFieldValueSerializer :
    MappingKSerializer<TextFieldValueSerializer.Surrogate, TextFieldValue>(
        base = Surrogate.serializer(),
        mapper = Mapper(
            direct = { surrogate ->
                TextFieldValue(
                    text = surrogate.text,
                    selection = TextRange(
                        start = surrogate.selectionStart,
                        end = surrogate.selectionEnd,
                    )
                )
            },
            reverse = { textFieldValue ->
                Surrogate(
                    text = textFieldValue.text,
                    selectionStart = textFieldValue.selection.start,
                    selectionEnd = textFieldValue.selection.end,
                )
            }
        )
    ) {

    @Serializable
    data class Surrogate(
        val text: String,
        val selectionStart: Int,
        val selectionEnd: Int,
    )
}