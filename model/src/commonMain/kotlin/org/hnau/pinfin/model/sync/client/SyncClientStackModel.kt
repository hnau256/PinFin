@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.sync.client

import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.stack.NonEmptyStack
import org.hnau.commons.app.model.stack.SkeletonWithModel
import org.hnau.commons.app.model.stack.goBackHandler
import org.hnau.commons.app.model.stack.modelsOnly
import org.hnau.commons.app.model.stack.push
import org.hnau.commons.app.model.stack.tryDropLast
import org.hnau.commons.app.model.stack.withModels
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.sync.client.budget.SyncClientLoadBudgetModel
import org.hnau.pinfin.model.sync.client.list.SyncClientListModel
import org.hnau.pinfin.model.sync.client.utils.TcpSyncClient
import org.hnau.pinfin.model.sync.utils.ServerAddress
import org.hnau.pinfin.model.sync.utils.ServerPort
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import org.hnau.commons.gen.pipe.annotations.Pipe
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
        val stack: MutableStateFlow<NonEmptyStack<SyncClientStackElementSkeleton>> =
            MutableStateFlow(NonEmptyStack(ElementSkeleton.list(Unit))),
    )

    @SealUp(
        variants = [
            Variant(
                type = SyncClientListModel::class,
                identifier = "list",
            ),
            Variant(
                type = SyncClientLoadBudgetModel::class,
                identifier = "budget",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "SyncClientStackElementModel",
    )
    interface Element {

        val goBackHandler: GoBackHandler

        companion object
    }

    @SealUp(
        variants = [
            Variant(
                type = Unit::class,
                identifier = "list",
            ),
            Variant(
                type = SyncClientLoadBudgetModel.Skeleton::class,
                identifier = "budget",
            ),
        ],
        wrappedValuePropertyName = "skeleton",
        sealedInterfaceName = "SyncClientStackElementSkeleton",
        serializable = true,
    )
    interface ElementSkeleton {

        companion object
    }

    private val stackWithModels: StateFlow<NonEmptyStack<SkeletonWithModel<SyncClientStackElementSkeleton, SyncClientStackElementModel>>> =
        skeleton
            .stack
            .withModels(
                scope = scope,
                getKey = SyncClientStackElementSkeleton::ordinal,
            ) { modelScope, skeleton ->
                createModel(
                    modelScope = modelScope,
                    skeleton = skeleton,
                )
            }

    private fun createModel(
        modelScope: CoroutineScope,
        skeleton: SyncClientStackElementSkeleton,
    ): SyncClientStackElementModel = skeleton.fold(
        ifList = {
            Element.list(
                scope = modelScope,
                dependencies = dependenciesWithSyncClient.list(
                    budgetOpener = { budgetId ->
                        this@SyncClientStackModel
                            .skeleton
                            .stack
                            .push(
                                ElementSkeleton.budget(
                                    id = budgetId,
                                )
                            )
                    }
                ),
            )
        },
        ifBudget = { budgetSkeleton ->
            Element.budget(
                scope = modelScope,
                skeleton = budgetSkeleton,
                dependencies = dependenciesWithSyncClient.budget(),
                goBack = {
                    this@SyncClientStackModel
                        .skeleton
                        .stack
                        .tryDropLast()
                },
            )
        },
    )

    val stack: StateFlow<NonEmptyStack<SyncClientStackElementModel>> =
        stackWithModels.modelsOnly(scope)

    val goBackHandler: GoBackHandler = stackWithModels.goBackHandler(
        scope = scope,
        extractGoBackHandler = SyncClientStackElementModel::goBackHandler,
        updateSkeletonStack = skeleton.stack::value::set,
    )
}