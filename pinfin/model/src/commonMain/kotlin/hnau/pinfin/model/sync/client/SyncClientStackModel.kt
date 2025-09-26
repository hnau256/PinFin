@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.sync.client

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.SkeletonWithModel
import hnau.common.app.model.stack.goBackHandler
import hnau.common.app.model.stack.modelsOnly
import hnau.common.app.model.stack.push
import hnau.common.app.model.stack.tryDropLast
import hnau.common.app.model.stack.withModels
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import hnau.pinfin.model.sync.client.list.SyncClientListModel
import hnau.pinfin.model.sync.client.utils.TcpSyncClient
import hnau.pinfin.model.sync.utils.ServerAddress
import hnau.pinfin.model.sync.utils.ServerPort
import hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class SyncClientStackModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val budgetsStorage: BudgetsStorage

        @Pipe
        interface WithSyncClient {

            fun list(
                budgetOpener: BudgetSyncOpener,
            ): SyncClientListModel.Dependencies

            fun budget(): SyncClientLoadBudgetModel.Dependencies
        }

        fun withSyncClient(
            tcpSyncClient: TcpSyncClient,
        ): WithSyncClient
    }

    private val dependenciesWithSyncClient: Dependencies.WithSyncClient =
        dependencies.withSyncClient(
            tcpSyncClient = TcpSyncClient(
                address = skeleton.address,
                port = skeleton.port,
            )
        )

    @Serializable
    data class Skeleton(
        val port: ServerPort,
        val address: ServerAddress,
        val stack: MutableStateFlow<NonEmptyStack<SyncClientStackElementModel.Skeleton>> =
            MutableStateFlow(NonEmptyStack(SyncClientStackElementModel.Skeleton.List)),
    )

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<SyncClientStackElementModel.Skeleton, SyncClientStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = SyncClientStackElementModel.Skeleton::key,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: SyncClientStackElementModel.Skeleton,
    ): SyncClientStackElementModel = when (skeleton) {
        is SyncClientStackElementModel.Skeleton.List -> SyncClientStackElementModel.List(
            SyncClientListModel(
                scope = modelScope,
                dependencies = dependenciesWithSyncClient.list(
                    budgetOpener = { budgetId ->
                        this@SyncClientStackModel
                            .skeleton
                            .stack
                            .push(
                                SyncClientStackElementModel.Skeleton.Budget(
                                    SyncClientLoadBudgetModel.Skeleton(
                                        id = budgetId,
                                    )
                                )
                            )
                    }
                ),
            )
        )

        is SyncClientStackElementModel.Skeleton.Budget -> SyncClientStackElementModel.Budget(
            SyncClientLoadBudgetModel(
                scope = modelScope,
                skeleton = skeleton.skeleton,
                dependencies = dependenciesWithSyncClient.budget(),
                goBack = {
                    this@SyncClientStackModel
                        .skeleton
                        .stack
                        .tryDropLast()
                },
            )
        )
    }

    val stack: StateFlow<NonEmptyStack<SyncClientStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = SyncClientStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}