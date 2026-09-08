package org.hnau.pinfin.model.utils.budget.query

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.BudgetConfig
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.impl.BudgetsStorageQuery
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.budget.storage.BudgetsStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeBudgetsStorage(
    override val list: StateFlow<List<KeyValue<BudgetId, BudgetRepository>>>,
) : BudgetsStorage {

    override suspend fun createNewBudgetIfNotExists(id: BudgetId, initialConfig: BudgetConfig) {
        error("not used in tests")
    }
}

private fun fakeRepository(
    state: BudgetState,
): BudgetRepository = BudgetRepository(
    state = MutableStateFlow(state),
    upchainRepository = object : org.hnau.upchain.core.repository.upchain.UpchainRepository {
        override val upchain: StateFlow<org.hnau.upchain.core.Upchain>
            get() = MutableStateFlow(org.hnau.upchain.core.Upchain.empty)

        override suspend fun <R> editWithResult(
            modify: suspend (org.hnau.upchain.core.Upchain) -> Pair<org.hnau.upchain.core.Upchain, R>,
        ): R = error("not used in tests")
    },
    remove = { error("not used in tests") },
)

class BudgetsStorageQueryTest {

    @Test
    fun budgetsReturnsIdAndTitleOfEveryBudget() = runBlocking {
        val firstId = BudgetId.new()
        val secondId = BudgetId.new()
        val firstState = testBudgetState().let { it.copy(info = it.info.copy(title = "First")) }
        val secondState = testBudgetState().let { it.copy(info = it.info.copy(title = "Second")) }
        val storage = FakeBudgetsStorage(
            MutableStateFlow(
                listOf(
                    KeyValue(firstId, fakeRepository(firstState)),
                    KeyValue(secondId, fakeRepository(secondState)),
                ),
            ),
        )

        val budgets = BudgetsStorageQuery(storage).budgets()

        assertEquals(setOf(firstId to "First", secondId to "Second"), budgets.map { it.id to it.title }.toSet())
    }

    @Test
    fun budgetReturnsNullForUnknownId() = runBlocking {
        val storage = FakeBudgetsStorage(MutableStateFlow(emptyList()))

        assertNull(BudgetsStorageQuery(storage).budget(BudgetId.new()))
    }

    @Test
    fun budgetReturnsQueryWithMatchingId() = runBlocking {
        val id = BudgetId.new()
        val storage = FakeBudgetsStorage(
            MutableStateFlow(listOf(KeyValue(id, fakeRepository(testBudgetState())))),
        )

        val query = BudgetsStorageQuery(storage).budget(id)

        assertEquals(id, query!!.id)
    }
}
