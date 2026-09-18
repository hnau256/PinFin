@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.transaction.edit

import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.Loadable
import org.hnau.commons.kotlin.coroutines.Delayed
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.data.Comment
import org.hnau.pinfin.model.transaction.edit.utils.EditNavigateContext
import org.hnau.pinfin.model.utils.budget.repository.BudgetRepository
import org.hnau.pinfin.model.utils.budget.state.BudgetState
import org.hnau.pinfin.model.utils.resolveSuggests

class CommentEditModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val extractSuggests: suspend (BudgetState) -> List<Pair<Comment, LocalDate>>,
    val navigateContext: EditNavigateContext,
) {

    @Pipe
    interface Dependencies {

        val budgetRepository: BudgetRepository
    }

    @Serializable
    data class Skeleton(
        val initial: Comment?,
        val input: MutableStateFlow<String> = initial
            ?.text
            .orEmpty()
            .toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                initial = null,
            )

            fun create(
                initial: Comment,
            ): Skeleton = Skeleton(
                initial = initial,
            )
        }
    }

    val input: MutableStateFlow<String>
        get() = skeleton.input

    data class Suggest(
        val comment: Comment,
        val onClick: () -> Unit,
    )

    val suggests: StateFlow<Loadable<Delayed<List<Suggest>>>> = resolveSuggests(
        scope = scope,
        source = dependencies
            .budgetRepository
            .state,
        searchQuery = input,
        extractItems = extractSuggests,
        extractText = { it.first.text },
        extractTimestamp = Pair<*, LocalDate>::second,
        convertToResult = { idWithComment ->
            val comment = idWithComment.first
            Suggest(
                comment = comment,
                onClick = {
                    input.value = comment.text
                    navigateContext.goForward()
                },
            )
        },
    )

    val commentEditable: StateFlow<Editable.Value<Comment>> = input.mapState(scope) { input ->
        Editable.Value.create(
            value = input.trim().let(::Comment),
            initialValueOrNone = skeleton.initial.toOption(),
        )
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}