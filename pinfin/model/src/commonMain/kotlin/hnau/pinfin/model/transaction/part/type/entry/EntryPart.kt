package hnau.pinfin.model.transaction.part.type.entry

enum class EntryPart {
    Records,
    Account;

    companion object {

        val default: EntryPart = Records
    }
}