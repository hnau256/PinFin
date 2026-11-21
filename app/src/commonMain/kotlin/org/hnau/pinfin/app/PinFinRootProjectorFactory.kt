package org.hnau.pinfin.app

import org.hnau.commons.app.model.theme.color.Hue
import org.hnau.commons.app.projector.app.RootProjectorFactory
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.RootProjector
import org.hnau.pinfin.projector.impl

val PinFinRootProjectorFactory: RootProjectorFactory<RootModel, RootProjector> = RootProjectorFactory(
    fallbackHue = Hue(300),
    createRootProjector = { scope, model ->
        RootProjector(
            scope = scope,
            model = model,
            dependencies = RootProjector.Dependencies.impl(
                localization = Localization.default,
            ),
        )
    },
    content = { rootProjector, contentPadding ->
        rootProjector.Content(
            contentPadding = contentPadding,
        )
    },
)
