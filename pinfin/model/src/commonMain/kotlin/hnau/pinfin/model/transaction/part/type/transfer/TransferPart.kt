package hnau.pinfin.model.transaction.part.type.transfer

import hnau.common.gen.enumvalues.annotations.EnumValues

@EnumValues
enum class TransferPart {
    From,
    To,
    Amount;

    companion object {

        val default: TransferPart = From
    }
}