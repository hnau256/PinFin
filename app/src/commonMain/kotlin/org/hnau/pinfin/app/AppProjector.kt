package org.hnau.pinfin.app

import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.model.app.AppModel
import org.hnau.commons.app.model.theme.ThemeBrightness
import org.hnau.commons.app.model.theme.color.Hue
import org.hnau.commons.app.model.theme.palette.SystemPalettes
import org.hnau.commons.app.projector.app.AppProjector
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.RootProjector
import org.hnau.pinfin.projector.impl

fun createAppProjector(
    scope: CoroutineScope,
    model: AppModel<RootModel, RootModel.Skeleton>,
    createSystemPalettes: (ThemeBrightness) -> SystemPalettes,
): AppProjector<RootModel, RootModel.Skeleton, RootProjector> = AppProjector(
    scope = scope,
    model = model,
    createProjector = { scope, model ->
        RootProjector(
            scope = scope,
            model = model,
            dependencies = RootProjector.Dependencies.impl(
                localization = Localization.default,
                currency = Currency.default, //TODO
            ),
        )
    },
    createSystemPalettes = createSystemPalettes,
    fallbackHue = Hue(240),
    content = { rootProjector, contentPadding ->
        rootProjector.Content(
            contentPadding = contentPadding,
        )
    }
)