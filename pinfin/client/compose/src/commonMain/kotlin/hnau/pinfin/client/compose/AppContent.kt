package hnau.pinfin.client.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import hnau.pinfin.client.app.PinFinApp
import hnau.pinfin.client.compose.utils.pinfinTheme
import hnau.pinfin.client.projector.init.InitProjector
import hnau.pinfin.client.projector.init.impl

@Composable
fun PinFinApp.Content() {
    val projectorScope = rememberCoroutineScope()
    val projector = remember {
        InitProjector(
                scope = projectorScope,
                dependencies = InitProjector.Dependencies.impl(),
                model = model,
            )
    }
    pinfinTheme {
        projector.Content()
    }
}
