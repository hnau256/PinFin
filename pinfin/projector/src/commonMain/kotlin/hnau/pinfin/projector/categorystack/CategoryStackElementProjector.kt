package hnau.pinfin.projector.categorystack

import androidx.compose.runtime.Composable
import hnau.pinfin.projector.IconProjector

sealed interface CategoryStackElementProjector {

    @Composable
    fun Content()

    val key: Int

    data class Info(
        private val projector: CategoryProjector,
    ) : CategoryStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 0
    }

    data class Icon(
        private val projector: IconProjector,
    ) : CategoryStackElementProjector {

        @Composable
        override fun Content() {
            projector.Content()
        }

        override val key: Int
            get() = 1
    }
}