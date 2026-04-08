@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.sync

import arrow.core.None
import arrow.core.some
import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.EditingString
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.toEditingString
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.utils.ModelSavableDelegate
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

class BudgetSyncConfigModel(
    scope: CoroutineScope,
    private val skeleton: Skeleton,
    close: () -> Unit,
    save: suspend (SyncConfig) -> Unit,
) {

    @Serializable
    data class Skeleton(
        val initialConfig: SyncConfig?,
        val scheme: MutableStateFlow<HttpScheme>,
        val host: MutableStateFlow<EditingString>,
        val modelSavableDelegate: ModelSavableDelegate.Skeleton<SyncConfig> = ModelSavableDelegate.Skeleton(),
    ) {

        companion object {

            private fun create(
                initialConfig: SyncConfig?,
                scheme: HttpScheme,
                host: String,
            ): Skeleton = Skeleton(
                initialConfig = initialConfig,
                scheme = scheme.toMutableStateFlowAsInitial(),
                host = host.toEditingString().toMutableStateFlowAsInitial(),
            )

            fun createForNew(): Skeleton = create(
                initialConfig = null,
                scheme = HttpScheme.default,
                host = "",
            )

            fun createForConfig(
                config: SyncConfig,
            ): Skeleton = create(
                initialConfig = config,
                scheme = config.scheme,
                host = config.host.host,
            )
        }
    }

    val scheme: MutableStateFlow<HttpScheme>
        get() = skeleton.scheme

    val hostInput: MutableStateFlow<EditingString>
        get() = skeleton.host

    private val hostOrNull: StateFlow<ServerHost?> = hostInput.mapState(
        scope = scope,
    ) { input ->
        ServerHost.createOrNull(input.text)
    }

    val hostIsCorrect: StateFlow<Boolean> =
        hostOrNull.mapState(scope) { it != null }

    private val config: StateFlow<Editable<SyncConfig>> = Editable.create(
        scope = scope,
        initialValueOrNone = skeleton.initialConfig.toOption(),
        valueOrNone = hostOrNull.flatMapWithScope(scope) { scope, addressOrNull ->
            addressOrNull.foldNullable(
                ifNull = { None.toMutableStateFlowAsInitial() },
                ifNotNull = { host ->
                    scheme.mapState(scope) { scheme ->
                        SyncConfig(
                            scheme = scheme,
                            host = host,
                        ).some()
                    }
                }
            )
        }
    )

    val savableDelegate: ModelSavableDelegate<SyncConfig> = ModelSavableDelegate(
        scope = scope,
        result = config,
        skeleton = skeleton.modelSavableDelegate,
        modelGoBackHandler = NeverGoBackHandler,
        save = save,
        close = close,
    )

    val goBackHandler: GoBackHandler
        get() = savableDelegate.goBackHandler
}