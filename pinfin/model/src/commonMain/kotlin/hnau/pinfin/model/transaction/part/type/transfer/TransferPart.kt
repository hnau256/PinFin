package hnau.pinfin.model.transaction.part.type.transfer

enum class TransferPart {
    From,
    To,
    Amount;

    companion object {

        val default: TransferPart = From
    }
}