package hnau.pinfin.model.transaction_old_2.part.type.transfer

enum class TransferPart {
    From,
    To,
    Amount;

    companion object {

        val default: TransferPart = From
    }
}