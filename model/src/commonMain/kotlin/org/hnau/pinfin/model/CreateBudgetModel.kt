package org.hnau.pinfin.model

import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
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
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.actionOrInProgressIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldNullable
import org.hnau.commons.kotlin.ifNull
import org.hnau.commons.kotlin.toEither
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.manage.BudgetOpener
import org.hnau.pinfin.model.utils.budget.ShareCode
import org.hnau.pinfin.model.utils.budget.repository.demo.DemoBudget
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.pinfin.model.utils.budget.storage.createNewBudgetIfNotExistsAndGet

class CreateBudgetModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val budgetsStorage: BudgetsStorage

        val budgetOpener: BudgetOpener
    }

    @Serializable
    data class Skeleton(
        val shareCode: InputSkeleton<String, ShareCode?> = shareCodeInputFactory.createSkeleton(
            value = null,
            useValueAsInitial = false,
        ),
    )

    val shareCode: InputModel<String, ShareCode?, Unit, InputType.Edit> =
        shareCodeInputFactory.createModel(
            scope = scope,
            skeleton = skeleton.shareCode,
        )

    val createFromShareCode: StateFlow<ActionOrElse<Unit, CancelOrInProgress.InProgress>?> = shareCode
        .editable
        .mapState(scope) { editableShareCode ->
            when (editableShareCode) {
                Editable.Incorrect -> null
                is Editable.Value -> editableShareCode.value
            }
        }
        .flatMapWithScope(scope) { scope, shareCodeOrNull ->
            shareCodeOrNull.foldNullable(
                ifNull = { null.toMutableStateFlowAsInitial() },
                ifNotNull = { shareCode ->
                    actionOrInProgressIfExecuting(scope) {
                        dependencies.budgetsStorage.createNewBudgetIfNotExists(
                            id = shareCode.id,
                            initialConfig = BudgetConfig(
                                title = shareCode.title,
                                sync = BudgetConfig.Sync(
                                    scheme = shareCode.scheme,
                                    host = shareCode.host,
                                )
                            )
                        )
                        dependencies.budgetOpener.openBudget(
                            budgetId = shareCode.id,
                        )
                    }
                }
            )
        }

    val createNewBudget: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(
            scope = scope,
        ) {
            val id = BudgetId.new()
            dependencies.budgetsStorage.createNewBudgetIfNotExists(
                id = id,
            )
            dependencies.budgetOpener.openBudget(
                budgetId = id,
            )
        }

    val createDemoBudget: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(
            scope = scope,
        ) {
            val updates = withContext(Dispatchers.Default) {
                DemoBudget.updates
            }
            dependencies
                .budgetsStorage
                .createNewBudgetIfNotExistsAndGet(
                    id = BudgetId.new(),
                )
                .applyUpdates(updates)
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler

    companion object {


        private val shareCodeInputFactory: InputModelFactory<String, ShareCode?, Unit, InputType.Edit> =
            InputType.Edit.toInputModelFactory(
                ParsingMapper(
                    encode = { shareCodeOrNull ->
                        shareCodeOrNull
                            ?.let(ShareCode.stringMapper.reverse)
                            .orEmpty()
                    },
                    parse = { input ->
                        input
                            .trim()
                            .takeIf(String::isNotEmpty)
                            ?.let { json ->
                                runCatching {
                                    ShareCode.stringMapper.direct(json)
                                }
                                    .toEither()
                                    .mapLeft { }
                            }
                            .ifNull { Either.Right(null) }
                    }
                )
            )
    }
}