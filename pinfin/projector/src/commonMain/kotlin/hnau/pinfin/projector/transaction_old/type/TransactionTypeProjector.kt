package hnau.pinfin.projector.transaction_old.type

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.transaction_old.type.entry.EntryProjector
import hnau.pinfin.projector.transaction_old.type.transfer.TransferProjector

sealed interface TransactionTypeProjector {

    @Composable
    fun Content()

    val key: Int

    data class Entry(
        private val projector: EntryProjector,
    ) : TransactionTypeProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Transfer(
        private val projector: TransferProjector,
    ) : TransactionTypeProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}