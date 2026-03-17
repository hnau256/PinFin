package org.hnau.pinfin.app

import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.model.app.AppModel
import org.hnau.commons.app.projector.app.AppProjector
import org.hnau.pinfin.model.RootModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.RootProjector
import org.hnau.pinfin.projector.impl

fun createAppProjector(
    scope: CoroutineScope,
    model: AppModel<RootModel, RootModel.Skeleton>,
): AppProjector<RootModel, RootModel.Skeleton, RootProjector> = AppProjector(
    scope = scope,
    model = model,
    createProjector = { scope, model ->
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
    }
)