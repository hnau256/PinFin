package hnau.pinfin.model.transaction.type

import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.pinfin.data.Transaction
import hnau.pinfin.data.TransactionType
import hnau.pinfin.model.transaction.type.entry.EntryModel
import hnau.pinfin.model.transaction.type.transfer.TransferModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface TransactionTypeModel : GoBackHandlerProvider {

    val key: Int

    val result: StateFlow<Transaction.Type?>

    val type: TransactionType

    data class Entry(
        val model: EntryModel,
    ) : TransactionTypeModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 0

        override val result: StateFlow<Transaction.Type?>
            get() = model.result

        override val type: TransactionType
            get() = TransactionType.Entry
    }

    data class Transfer(
        val model: TransferModel,
    ) : TransactionTypeModel, GoBackHandlerProvider by model {

        override val key: Int
            get() = 1

        override val result: StateFlow<Transaction.Type?>
            get() = model.result

        override val type: TransactionType
            get() = TransactionType.Transfer
    }

    @Serializable
    sealed interface Skeleton {

        val type: TransactionType

        @Serializable
        @SerialName("entry")
        data class Entry(
            val skeleton: EntryModel.Skeleton,
        ) : Skeleton {

            override val type: TransactionType
                get() = TransactionType.Entry
        }

        @Serializable
        @SerialName("transfer")
        data class Transfer(
            val skeleton: TransferModel.Skeleton,
        ) : Skeleton {

            override val type: TransactionType
                get() = TransactionType.Transfer
        }
    }
}
