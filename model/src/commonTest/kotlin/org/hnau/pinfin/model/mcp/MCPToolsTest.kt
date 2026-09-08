package org.hnau.pinfin.model.mcp

import io.modelcontextprotocol.kotlin.sdk.server.ClientConnection
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.CreateMessageRequest
import io.modelcontextprotocol.kotlin.sdk.types.CreateMessageResult
import io.modelcontextprotocol.kotlin.sdk.types.ElicitRequest
import io.modelcontextprotocol.kotlin.sdk.types.ElicitRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.ElicitResult
import io.modelcontextprotocol.kotlin.sdk.types.ElicitationCompleteNotification
import io.modelcontextprotocol.kotlin.sdk.types.EmptyResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ListRootsRequest
import io.modelcontextprotocol.kotlin.sdk.types.ListRootsResult
import io.modelcontextprotocol.kotlin.sdk.types.LoggingMessageNotification
import io.modelcontextprotocol.kotlin.sdk.types.PingRequest
import io.modelcontextprotocol.kotlin.sdk.types.ResourceUpdatedNotification
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.ServerNotification
import io.modelcontextprotocol.kotlin.sdk.shared.RequestOptions
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.BudgetQuery
import org.hnau.pinfin.model.utils.budget.query.BudgetSummary
import org.hnau.pinfin.model.utils.budget.query.BudgetsQuery
import org.hnau.pinfin.model.utils.budget.query.RecordsFilter
import org.hnau.pinfin.model.utils.budget.query.RecordsPage
import org.hnau.pinfin.model.utils.budget.query.AnalyticsTable
import org.hnau.pinfin.model.utils.budget.query.Aggregation
import org.hnau.pinfin.model.utils.budget.query.BudgetMetadata
import org.hnau.pinfin.model.utils.budget.query.CurrencyMeta
import org.hnau.pinfin.model.utils.budget.query.GroupBy
import org.hnau.pinfin.model.utils.budget.query.PeriodSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private object FakeClientConnection : ClientConnection {
    override val sessionId: String get() = "test"
    override suspend fun notification(notification: ServerNotification, relatedRequestId: io.modelcontextprotocol.kotlin.sdk.types.RequestId?) = error("not used")
    override suspend fun ping(request: PingRequest, options: RequestOptions?): EmptyResult = error("not used")
    override suspend fun createMessage(request: CreateMessageRequest, options: RequestOptions?): CreateMessageResult = error("not used")
    override suspend fun listRoots(request: ListRootsRequest, options: RequestOptions?): ListRootsResult = error("not used")
    override suspend fun createElicitation(message: String, requestedSchema: ElicitRequestParams.RequestedSchema, options: RequestOptions?): ElicitResult = error("not used")
    override suspend fun createElicitation(message: String, elicitationId: String, url: String, options: RequestOptions?): ElicitResult = error("not used")
    override suspend fun createElicitation(request: ElicitRequest, options: RequestOptions?): ElicitResult = error("not used")
    override suspend fun sendLoggingMessage(notification: LoggingMessageNotification) = error("not used")
    override suspend fun sendResourceUpdated(notification: ResourceUpdatedNotification) = error("not used")
    override suspend fun sendResourceListChanged() = error("not used")
    override suspend fun sendToolListChanged() = error("not used")
    override suspend fun sendPromptListChanged() = error("not used")
    override suspend fun sendElicitationComplete(notification: ElicitationCompleteNotification) = error("not used")
}

private class FakeBudgetQuery(
    override val id: BudgetId,
) : BudgetQuery {
    override suspend fun metadata(): BudgetMetadata = BudgetMetadata(
        id = id,
        title = "Test",
        currency = CurrencyMeta(scale = 2),
        transactionsCount = 0,
        recordsCount = 0,
        categories = emptyList(),
        accounts = emptyList(),
    )

    override suspend fun records(filter: RecordsFilter?, offset: Int, limit: Int): RecordsPage = RecordsPage(
        records = emptyList(),
        total = 0,
        offset = offset,
        limit = limit,
        hasMore = false,
    )

    override suspend fun analytics(
        filter: RecordsFilter?,
        period: PeriodSpec,
        groupBy: GroupBy?,
        aggregation: Aggregation,
    ): AnalyticsTable = AnalyticsTable.empty(aggregation = aggregation, groupBy = groupBy)
}

private class FakeBudgetsQuery(
    private val budgetId: BudgetId,
) : BudgetsQuery {
    override suspend fun budgets(): List<BudgetSummary> = listOf(BudgetSummary(id = budgetId, title = "Test"))
    override suspend fun budget(id: BudgetId): BudgetQuery? = id.takeIf { it == budgetId }?.let { FakeBudgetQuery(it) }
}

class MCPToolsTest {

    private fun createServer(
        budgetsQuery: BudgetsQuery,
    ): Server = Server(
        serverInfo = Implementation(name = "pinfin-test", version = "0.0.1"),
        options = ServerOptions(capabilities = ServerCapabilities(tools = ServerCapabilities.Tools())),
    ).apply {
        registerPinFinTools(budgetsQuery)
    }

    private suspend fun call(
        server: Server,
        name: String,
        arguments: JsonObject? = null,
    ): JsonObject {
        val handler = server.tools[name]!!.handler
        val result = FakeClientConnection.handler(
            CallToolRequest(params = CallToolRequestParams(name = name, arguments = arguments)),
        )
        assertTrue(result.isError != true, "expected success but got: ${result.content}")
        return result.structuredContent!!
    }

    @Test
    fun registersFourReadOnlyTools() {
        val server = createServer(FakeBudgetsQuery(BudgetId.new()))

        assertEquals(setOf("list_budgets", "get_budget_metadata", "list_records", "get_analytics"), server.tools.keys)
        server.tools.values.forEach { registered ->
            assertEquals(true, registered.tool.annotations?.readOnlyHint)
        }
    }

    @Test
    fun listBudgetsReturnsRegisteredBudget() = runBlocking {
        val id = BudgetId.new()
        val server = createServer(FakeBudgetsQuery(id))

        val result = call(server, "list_budgets")

        assertTrue(result.toString().contains(id.id.toString()))
    }

    @Test
    fun getBudgetMetadataForUnknownIdReturnsError() = runBlocking {
        val server = createServer(FakeBudgetsQuery(BudgetId.new()))
        val handler = server.tools["get_budget_metadata"]!!.handler

        val result = FakeClientConnection.handler(
            CallToolRequest(
                params = CallToolRequestParams(
                    name = "get_budget_metadata",
                    arguments = buildJsonObject { put("budget_id", BudgetId.new().id.toString()) },
                ),
            ),
        )

        assertEquals(true, result.isError)
    }

    @Test
    fun getBudgetMetadataForKnownIdSucceeds() = runBlocking {
        val id = BudgetId.new()
        val server = createServer(FakeBudgetsQuery(id))

        val result = call(server, "get_budget_metadata", buildJsonObject { put("budget_id", id.id.toString()) })

        assertEquals("Test", result["title"]!!.jsonPrimitive.content)
    }
}
