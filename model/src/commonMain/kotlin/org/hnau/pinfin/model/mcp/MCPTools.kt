package org.hnau.pinfin.model.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.hnau.pinfin.data.BudgetId
import org.hnau.pinfin.model.utils.budget.query.Aggregation
import org.hnau.pinfin.model.utils.budget.query.AnalyticsTable
import org.hnau.pinfin.model.utils.budget.query.BudgetMetadata
import org.hnau.pinfin.model.utils.budget.query.BudgetQuery
import org.hnau.pinfin.model.utils.budget.query.BudgetsList
import org.hnau.pinfin.model.utils.budget.query.BudgetsQuery
import org.hnau.pinfin.model.utils.budget.query.DEFAULT_RECORDS_LIMIT
import org.hnau.pinfin.model.utils.budget.query.GroupBy
import org.hnau.pinfin.model.utils.budget.query.PeriodSpec
import org.hnau.pinfin.model.utils.budget.query.RecordsFilter
import org.hnau.pinfin.model.utils.budget.query.RecordsPage
import org.hnau.pinfin.model.utils.budget.query.queryJson

internal fun Server.registerPinFinTools(
    budgetsQuery: BudgetsQuery,
) {
    addTool(
        name = "list_budgets",
        description = "Lists all budgets available in the PinFin app. Use the returned id as budget_id for the other tools.",
        inputSchema = ToolSchema(),
        toolAnnotations = readOnlyToolAnnotations,
    ) { request ->
        handle<NoInput, BudgetsList>(request) {
            BudgetsList(budgets = budgetsQuery.budgets())
        }
    }

    addTool(
        name = "get_budget_metadata",
        description = "Returns metadata of a budget: currency scale, date range of data, counts, and the full lists " +
            "of categories and accounts (ids, titles, directions/balances). Call this before filtering or grouping " +
            "to learn valid category and account ids.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("budget_id", budgetIdSchema)
            },
            required = listOf("budget_id"),
        ),
        toolAnnotations = readOnlyToolAnnotations,
    ) { request ->
        handle<BudgetIdInput, BudgetMetadata>(request) { input ->
            budgetsQuery.budgetOrThrow(input.budgetId).metadata()
        }
    }

    addTool(
        name = "list_records",
        description = "Returns a page of flat money records of a budget, newest first. Every record is a single " +
            "movement of money on one account. A transfer between accounts appears as two records without " +
            "category (debit on the source account, credit on the destination), linked by transaction_id. " +
            "Use filter to narrow down; use offset/limit to paginate (limit max 200).",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("budget_id", budgetIdSchema)
                put("filter", recordsFilterSchema)
                put("offset", schemaInteger("Zero-based offset into the result, default 0.", minimum = 0))
                put("limit", schemaInteger("Max records to return, default $DEFAULT_RECORDS_LIMIT, max 200.", minimum = 1, maximum = 200))
            },
            required = listOf("budget_id"),
        ),
        toolAnnotations = readOnlyToolAnnotations,
    ) { request ->
        handle<ListRecordsInput, RecordsPage>(request) { input ->
            budgetsQuery.budgetOrThrow(input.budgetId).records(
                filter = input.filter,
                offset = input.offset,
                limit = input.limit,
            )
        }
    }

    addTool(
        name = "get_analytics",
        description = "Builds an analytics table for a budget. Rows are calendar periods (whole range, N months " +
            "from a start day, N years, or N days from an anchor) laid over the date bounds of the filter; columns " +
            "are groups (categories, accounts, or none). Each cell and each row carries sum_debit " +
            "(expenses/outflow), sum_credit (income/inflow) and sum (credit minus debit). aggregation \"sum\" " +
            "totals the records (set incremental=true to get running totals, e.g. how savings on an account " +
            "grow); \"average\" divides each period into subperiods and returns the average per subperiod (e.g. " +
            "average per month within each year). All arithmetic is done server-side; do not recompute.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("budget_id", budgetIdSchema)
                put("filter", recordsFilterSchema)
                put("period", periodSpecSchema)
                put("group_by", schemaString("Group columns by category or account.", enum = listOf("category", "account")))
                put("aggregation", aggregationSchema)
            },
            required = listOf("budget_id"),
        ),
        toolAnnotations = readOnlyToolAnnotations,
    ) { request ->
        handle<AnalyticsInput, AnalyticsTable>(request) { input ->
            budgetsQuery.budgetOrThrow(input.budgetId).analytics(
                filter = input.filter,
                period = input.period,
                groupBy = input.groupBy,
                aggregation = input.aggregation,
            )
        }
    }
}

private val readOnlyToolAnnotations = ToolAnnotations(
    readOnlyHint = true,
    destructiveHint = false,
    idempotentHint = true,
    openWorldHint = false,
)

private val budgetIdSchema: JsonObject =
    schemaString("Budget id (UUID), from list_budgets.")

private val recordsFilterSchema: JsonObject = schemaObject(
    description = "Filter for narrowing down records. All fields are optional; absence means no restriction.",
) {
    put("date_min", schemaString("Inclusive lower date bound.", format = "date"))
    put("date_max", schemaString("Inclusive upper date bound.", format = "date"))
    put("query", schemaString("Case-insensitive substring to search for in the record's comment."))
    put("amount_min", schemaString("Inclusive lower amount bound, e.g. \"10.00\"."))
    put("amount_max", schemaString("Inclusive upper amount bound, e.g. \"1000.00\"."))
    put(
        "categories",
        schemaArray(
            "Only records with one of these category ids.",
            schemaString("Category id, e.g. \"-Food\" (expense) or \"+Salary\" (income)."),
        ),
    )
    put("accounts", schemaArray("Only records with one of these account ids.", schemaString("Account id.")))
    put(
        "direction",
        schemaString(
            "credit/debit - only entries (non-transfers) of that direction; transfer - only transfer halves.",
            enum = listOf("credit", "debit", "transfer"),
        ),
    )
}

private val periodSpecSchema: JsonObject = schemaOneOf(
    description = "How to split the table range into rows. Default: whole.",
    variants = listOf(
        schemaObject(required = listOf("type")) {
            put("type", buildJsonObject { put("const", "whole") })
        },
        schemaObject(required = listOf("type", "count", "start_day")) {
            put("type", buildJsonObject { put("const", "months") })
            put("count", schemaInteger("Number of months per row (1 = month, 3 = quarter).", minimum = 1))
            put("start_day", schemaInteger("Day of month each row starts at (1..31).", minimum = 1, maximum = 31))
        },
        schemaObject(required = listOf("type", "count", "start_month", "start_day")) {
            put("type", buildJsonObject { put("const", "years") })
            put("count", schemaInteger("Number of years per row.", minimum = 1))
            put("start_month", schemaInteger("Month each row starts at (1..12).", minimum = 1, maximum = 12))
            put("start_day", schemaInteger("Day of month each row starts at (1..31).", minimum = 1, maximum = 31))
        },
        schemaObject(required = listOf("type", "count", "anchor")) {
            put("type", buildJsonObject { put("const", "days") })
            put("count", schemaInteger("Number of days per row (e.g. 7 + a Monday anchor = calendar week).", minimum = 1))
            put("anchor", schemaString("A date on the boundary of a row.", format = "date"))
        },
    ),
)

private val aggregationSchema: JsonObject = schemaOneOf(
    description = "How to aggregate each row. Default: sum.",
    variants = listOf(
        schemaObject(required = listOf("type")) {
            put("type", buildJsonObject { put("const", "sum") })
            put("incremental", schemaBoolean("If true, values accumulate row over row (running totals)."))
        },
        schemaObject(required = listOf("type", "subperiod")) {
            put("type", buildJsonObject { put("const", "average") })
            put(
                "subperiod",
                schemaObject(required = listOf("count", "unit")) {
                    put("count", schemaInteger("Subperiod length.", minimum = 1))
                    put("unit", schemaString("Subperiod unit.", enum = listOf("day", "month", "year")))
                },
            )
        },
    ),
)

@Serializable
private object NoInput

@Serializable
private data class BudgetIdInput(
    @SerialName("budget_id")
    val budgetId: BudgetId,
)

@Serializable
private data class ListRecordsInput(
    @SerialName("budget_id")
    val budgetId: BudgetId,
    val filter: RecordsFilter? = null,
    val offset: Int = 0,
    val limit: Int = DEFAULT_RECORDS_LIMIT,
)

@Serializable
private data class AnalyticsInput(
    @SerialName("budget_id")
    val budgetId: BudgetId,
    val filter: RecordsFilter? = null,
    val period: PeriodSpec = PeriodSpec.default,
    @SerialName("group_by")
    val groupBy: GroupBy? = null,
    val aggregation: Aggregation = Aggregation.default,
)

private suspend fun BudgetsQuery.budgetOrThrow(
    id: BudgetId,
): BudgetQuery = budget(id) ?: throw IllegalArgumentException(
    "Budget $id not found. Call list_budgets to get valid ids."
)

private suspend inline fun <reified I, reified O> handle(
    request: CallToolRequest,
    crossinline block: suspend (I) -> O,
): CallToolResult = runCatching {
    val input = queryJson.decodeFromJsonElement<I>(request.params.arguments ?: JsonObject(emptyMap()))
    val output = block(input)
    val element = queryJson.encodeToJsonElement(output).jsonObject
    CallToolResult(
        content = listOf(TextContent(text = element.toString())),
        structuredContent = element,
    )
}.getOrElse { e ->
    CallToolResult(
        content = listOf(TextContent(text = "Error: ${e.message ?: e::class.simpleName}")),
        isError = true,
    )
}
