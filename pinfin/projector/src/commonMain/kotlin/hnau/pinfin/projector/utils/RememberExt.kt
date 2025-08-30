package hnau.pinfin.projector.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

//TODO extract to common-app

@Composable
inline fun <I, O> I.rememberRun(
    vararg additionalKeys: Any,
    crossinline block: I.() -> O,
): O = remember(this, *additionalKeys) {
    block()
}

@Composable
inline fun <I, O> I.rememberLet(
    vararg additionalKeys: Any,
    crossinline block: (I) -> O,
): O = remember(this, *additionalKeys) {
    block(this)
}