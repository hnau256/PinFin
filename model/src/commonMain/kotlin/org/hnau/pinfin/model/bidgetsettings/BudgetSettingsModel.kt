package org.hnau.pinfin.model.bidgetsettings

import arrow.core.left
import arrow.core.right
import arrow.core.toNonEmptyListOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.input.InputModel
import org.hnau.commons.app.model.input.InputSkeleton
import org.hnau.commons.app.model.input.InputType
import org.hnau.commons.app.model.input.factory.InputModelFactory
import org.hnau.commons.app.model.input.factory.createModel
import org.hnau.commons.app.model.input.factory.createSkeleton
import org.hnau.commons.app.model.input.factory.toInputModelFactory
import org.hnau.commons.app.model.input.parser.ParsingMapper
import org.hnau.commons.app.model.input.parser.createValidator
import org.hnau.commons.app.model.utils.ModelSavableDelegate
import org.hnau.commons.app.model.utils.combineEditableWith
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.foldNullable
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.utils.DecimalScale
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.upchain.sync.core.ServerHost
import org.hnau.upchain.sync.http.HttpScheme

class BudgetSettingsModel(
    scope: CoroutineScope,
    skeleton: Skeleton,
    dependencies: Dependencies,
    close: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val main: Main,
        val sync: Sync,
        val savableDelegate: ModelSavableDelegate.Skeleton<BudgetInfo> = ModelSavableDelegate.Skeleton(),
    ) {

        @Serializable
        data class Main(
            val title: InputSkeleton<String, String>,
            val mantissaLength: InputSkeleton<String, DecimalScale>,
        ) {

            companion object {

                fun create(
                    info: BudgetInfo,
                ): Main = Main(
                    title = mainTitleInputFactory.createSkeleton(
                        value = info.title,
                        useValueAsInitial = true,
                    ),
                    mantissaLength = mainMantissaLengthInputFactory.createSkeleton(
                        value = info.currency.scale,
                        useValueAsInitial = true,
                    ),
                )
            }
        }

        @Serializable
        data class Sync(
            val scheme: InputSkeleton<HttpScheme, HttpScheme>,
            val host: InputSkeleton<String, ServerHost>,
        ) {

            companion object {

                fun create(
                    info: BudgetInfo.Sync,
                ): Sync = Sync(
                    scheme = syncSchemeInputFactory.createSkeleton(
                        value = info.scheme,
                        useValueAsInitial = true,
                    ),
                    host = syncHostInputFactory.createSkeleton(
                        value = info.host,
                        useValueAsInitial = true,
                    ),
                )
            }
        }

        companion object {

            fun create(
                info: BudgetInfo,
            ): Skeleton = Skeleton(
                main = Main.create(
                    info = info,
                ),
                sync = Sync.create(
                    info = info.sync,
                )
            )
        }
    }

    val mainTitle: InputModel<String, String, Unit, InputType.Edit> =
        mainTitleInputFactory.createModel(
            scope = scope,
            skeleton = skeleton.main.title,
        )

    val mainMantissaLength: InputModel<String, DecimalScale, Unit, InputType.Edit> =
        mainMantissaLengthInputFactory.createModel(
            scope = scope,
            skeleton = skeleton.main.mantissaLength,
        )

    val syncScheme: InputModel<HttpScheme, HttpScheme, Nothing, InputType.Variant<HttpScheme>> =
        syncSchemeInputFactory.createModel(
            scope = scope,
            skeleton = skeleton.sync.scheme,
        )

    val syncHost: InputModel<String, ServerHost, Unit, InputType.Edit> =
        syncHostInputFactory.createModel(
            scope = scope,
            skeleton = skeleton.sync.host,
        )

    val savableDelegate: ModelSavableDelegate<BudgetInfo> = ModelSavableDelegate(
        scope = scope,
        result = mainTitle.editable
            .combineEditableWith(
                scope = scope,
                other = mainMantissaLength.editable,
                combine = ::Pair,
            )
            .combineEditableWith(
                scope = scope,
                other = syncScheme.editable
                    .combineEditableWith(
                        scope = scope,
                        other = syncHost.editable,
                        combine = BudgetInfo::Sync,
                    )
            ) { (title, mantissaLength), sync ->
                BudgetInfo(
                    title = title,
                    currency = Currency(
                        scale = mantissaLength,
                    ),
                    sync = sync,
                )
            },
        skeleton = skeleton.savableDelegate,
        modelGoBackHandler = NeverGoBackHandler,
        close = close,
        save = { budgetInfo ->
            dependencies
                .budgetRepository
                .config(
                    BudgetConfig(
                        title = budgetInfo.title,
                        currency = budgetInfo.currency,
                        sync = BudgetConfig.Sync(
                            scheme = budgetInfo.sync.scheme,
                            host = budgetInfo.sync.host,
                        )
                    )
                )
        },
    )

    val goBackHandler: GoBackHandler
        get() = savableDelegate.goBackHandler

    companion object {

        private val mainTitleInputFactory: InputModelFactory<String, String, Unit, InputType.Edit> =
            InputType.Edit.toInputModelFactory(
                ParsingMapper.createValidator { string ->
                    string
                        .takeIf(String::isNotEmpty)
                        .foldNullable(
                            ifNull = { Unit.left() },
                            ifNotNull = { Unit.right() }
                        )
                }
            )

        private val mainMantissaLengthInputFactory: InputModelFactory<String, DecimalScale, Unit, InputType.Edit> =
            InputType.Edit.toInputModelFactory(
                parsingMapper = ParsingMapper(
                    encode = { scale -> scale.scale.toString() },
                    parse = { string ->
                        string
                            .toLongOrNull()
                            ?.takeIf { it in 0..5 }
                            ?.let(::DecimalScale)
                            .foldNullable(
                                ifNull = { Unit.left() },
                                ifNotNull = { it.right() }
                            )
                    }
                )
            )

        private val syncSchemeInputFactory: InputModelFactory<HttpScheme, HttpScheme, Nothing, InputType.Variant<HttpScheme>> =
            InputType.Variant(
                variants = HttpScheme.entries.toNonEmptyListOrThrow()
            )
                .toInputModelFactory()

        private val syncHostInputFactory: InputModelFactory<String, ServerHost, Unit, InputType.Edit> =
            InputType.Edit.toInputModelFactory(
                ParsingMapper(
                    encode = ServerHost::host,
                    parse = { input ->
                        ServerHost
                            .createOrNull(input)
                            .foldNullable(
                                ifNull = { Unit.left() },
                                ifNotNull = ServerHost::right,
                            )
                    }
                )
            )
    }
}