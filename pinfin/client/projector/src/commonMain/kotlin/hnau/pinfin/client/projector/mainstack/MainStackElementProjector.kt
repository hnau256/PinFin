package hnau.pinfin.client.projector.mainstack

import androidx.compose.runtime.Composable
import hnau.pinfin.client.projector.main.MainProjector
import hnau.pinfin.client.projector.transaction.TransactionProjector

sealed interface MainStackElementProjector {

    @Composable
    fun Content()

    val key: Any?

    data class Main(
        private val projector: MainProjector,
    ) : MainStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Any
            get() = 0
    }

    data class Transaction(
        private val projector: TransactionProjector,
    ) : MainStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Any
            get() = 1
    }
}