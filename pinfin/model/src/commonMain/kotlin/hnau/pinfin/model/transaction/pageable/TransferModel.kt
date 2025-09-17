@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.flatMapWithScope
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
import hnau.pinfin.model.transaction.utils.Editable
import hnau.pinfin.model.transaction.utils.combineEditableWith
import hnau.pinfin.model.utils.budget.state.AccountInfo
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

class TransferModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val isFocused: StateFlow<Boolean>,
    private val requestFocus: () -> Unit,
    private val goForward: () -> Unit,
) {

    enum class Part {

        From, To, Amount;

        companion object {

            val default: Part
                get() = From
        }
    }

    sealed interface PageType {

        val key: Int

        val goBackHandler: GoBackHandler

        data class From(
            val model: ChooseOrCreateModel<AccountInfo>,
        ) : PageType {
            override val key: Int
                get() = 0

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class To(
            val model: ChooseOrCreateModel<AccountInfo>,
        ) : PageType {
            override val key: Int
                get() = 1

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Amount(
            val model: AmountModel.Page,
        ) : PageType {
            override val key: Int
                get() = 2

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }
    }

    @Pipe
    interface Dependencies {

        fun account(): AccountModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val from: AccountModel.Skeleton,
        val to: AccountModel.Skeleton,
        val amount: AmountModel.Skeleton,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                from = AccountModel.Skeleton.createForNew(),
                to = AccountModel.Skeleton.createForNew(),
                amount = AmountModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                transfer: TransactionInfo.Type.Transfer,
            ): Skeleton = Skeleton(
                from = AccountModel.Skeleton.createForEdit(
                    account = transfer.from,
                ),
                to = AccountModel.Skeleton.createForEdit(
                    account = transfer.to,
                ),
                amount = AmountModel.Skeleton.createForEdit(
                    amount = transfer.amount,
                ),
            )
        }
    }

    private fun createIsFocused(
        part: Part,
    ): StateFlow<Boolean> = isFocused.flatMapWithScope(scope) { isFocusedScope, isFocused ->
            isFocused.foldBoolean(
                ifFalse = { false.toMutableStateFlowAsInitial() },
                ifTrue = {
                    skeleton
                        .part
                        .mapState(isFocusedScope) { it == part }
                }
            )
        }


    private fun switchToPart(
        part: Part,
    ) {
        skeleton.part.value = part
    }

    private fun createRequestFocus(
        part: Part,
    ): () -> Unit = {
        switchToPart(part)
        requestFocus()
    }

    private fun createGoForward(
        from: Part,
    ): () -> Unit = {
        from
            .shift(1)
            .foldNullable(
                ifNull = goForward,
                ifNotNull = skeleton.part::value::set,
            )
    }

    val from = AccountModel(
        scope = scope,
        skeleton = skeleton.from,
        dependencies = dependencies.account(),
        isFocused = createIsFocused(Part.From),
        requestFocus = createRequestFocus(Part.From),
        goForward = createGoForward(Part.From),
        useMostPopularAccountAsDefault = true,
    )

    val to = AccountModel(
        scope = scope,
        skeleton = skeleton.to,
        dependencies = dependencies.account(),
        isFocused = createIsFocused(Part.To),
        requestFocus = createRequestFocus(Part.To),
        goForward = createGoForward(Part.To),
        useMostPopularAccountAsDefault = false,
    )

    val amount = AmountModel(
        scope = scope,
        skeleton = skeleton.amount,
        isFocused = createIsFocused(Part.Amount),
        requestFocus = createRequestFocus(Part.Amount),
        goForward = createGoForward(Part.Amount),
    )

    class Page(
        scope: CoroutineScope,
        val page: StateFlow<PageType>,
    ) {

        val goBackHandler: GoBackHandler =
            page.flatMapState(scope, PageType::goBackHandler)
    }

    fun createPage(
        scope: CoroutineScope,
    ): Page = Page(
        scope = scope,
        page = skeleton
            .part
            .mapWithScope(scope) { partScope, part ->
                when (part) {
                    Part.From -> PageType.From(
                        model = from.createPage(
                            scope = partScope,
                        ),
                    )

                    Part.To -> PageType.To(
                        model = to.createPage(
                            scope = partScope,
                        ),
                    )

                    Part.Amount -> PageType.Amount(
                        model = amount.createPage(
                            scope = partScope,
                        ),
                    )
                }
            },
    )

    internal val transfer: StateFlow<Editable<TransactionInfo.Type.Transfer>> = from
        .accountEditable
        .combineEditableWith(
            scope = scope,
            other = to.accountEditable,
            combine = ::Pair,
        )
        .combineEditableWith(
            scope = scope,
            other = amount.amountEditable,
        ) { (from, to), amount ->
            TransactionInfo.Type.Transfer(
                to = to,
                from = from,
                amount = amount,
            )
        }

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = skeleton
        .part
        .flatMapWithScope(scope) { partScope, part ->
            when (part) {
                Part.From -> from.goBackHandler
                Part.To -> to.goBackHandler
                Part.Amount -> amount.goBackHandler
            }
        }
}