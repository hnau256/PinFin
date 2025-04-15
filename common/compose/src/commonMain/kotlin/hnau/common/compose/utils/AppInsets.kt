package hnau.common.compose.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

interface AppInsets {

    @get:Composable
    val insets: PaddingValues

    companion object {

        val empty: AppInsets = object : AppInsets {
            override val insets: PaddingValues
                @Composable
                get() = PaddingValues()

        }
    }

}