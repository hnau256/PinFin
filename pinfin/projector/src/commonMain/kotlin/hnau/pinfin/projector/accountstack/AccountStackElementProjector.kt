package hnau.pinfin.projector.accountstack

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.IconProjector

sealed interface AccountStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Info(
        private val projector: AccountProjector,
    ) : AccountStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Icon(
        private val projector: IconProjector,
    ) : AccountStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}