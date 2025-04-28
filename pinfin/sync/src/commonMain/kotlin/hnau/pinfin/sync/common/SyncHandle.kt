package hnau.pinfin.sync.common

import hnau.pinfin.data.BudgetId
import hnau.pinfin.upchain.Update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
sealed interface SyncHandle<O> {

    val responseSerializer: KSerializer<O>

    @Serializable
    @SerialName("ping")
    data object Ping : SyncHandle<Ping.Response> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        @Serializable
        data object Response
    }

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
                val peekHash: UpchainHash,
            )
        }
    }

    @Serializable
    @SerialName("check_contains_one_of_hashes")
    data class CheckContainsOneOfHashes(
        val budgetId: BudgetId,
        val hashes: List<UpchainHash>,
    ) : SyncHandle<CheckContainsOneOfHashes.Response> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        @Serializable
        data class Response(
            val foundHash: UpchainHash?,
        )
    }

    @Serializable
    @SerialName("get_updates")
    data class GetUpdates(
        val budgetId: BudgetId,
        val after: UpchainHash,
    ) : SyncHandle<GetUpdates.Response> {

        override val responseSerializer: KSerializer<Response>
            get() = Response.serializer()

        @Serializable
        data class Response(
            val updates: List<Update>,
            val hasMoreUpdates: Boolean,
        )
    }

    @Serializable
    @SerialName("add_updates")
    data class AddUpdates(
        val budgetId: BudgetId,
        val after: UpchainHash,
        val updates: List<Update>,
    ) : SyncHandle<AddUpdates.Response> {

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