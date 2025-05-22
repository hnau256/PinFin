package hnau.pinfin.app

import hnau.common.model.goback.GlobalGoBackHandler
import hnau.pinfin.projector.RootProjector
import hnau.pinfin.projector.RootProjectorDependenciesImpl

actual fun createRootProjectorDependencies(
    globalGoBackHandler: GlobalGoBackHandler,
): RootProjector.Dependencies = RootProjectorDependenciesImpl(
    globalGoBackHandler = globalGoBackHandler,
)