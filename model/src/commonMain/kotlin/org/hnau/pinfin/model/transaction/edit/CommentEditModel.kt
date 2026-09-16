package org.hnau.pinfin.model.transaction.edit

import arrow.core.some
import arrow.core.toOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.goback.NeverGoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.pinfin.data.Comment

class CommentEditModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
) {

    @Pipe
    interface Dependencies {


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

    val commentEditable: StateFlow<Editable.Value<Comment>> = input.mapState(scope) { input ->
        Editable.Value.create(
            value = input.trim().let(::Comment),
            initialValueOrNone = skeleton.initial.toOption(),
        )
    }

    val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler
}