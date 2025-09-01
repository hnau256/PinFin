@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.Comment
import hnau.pinfin.model.utils.Delayed
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.budget.state.BudgetState
import hnau.pinfin.model.utils.resolveSuggests
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

class CommentModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val isFocused: StateFlow<Boolean>,
    val requestFocus: () -> Unit,
    private val extractSuggests: suspend (BudgetState) -> List<Pair<Comment, Instant>>,
) {

    @Pipe
    interface Dependencies {

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: Page.Skeleton? = null,
        val comment: MutableStateFlow<EditingString>,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                comment = ""
                    .toEditingString()
                    .toMutableStateFlowAsInitial(),
            )

            fun createForEdit(
                comment: Comment,
            ): Skeleton = Skeleton(
                comment = comment
                    .text
                    .toEditingString()
                    .toMutableStateFlowAsInitial(),
            )
        }
    }

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val comment: MutableStateFlow<EditingString>,
        extractSuggests: suspend (BudgetState) -> List<Pair<Comment, Instant>>,
    ) {

        @Pipe
        interface Dependencies {

            val budgetRepository: BudgetRepository
        }

        @Serializable
        /*data*/ class Skeleton

        data class Suggest(
            val comment: Comment,
            val onClick: () -> Unit,
        )

        val suggests: StateFlow<Loadable<Delayed<List<Suggest>>>> = resolveSuggests(
            scope = scope,
            source = dependencies
                .budgetRepository
                .state,
            searchQuery = comment.mapState(scope, EditingString::text),
            extractItems = extractSuggests,
            extractText = { it.first.text },
            extractTimestamp = Pair<*, Instant>::second,
            convertToResult = { (item) ->
                Suggest(
                    comment = item,
                    onClick = {
                        comment.value = item.text.toEditingString()
                        //TODO forward
                    }
                )
            },
        )

        val goBackHandler: GoBackHandler
            get() = NeverGoBackHandler
    }

    fun createPage(
        scope: CoroutineScope,
    ): Page = Page(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { Page.Skeleton() },
        comment = skeleton.comment,
        extractSuggests = extractSuggests,
    )

    val commentEditingString: MutableStateFlow<EditingString>
        get() = skeleton.comment

    val comment: StateFlow<Comment> = skeleton
        .comment
        .mapState(scope) { it.text.let(::Comment) }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}