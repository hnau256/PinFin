@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.part

import hnau.common.app.model.EditingString
import hnau.common.app.model.toEditingString
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.data.Comment
import hnau.pinfin.model.transaction.page.CommentPageModel
import hnau.pinfin.model.transaction.page.PageModel
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class CommentModel(
    scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    val requestFocus: () -> Unit,
    val isFocused: StateFlow<Boolean>,
) : PartModel {

    @Pipe
    interface Dependencies {

        fun page(): CommentPageModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val comment: MutableStateFlow<EditingString>,
        var page: CommentPageModel.Skeleton? = null,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                comment = "".toEditingString().toMutableStateFlowAsInitial(),
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

    val comment: StateFlow<Comment> = skeleton
        .comment
        .mapState(scope) {comment ->
            comment
                .text
                .let(::Comment)
        }

    override fun createPage(
        scope: CoroutineScope,
            ): PageModel = CommentPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { CommentPageModel.Skeleton() },
                comment = skeleton.comment,
    )
}