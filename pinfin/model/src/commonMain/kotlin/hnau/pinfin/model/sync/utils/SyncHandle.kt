package hnau.pinfin.model.sync.utils

import hnau.pinfin.data.BudgetId
import hnau.pinfin.model.utils.budget.state.BudgetInfo
import hnau.pinfin.model.utils.budget.upchain.Upchain
import hnau.pinfin.model.utils.budget.upchain.UpchainHash
import hnau.pinfin.model.utils.budget.upchain.Update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
sealed interface SyncHandle<O> {

    val responseSerializer: KSerializer<O>

    @Serializable
    @SerialName("get_budgets")
    data object GetBudgets : SyncHandle<GetBudgets.Response> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        @Serializable
        data class Response(
            val budgets: List<Budget>,
        ) {

            @Serializable
            data class Budget(
                val id: BudgetId,
                val peekHash: UpchainHash?,
                val info: BudgetInfo,
            )
        }
    }

    @Serializable
    @SerialName("get_updates")
    data class GetMaxToMinUpdates(
        val budgetId: BudgetId,
        val before: UpchainHash?,
    ) : SyncHandle<GetMaxToMinUpdates.Response> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        @Serializable
        data class Response(
            val updates: List<Upchain.Item>,
            val hasMoreUpdates: Boolean,
        )
    }

    @Serializable
    @SerialName("append_updates")
    data class AppendUpdates(
        val budgetId: BudgetId,
        val peekHashToCheck: UpchainHash?,
        val updates: List<Update>,
    ) : SyncHandle<AppendUpdates.Response> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        @Serializable
        data object Response
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        val serializer: KSerializer<SyncHandle<*>> = serializer(
            typeSerial0 = Unit.serializer(),
        ) as KSerializer<SyncHandle<*>>
    }
}