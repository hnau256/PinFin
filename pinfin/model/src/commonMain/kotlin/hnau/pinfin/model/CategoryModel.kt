@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.GoBackHandlerProvider
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.actionOrNullIfExecuting
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.CategoryConfig
import hnau.pinfin.data.CategoryId
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class CategoryModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    onReady: () -> Unit,
): GoBackHandlerProvider {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val id: CategoryId,
        val title: MutableStateFlow<EditingString>,
    ) {

        constructor(
            category: CategoryInfo,
        ): this(
            id = category.id,
            title = category.title.toEditingString().toMutableStateFlowAsInitial(),
        )
    }

    val title: MutableStateFlow<EditingString>
        get() = skeleton.title

    private val nonEmptyTitle: StateFlow<String?> = title.mapState(scope) { title ->
        title
            .text
            .trim()
            .takeIf(String::isNotEmpty)
    }

    val titleIsCorrect: StateFlow<Boolean> =
        nonEmptyTitle.mapState(scope) { it != null}

    private val config: StateFlow<CategoryConfig?> = nonEmptyTitle
            .scopedInState(scope)
            .mapState(scope) { (titleScope, titleOrNull) ->
                titleOrNull?.let {title ->
                    CategoryConfig(
                        title = title,
                    )
                }
            }

    val save: StateFlow<StateFlow<(() -> Unit)?>?> = config
        .mapWithScope(scope) { configScope, configOrNull ->
            configOrNull?.let { config ->
                actionOrNullIfExecuting(configScope) {
                    dependencies
                        .budgetRepository
                        .categories
                        .addConfig(
                            id = skeleton.id,
                            config = config,
                        )
                    onReady()
                }
            }
        }

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler //TODO show cancel edit dialog
}