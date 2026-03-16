package org.hnau.pinfin.model.budget.analytics.tab.graph.configure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.pinfin.model.utils.analytics.config.AnalyticsConfig

class GraphConfigureModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    val config: StateFlow<AnalyticsConfig>,
    updateConfig: (AnalyticsConfig) -> Unit,
) {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}