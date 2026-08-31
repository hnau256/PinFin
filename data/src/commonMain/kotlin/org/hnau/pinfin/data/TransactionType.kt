package org.hnau.pinfin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hnau.commons.gen.enumvalues.annotations.EnumValues
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
@EnumValues(serializable = true)
@Serializable
enum class TransactionType {

    @SerialName("entry")
    Entry,

    @SerialName("transfer")
    Transfer;

    companion object {

        val default = Entry
    }
}