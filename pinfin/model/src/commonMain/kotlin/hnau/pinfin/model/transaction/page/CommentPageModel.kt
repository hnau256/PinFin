@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.page

import hnau.common.app.model.EditingString
import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.Loadable
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.data.Comment
import hnau.pinfin.model.utils.Delayed
import hnau.pinfin.model.utils.budget.repository.BudgetRepository
import hnau.pinfin.model.utils.resolveSuggests
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Instant

class CommentPageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
        val comment: MutableStateFlow<EditingString>,
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
        searchQuery = comment.mapState(scope) { it.text },
        source = dependencies.budgetRepository.state,
        extractItems = { state ->
            state
                .transactions
                .map { transaction ->
                    val text = transaction
                        .comment
                        .text
                        .trim()
                    transaction.timestamp to Suggest(
                        comment = text.let(::Comment),
                        onClick = { comment.value = text.toEditingString() },
                    )
                }
        },
        extractText = { (_, suggest) -> suggest.comment.text },
        extractTimestamp = Pair<Instant, *>::first,
        convertToResult = Pair<*, Suggest>::second
    )

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}