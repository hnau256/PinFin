package hnau.pinfin.model.transaction

import hnau.common.gen.enumvalues.annotations.EnumValues

@EnumValues(
    serializable = true,
)
enum class Part {
    Date,
    Time,
    Comment,

    Type,
}