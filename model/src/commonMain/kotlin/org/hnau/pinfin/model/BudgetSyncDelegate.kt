@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.preferences.Preference
import org.hnau.commons.app.model.preferences.Preferences
import org.hnau.commons.app.model.preferences.map
import org.hnau.commons.app.model.preferences.mapOption
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.CancelOrInProgress
import org.hnau.commons.kotlin.coroutines.actionOrCancelIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifTrue
import org.hnau.commons.kotlin.invoke
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.optionToNullable
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetInfo
import org.hnau.upchain.core.UpchainId
import org.hnau.upchain.sync.client.core.SyncListener
import org.hnau.upchain.sync.client.core.sync
import org.hnau.upchain.sync.client.http.HttpSyncClient
import kotlin.time.Clock
import kotlin.time.Instant

class BudgetSyncDelegate(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {

        val id: BudgetId

        val budgetRepository: BudgetRepository

        val preferences: Preferences
    }

    @Serializable
    data class Statistics(
        val receivedUpdates: Int = 0,
        val appliedUpdates: Int = 0,
        val sentUpdates: Int = 0,
    )

    @Serializable
    data class Skeleton(
        val lastCurrentSessionResult: MutableStateFlow<Boolean?> = null.toMutableStateFlowAsInitial(),
        val statistics: MutableStateFlow<Statistics> = Statistics().toMutableStateFlowAsInitial(),
    )

    val lastCurrentSessionResult: StateFlow<Boolean?>
        get() = skeleton.lastCurrentSessionResult

    val statistics: MutableStateFlow<Statistics>
        get() = skeleton.statistics

    val lastSuccessSync: StateFlow<Instant?>
        get() = lastSuccessSyncPreference.value


    val sync: StateFlow<ActionOrElse<Unit, CancelOrInProgress.Cancel>> =
        actionOrCancelIfExecuting(scope) { scope ->
            val config = getConfig()
            val client = HttpSyncClient(
                scope = scope,
                host = config.host,
                scheme = config.scheme,
            )

            @Suppress("DEPRECATION")
            val success = dependencies
                .budgetRepository
                .upchainRepository
                .sync(
                    id = dependencies.id.id.let(::UpchainId),
                    api = client,
                    listener = SyncListener(
                        onUpdatesFromServers = { newUpdatesCount ->
                            skeleton.statistics.update { statistics ->
                                statistics.copy(
                                    receivedUpdates = statistics.receivedUpdates + newUpdatesCount,
                                )
                            }
                        },
                        onApplyServerUpdates = { newUpdatesCount ->
                            skeleton.statistics.update { statistics ->
                                statistics.copy(
                                    appliedUpdates = statistics.appliedUpdates + newUpdatesCount,
                                )
                            }
                        },
                        onUpdatesToServer = { newUpdatesCount ->
                            skeleton.statistics.update { statistics ->
                                statistics.copy(
                                    sentUpdates = statistics.sentUpdates + newUpdatesCount,
                                )
                            }
                        },
                    ),
                )
                .isSuccess

            skeleton.lastCurrentSessionResult.value = success

            if (success) {
                val now = Clock.System.now()
                lastSuccessSyncPreference.update(now)
            }
        }

    private val lastSuccessSyncPreference: Preference<Instant?> = dependencies
        .preferences["last_success_sync_${dependencies.id.id}"]
        .map(
            scope = scope,
            mapper = Mapper.stringToInstant,
        )
        .mapOption(
            scope = scope,
            mapper = Mapper.optionToNullable()
        )

    private fun getConfig(): BudgetInfo.Sync = dependencies
        .budgetRepository
        .state
        .value
        .info
        .sync

    init {

        val trySync: () -> Unit = {
            when (val sync = sync.value) {
                is ActionOrElse.Action -> sync.action()
                is ActionOrElse.Else<*> -> Unit
            }
        }

        getConfig()
            .onLaunch
            .ifTrue { trySync() }

        scope.launch {
            dependencies
                .budgetRepository
                .upchainEditVersion
                .drop(1)
                .collect {
                    getConfig()
                        .onUpdate
                        .ifTrue { trySync() }
                }
        }
    }
}

@Deprecated("use from hnau.commons")
val Mapper.Companion.stringToInstant: Mapper<String, Instant>
    get() = Mapper(
        direct = Instant::parse,
        reverse = Instant::toString,
    )