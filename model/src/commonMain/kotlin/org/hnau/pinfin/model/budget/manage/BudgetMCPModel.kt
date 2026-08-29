@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.manage

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.ifTrue
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer

class BudgetMCPModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies

    @Serializable
    data class Skeleton(
        val mcpIsEnabled: MutableStateFlow<Boolean> =
            false.toMutableStateFlowAsInitial(),
    )

    val mcpIsEnabled: MutableStateFlow<Boolean>
        get() = skeleton.mcpIsEnabled

    init {
        scope.launch {
            mcpIsEnabled.collectLatest { mcpIsEnabled ->
                mcpIsEnabled.ifTrue {
                    launchMCP()
                }
            }
        }
    }

    private suspend fun launchMCP(): Nothing {
        val server = scope.embeddedServer(
            factory = CIO,
            host = "0.0.0.0",
            port = MCP_PORT,
        ) {
            mcpStreamableHttp(
                enableDnsRebindingProtection = false,
            ) {
                createMcpServer()
            }
        }
        server.start(wait = false)
        try {
            awaitCancellation()
        } finally {
            server.stop()
        }
    }

    private fun createMcpServer(): Server = Server(
        serverInfo = Implementation(
            name = "pinfin",
            version = "0.1.0",
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                resources = ServerCapabilities.Resources(),
                tools = ServerCapabilities.Tools(),
            ),
        ),
    ) {
        addResourceTemplate(
            uriTemplate = "echo://{text}",
            name = "echo",
            description = "Returns the managed part of the URI path",
            mimeType = "text/plain",
        ) { request, variables ->
            ReadResourceResult(
                contents = listOf(
                    TextResourceContents(
                        text = variables["text"].orEmpty(),
                        uri = request.params.uri,
                        mimeType = "text/plain",
                    ),
                ),
            )
        }

        addTool(
            name = "echo",
            description = "Returns the string passed to it",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("text", buildJsonObject { put("type", "string") })
                },
                required = listOf("text"),
            ),
        ) { request ->
            CallToolResult(
                content = listOf(
                    TextContent(
                        text = request.params.arguments
                            ?.get("text")
                            ?.jsonPrimitive
                            ?.contentOrNull
                            .orEmpty(),
                    ),
                ),
            )
        }
    }

    private companion object {
        const val MCP_PORT: Int = 8080
    }
}
