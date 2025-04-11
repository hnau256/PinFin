package hnau.pinfin.client.model.transaction.type

import hnau.common.app.goback.GoBackHandlerProvider
import hnau.pinfin.client.model.transaction.type.entry.EntryModel
import hnau.pinfin.client.model.transaction.type.transfer.TransferModel
import hnau.pinfin.scheme.Transaction
import hnau.pinfin.scheme.TransactionType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface TransactionTypeModel : GoBackHandlerProvider {

    val key: Any?

    val result: StateFlow<Transaction.Type?>

    val type: TransactionType

    data class Entry(
        val model: EntryModel,
    ) : TransactionTypeModel, GoBackHandlerProvider by model {

        override val key: Any
            get() = 0

        override val result: StateFlow<Transaction.Type?>
            get() = model.result

        override val type: TransactionType
            get() = TransactionType.Entry
    }

    data class Transfer(
        val model: TransferModel,
    ) : TransactionTypeModel, GoBackHandlerProvider by model {

        override val key: Any
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
