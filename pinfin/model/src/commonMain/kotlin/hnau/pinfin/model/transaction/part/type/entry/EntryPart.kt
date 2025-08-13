package hnau.pinfin.model.transaction.part.type.entry

import hnau.common.gen.enumvalues.annotations.EnumValues

@EnumValues
enum class EntryPart {
    Records,
    Account;

    companion object {

        val default: EntryPart = Records
    }
}