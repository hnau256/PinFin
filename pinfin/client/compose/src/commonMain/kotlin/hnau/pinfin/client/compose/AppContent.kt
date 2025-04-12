package hnau.pinfin.client.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import arrow.core.Either
import hnau.common.color.material.MaterialHue
import hnau.common.compose.uikit.HnauTheme
import hnau.common.compose.utils.ThemeBrightness
import hnau.pinfin.client.app.PinFinApp
import hnau.pinfin.client.projector.InitProjector

@Composable
fun PinFinApp.Content(
    dependencies: AppContentDependencies,
    forcedBrightness: ThemeBrightness? = null,
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
        forcedBrightness = forcedBrightness,
        primaryHueOrDynamicColorsGenerator = when (val generator =
            dependencies.dynamicColorsGenerator) {
            null -> Either.Left(MaterialHue.Teal)
            else -> Either.Right(generator)
        }
    ) {
        projector.Content()
    }
}
