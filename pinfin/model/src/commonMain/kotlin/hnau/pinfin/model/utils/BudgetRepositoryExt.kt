package hnau.pinfin.model.utils

import hnau.common.kotlin.Loadable
import hnau.common.kotlin.Loading
import hnau.common.kotlin.Ready
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toLoadableStateFlow
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.fold
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.StateFlow

fun Deferred<BudgetRepository>.toBudgetInfoStateFlow(
    scope: CoroutineScope,
): StateFlow<Loadable<BudgetInfo>> = this
    .toLoadableStateFlow(scope)
    .scopedInState(scope)
    .flatMapState(scope) { (stateScope, repositoryOrLoading) ->
        repositoryOrLoading.fold(
            ifLoading = { Loading.toMutableStateFlowAsInitial() },
            ifReady = { repository ->
                repository.state.mapState(stateScope) { state ->
                    state.info.let(::Ready)
                }
            }
        )
    }