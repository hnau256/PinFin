package hnau.pinfin.projector

import hnau.common.model.goback.GlobalGoBackHandler

actual fun createRootProjectorDependencies(
    globalGoBackHandler: GlobalGoBackHandler,
): RootProjector.Dependencies = RootProjectorDependenciesImpl(
    globalGoBackHandler = globalGoBackHandler,
)