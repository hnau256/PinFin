@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.pageable

import arrow.core.toOption
import org.hnau.commons.app.model.EditingString
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.toEditingString
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.Delayed
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.model.transaction.utils.Editable
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.resolveSuggests
import org.hnau.commons.gen.pipe.annotations.Pipe
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
    val goForward: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        val initialComment: Comment?,
        val comment: MutableStateFlow<EditingString> =
            initialComment?.text.orEmpty().toEditingString().toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initialComment = null,
            )

            fun createForEdit(
                comment: Comment,
            ): Skeleton = Skeleton(
                initialComment = comment,
            )
        }
    }

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        val comment: MutableStateFlow<EditingString>,
        extractSuggests: suspend (BudgetState) -> List<Pair<Comment, Instant>>,
        private val goForward: () -> Unit,
    ) {

        @Pipe
        interface Dependencies {

            val budgetRepository: BudgetRepository
        }


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
                        goForward()
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
        comment = skeleton.comment,
        extractSuggests = extractSuggests,
        goForward = goForward,
    )

    val commentEditingString: MutableStateFlow<EditingString>
        get() = skeleton.comment

    internal val commentEditable: StateFlow<Editable.Value<Comment>> = Editable.Value.create(
        scope = scope,
        value = skeleton
            .comment
            .mapState(scope) { it.text.let(::Comment) },
        initialValueOrNone = skeleton.initialComment.toOption(),
    )

    val comment: StateFlow<Comment> = commentEditable
        .mapState(scope, Editable.Value<Comment>::value)

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}