package hnau.pinfin.app

import hnau.common.app.model.app.AppModel
import hnau.common.app.projector.app.AppProjector
import hnau.pinfin.model.RootModel
import hnau.pinfin.projector.RootProjector
import hnau.pinfin.projector.impl
import kotlinx.coroutines.CoroutineScope

fun createAppProjector(
    scope: CoroutineScope,
    model: AppModel<RootModel, RootModel.Skeleton>,
): AppProjector<RootModel, RootModel.Skeleton, RootProjector> = AppProjector(
    scope = scope,
    model = model,
    createProjector = { scope, model, globalGoBackHandler ->
        RootProjector(
            scope = scope,
            model = model,
            dependencies = RootProjector.Dependencies.impl(
                globalGoBackHandler = globalGoBackHandler,
            ),
        )
    },
    content = { rootProjector ->
        rootProjector.Content()
    }
)