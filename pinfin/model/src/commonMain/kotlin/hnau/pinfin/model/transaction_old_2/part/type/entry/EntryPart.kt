package hnau.pinfin.model.transaction_old_2.part.type.entry

enum class EntryPart {
    Records,
    Account;

    companion object {

        val default: EntryPart = Records
    }
}