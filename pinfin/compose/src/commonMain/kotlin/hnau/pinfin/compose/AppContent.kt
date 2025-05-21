package hnau.pinfin.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import arrow.core.Either
import hnau.common.model.color.material.MaterialHue
import hnau.common.model.goback.GlobalGoBackHandlerImpl
import hnau.common.projector.uikit.HnauTheme
import hnau.common.projector.utils.DynamicColorsGenerator
import hnau.common.projector.utils.ThemeBrightness
import hnau.common.projector.utils.provideDynamicColorsGenerator
import hnau.pinfin.app.PinFinApp
import hnau.pinfin.projector.RootProjector

@Composable
fun PinFinApp.Content(
    dependencies: AppContentDependencies,
    forcedBrightness: ThemeBrightness? = null,
) {
    val projectorScope = rememberCoroutineScope()
    val projector = remember(model, dependencies) {
        RootProjector(
            scope = projectorScope,
            dependencies = dependencies.root(
                globalGoBackHandler = GlobalGoBackHandlerImpl(model.goBackHandler),
            ),
            model = model,
        )
    }
    val dynamicColorsGenerator: DynamicColorsGenerator? =
        remember { provideDynamicColorsGenerator() }
    HnauTheme(
        forcedBrightness = forcedBrightness,
        primaryHueOrDynamicColorsGenerator = /*when (dynamicColorsGenerator) {
            null -> Either.Left(MaterialHue.Teal)
            else -> Either.Right(dynamicColorsGenerator)
        }*/ //TODO config from settings
            Either.Left(MaterialHue.LightGreen)
    ) {
        projector.Content()
    }
}
