@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.CategoryId
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.transaction_old_2.page.CommentPageModel.Suggest
import hnau.pinfin.model.utils.Delayed
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import hnau.pinfin.model.utils.resolveSuggests
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

class CategoryModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    comment: StateFlow<Comment>, //TODO use
) {

    @Pipe
    interface Dependencies {

        fun chooseOrCreate(): ChooseOrCreateModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var chooseOrCreate: ChooseOrCreateModel.Skeleton? = null,
        val category: MutableStateFlow<CategoryInfo?>,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                category = null.toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                category: CategoryInfo,
            ): Skeleton = Skeleton(
                category = category.toMutableStateFlowAsInitial(),
            )
        }
    }

    fun createPage(
        scope: CoroutineScope,
        usedCategories: StateFlow<Set<CategoryInfo>>,
    ): ChooseOrCreateModel<CategoryInfo> = ChooseOrCreateModel(
        scope = scope,
        dependencies = dependencies.chooseOrCreate(),
        skeleton = skeleton::chooseOrCreate
            .toAccessor()
            .getOrInit { ChooseOrCreateModel.Skeleton() },
        extractItemsFromState = BudgetState::categories,
        additionalItems = usedCategories,
        itemTextMapper = Mapper(
            direct = CategoryInfo::title,
            reverse = { title ->
                CategoryInfo(
                    id = CategoryId(title),
                    config = null,
                )
            }
        ),
        selected = skeleton.category.mapState(scope, CategoryInfo?::toOption),
        onReady = { selected ->
            skeleton.category.value = selected
            //TODO go forward
        }
    )

    val category: StateFlow<CategoryInfo?>
        get() = skeleton.category

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}