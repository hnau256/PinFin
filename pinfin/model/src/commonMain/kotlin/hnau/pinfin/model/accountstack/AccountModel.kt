package hnau.pinfin.model.accountstack

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.combineStateWith
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.pinfin.data.AccountConfig
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pinfin.model.utils.icons.icon
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class AccountModel(
    scope: CoroutineScope,
    private val info: AccountInfo,
    val icon: StateFlow<IconVariant?>,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    onReady: () -> Unit,
    val chooseIcon: () -> Unit,
) : GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val title: MutableStateFlow<EditingString>,
        val hue: MutableStateFlow<Hue>,
        val hideIfAmountIsZero: MutableStateFlow<Boolean>,
    ) {

        constructor(
            info: AccountInfo,
        ) : this(
            title = info.title.toEditingString().toMutableStateFlowAsInitial(),
            hue = info.hue.toMutableStateFlowAsInitial(),
            hideIfAmountIsZero = info.hideIfAmountIsZero.toMutableStateFlowAsInitial(),
        )
    }

    val title: MutableStateFlow<EditingString>
        get() = skeleton.title

    val hue: MutableStateFlow<Hue>
        get() = skeleton.hue

    private val nonEmptyTitle: StateFlow<String?> = title.mapState(scope) { title ->
        title
            .text
            .trim()
            .takeIf(String::isNotEmpty)
    }

    val titleIsCorrect: StateFlow<Boolean> =
        nonEmptyTitle.mapState(scope) { it != null }

    val hideIfAmountIsZero: MutableStateFlow<Boolean>
        get() = skeleton.hideIfAmountIsZero
    private val config: StateFlow<AccountConfig?> = nonEmptyTitle
        .combineStateWith(
            scope = scope,
            other = hue,
        ) { titleOrNull, hue ->
            titleOrNull?.let { title ->
                title to hue
            }
        }
        .combineStateWith(
            scope = scope,
            other = hideIfAmountIsZero,
        ) { titleWithHueOrNull, hideIfAmountIsZero ->
            titleWithHueOrNull?.let { (title, hue) ->
                Triple(title, hue, hideIfAmountIsZero)
            }
        }
        .combineStateWith(
            scope = scope,
            other = icon,
        ) { titleWithHueAndHideIfAmountIsZeroOrNull, icon ->
            titleWithHueAndHideIfAmountIsZeroOrNull?.let { (title, hue, hideIfAmountIfZero) ->
                AccountConfig(
                    title = title.takeIf { it != info.title },
                    hue = hue.takeIf { it != info.hue },
                    icon = icon.takeIf { it != info.icon }?.icon,
                    hideIfAmountIsZero = hideIfAmountIfZero.takeIf { it != info.hideIfAmountIsZero },
                )
            }
        }

    val save: StateFlow<StateFlow<(() -> Unit)?>?> = config
        .mapWithScope(scope) { scope, configOrNull ->
            configOrNull?.let { config ->
                actionOrNullIfExecuting(scope) {
                    dependencies
                        .budgetRepository
                        .accounts
                        .addConfig(
                            id = info.id,
                            config = config,
                        )
                    onReady()
                }
            }
        }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler //TODO show cancel edit dialog
}