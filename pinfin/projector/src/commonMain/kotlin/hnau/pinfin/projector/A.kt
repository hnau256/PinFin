package hnau.pinfin.projector

import hnau.common.model.goback.GlobalGoBackHandler

expect fun createRootProjectorDependencies(
    globalGoBackHandler: GlobalGoBackHandler,
): RootProjector.Dependencies