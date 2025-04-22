package hnau.pinfin.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import arrow.core.Either
import hnau.common.app.goback.GlobalGoBackHandlerImpl
import hnau.common.color.material.MaterialHue
import hnau.common.compose.uikit.HnauTheme
import hnau.common.compose.utils.ThemeBrightness
import hnau.pinfin.app.PinFinApp
import hnau.pinfin.projector.LoadBudgetProjector
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
