@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.sync

import arrow.core.left
import arrow.core.right
import arrow.core.toNonEmptyListOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.input.InputModel
import org.hnau.commons.app.model.input.InputParser
import org.hnau.commons.app.model.input.InputSkeleton
import org.hnau.commons.app.model.input.InputType
import org.hnau.commons.app.model.input.factory.InputModelFactory
import org.hnau.commons.app.model.input.factory.createModel
import org.hnau.commons.app.model.input.factory.createSkeleton
import org.hnau.commons.app.model.input.factory.type.createVariant
import org.hnau.commons.app.model.input.factory.type.editText
import org.hnau.commons.app.model.input.plus
import org.hnau.commons.app.model.utils.ModelSavableDelegate
import org.hnau.commons.app.model.utils.combineEditableWith
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

class BudgetSyncConfigModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
    close: () -> Unit,
    save: suspend (SyncConfig) -> Unit,
) {

    @Serializable
    data class Skeleton(
        val scheme: InputSkeleton<HttpScheme, HttpScheme>,
        val host: InputSkeleton<String, ServerHost>,
        val savableDelegate: ModelSavableDelegate.Skeleton<SyncConfig> = ModelSavableDelegate.Skeleton(),
    ) {

        companion object {

            fun create(
                config: SyncConfig,
            ): Skeleton = Skeleton(
                scheme = schemeInputFactory.createSkeleton(
                    value = config.scheme,
                    useValueAsInitial = true,
                ),
                host = hostInputFactory.createSkeleton(
                    value = config.host,
                    useValueAsInitial = true,
                ),
            )
        }
    }

    val scheme: InputModel<HttpScheme, Nothing, HttpScheme, InputType.Variant<HttpScheme>> =
        schemeInputFactory.createModel(
            scope = scope,
            skeleton = skeleton.scheme,
        )

    val host: InputModel<String, Unit, ServerHost, InputType.Edit> = hostInputFactory.createModel(
        scope = scope,
        skeleton = skeleton.host,
    )

    val savableDelegate: ModelSavableDelegate<SyncConfig> = ModelSavableDelegate(
        scope = scope,
        result = scheme.editable.combineEditableWith(
            scope = scope,
            other = host.editable,
            combine = ::SyncConfig,
        ),
        skeleton = skeleton.savableDelegate,
        modelGoBackHandler = NeverGoBackHandler,
        close = close,
        save = save,
    )

    val goBackHandler: GoBackHandler
        get() = savableDelegate.goBackHandler

    companion object {

        private val schemeInputFactory: InputModelFactory<HttpScheme, Nothing, HttpScheme, InputType.Variant<HttpScheme>> =
            InputModelFactory.createVariant(
                variants = HttpScheme.entries.toNonEmptyListOrThrow()
            )

        private val hostInputFactory: InputModelFactory<String, Unit, ServerHost, InputType.Edit> =
            InputModelFactory.editText(
                encoder = ServerHost::host,
                configParser = {
                    it + InputParser<String, Unit, ServerHost> { input ->
                        ServerHost
                            .createOrNull(input)
                            .foldNullable(
                                ifNull = { Unit.left() },
                                ifNotNull = ServerHost::right,
                            )
                    }
                }
            )
    }
}