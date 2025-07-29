@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.AccountConfig
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class AccountModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val onReady: () -> Unit,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val info: AccountInfo,
        val title: MutableStateFlow<EditingString> = info
            .title
            .toEditingString()
            .toMutableStateFlowAsInitial(),
        val hideIfAmountIsZero: MutableStateFlow<Boolean> = info
            .hideIfAmountIsZero
            .toMutableStateFlowAsInitial(),
    )

    val title: MutableStateFlow<EditingString>
        get() = skeleton.title

    private val nonEmptyTitle: StateFlow<String?> = title.mapState(scope) { title ->
        title.text.takeIf(String::isNotEmpty)
    }

    val titleIsCorrect: StateFlow<Boolean> =
        nonEmptyTitle.mapState(scope) { it != null }

    val hideIfAmountIsZero: MutableStateFlow<Boolean>
        get() = skeleton.hideIfAmountIsZero

    private val config: StateFlow<AccountConfig?> = nonEmptyTitle
            .scopedInState(scope)
            .flatMapState(scope) { (titleScope, titleOrNull) ->
                titleOrNull.foldNullable(
                    ifNull = { null.toMutableStateFlowAsInitial() },
                    ifNotNull = { title ->
                        hideIfAmountIsZero.mapState(titleScope) { hideIfAmountIsZero ->
                            AccountConfig(
                                title = title
                                    .takeIf { it != skeleton.info.title },
                                hideIfAmountIsZero = hideIfAmountIsZero
                                    .takeIf { it != skeleton.info.hideIfAmountIsZero },
                            )
                        }
                    }
                )
            }

    val save: StateFlow<StateFlow<(() -> Unit)?>?> = config
        .mapWithScope(scope) { configScope, configOrNull ->
            configOrNull?.let { config ->
                actionOrNullIfExecuting(configScope) {
                    dependencies
                        .budgetRepository
                        .accounts
                        .addConfig(
                            id = skeleton.info.id,
                            config = config,
                        )
                    onReady()
                }
            }
        }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler //TODO show cancel edit dialog
}