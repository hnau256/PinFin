package hnau.pinfin.model.categorystack

import org.hnau.commons.app.model.EditingString
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.toEditingString
import org.hnau.commons.kotlin.coroutines.actionOrNullIfExecuting
import org.hnau.commons.kotlin.coroutines.flow.state.combineStateWith
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import hnau.pinfin.data.CategoryConfig
import hnau.pinfin.data.Hue
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.icons.IconVariant
import hnau.pinfin.model.utils.icons.icon
import org.hnau.commons.gen.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class CategoryModel(
    scope: CoroutineScope,
    private val info: CategoryInfo,
    val icon: StateFlow<IconVariant?>,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    onReady: () -> Unit,
    val chooseIcon: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val title: MutableStateFlow<EditingString>,
        val hue: MutableStateFlow<Hue>,
    ) {

        constructor(
            info: CategoryInfo,
        ) : this(
            title = info.title.toEditingString().toMutableStateFlowAsInitial(),
            hue = info.hue.toMutableStateFlowAsInitial(),
        )
    }

    val title: MutableStateFlow<EditingString>
        get() = skeleton.title

    val hue: MutableStateFlow<Hue>
        get() = skeleton.hue

    private val nonEmptyTitle: StateFlow<String?> = title.mapState(scope) { title ->
        title
            .text
            .trim()
            .takeIf(String::isNotEmpty)
    }

    val titleIsCorrect: StateFlow<Boolean> =
        nonEmptyTitle.mapState(scope) { it != null }

    private val config: StateFlow<CategoryConfig?> = nonEmptyTitle
        .combineStateWith(
            scope = scope,
            other = hue,
        ) { titleOrNull, hue ->
            titleOrNull?.let { title ->
                title to hue
            }
        }
        .combineStateWith(
            scope = scope,
            other = icon,
        ) { titleWithHueOrNull, icon ->
            titleWithHueOrNull?.let { (title, hue) ->
                CategoryConfig(
                    title = title.takeIf { it != info.title },
                    hue = hue.takeIf { it != info.hue },
                    icon = icon.takeIf { it != info.icon }?.icon,
                )
            }
        }

    val save: StateFlow<StateFlow<(() -> Unit)?>?> = config
        .mapWithScope(scope) { scope, configOrNull ->
            configOrNull?.let { config ->
                actionOrNullIfExecuting(scope) {
                    dependencies
                        .budgetRepository
                        .categories
                        .addConfig(
                            id = info.id,
                            config = config,
                        )
                    onReady()
                }
            }
        }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler //TODO show cancel edit dialog
}