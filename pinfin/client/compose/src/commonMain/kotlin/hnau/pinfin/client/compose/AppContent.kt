package hnau.pinfin.client.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import arrow.core.Either
import hnau.common.color.material.MaterialHue
import hnau.common.compose.uikit.HnauTheme
import hnau.pinfin.client.app.PinFinApp
import hnau.pinfin.client.projector.InitProjector

@Composable
fun PinFinApp.Content(
    dependencies: AppContentDependencies,
) {
    val projectorScope = rememberCoroutineScope()
    val projector = remember(model, dependencies) {
        InitProjector(
            scope = projectorScope,
            dependencies = dependencies.init(),
            model = model,
        )
    }
    HnauTheme(
        primaryHueOrDynamicColorsGenerator = when (val generator =
            dependencies.dynamicColorsGenerator) {
            null -> Either.Left(MaterialHue.Blue)
            else -> Either.Right(generator)
        }
    ) {
        projector.Content()
    }
}
