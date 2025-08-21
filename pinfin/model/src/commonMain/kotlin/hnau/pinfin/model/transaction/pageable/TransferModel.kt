@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package hnau.pinfin.model.transaction.pageable

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.serialization.MutableStateFlowSerializer
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction.utils.ChooseOrCreateModel
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

        fun amount(): AmountModel.Dependencies

        fun page(): Page.Dependencies
    }

    @Serializable
    data class Skeleton(
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        var page: Page.Skeleton? = null,
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
    ): StateFlow<Boolean> = isFocused
        .scopedInState(scope)
        .flatMapState(scope) { (isFocusedScope, isFocused) ->
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

    val from = AccountModel(
        scope = scope,
        skeleton = skeleton.from,
        dependencies = dependencies.account(),
        isFocused = createIsFocused(Part.From),
        requestFocus = createRequestFocus(Part.From),
    )

    val to = AccountModel(
        scope = scope,
        skeleton = skeleton.to,
        dependencies = dependencies.account(),
        isFocused = createIsFocused(Part.To),
        requestFocus = createRequestFocus(Part.To),
    )

    val amount = AmountModel(
        scope = scope,
        skeleton = skeleton.amount,
        dependencies = dependencies.amount(),
        isFocused = createIsFocused(Part.Amount),
        requestFocus = createRequestFocus(Part.Amount),
    )

    class Page(
        scope: CoroutineScope,
        dependencies: Dependencies,
        skeleton: Skeleton,
        val page: StateFlow<PageType>,
    ) {

        @Pipe
        interface Dependencies

        @Serializable
        /*data*/ class Skeleton

        val goBackHandler: GoBackHandler =
            page.flatMapState(scope, PageType::goBackHandler)
    }

    fun createPage(
        scope: CoroutineScope,
    ): Page = Page(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { Page.Skeleton() },
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

    val transfer: StateFlow<TransactionInfo.Type.Transfer?> = from
        .account
        .scopedInState(scope)
        .flatMapState(scope) { (fromScope, fromOrNull) ->
            fromOrNull.foldNullable(
                ifNull = { null.toMutableStateFlowAsInitial() },
                ifNotNull = { from ->
                    createTransferFromFrom(
                        scope = fromScope,
                        from = from,
                    )
                }
            )
        }

    private fun createTransferFromFrom(
        scope: CoroutineScope,
        from: AccountInfo,
    ): StateFlow<TransactionInfo.Type.Transfer?> = to
        .account
        .scopedInState(scope)
        .flatMapState(scope) { (fromScope, toOrNull) ->
            toOrNull.foldNullable(
                ifNull = { null.toMutableStateFlowAsInitial() },
                ifNotNull = { to ->
                    createTransferFromFromAndTo(
                        scope = fromScope,
                        to = to,
                        from = from,
                    )
                }
            )
        }

    private fun createTransferFromFromAndTo(
        scope: CoroutineScope,
        from: AccountInfo,
        to: AccountInfo,
    ): StateFlow<TransactionInfo.Type.Transfer?> = amount
        .amount
        .mapState(scope) { amountOrNull ->
            amountOrNull?.let { amount ->
                TransactionInfo.Type.Transfer(
                    to = to,
                    from = from,
                    amount = amount,
                )
            }
        }

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = skeleton
        .part
        .scopedInState(scope)
        .flatMapState(scope) { (partScope, part) ->
            val partGoBackHandler = when (part) {
                Part.From -> from.goBackHandler
                Part.To -> to.goBackHandler
                Part.Amount -> amount.goBackHandler
            }
            partGoBackHandler.mapState(partScope) { partGoBack ->
                partGoBack ?: part
                    .shift(-1)
                    ?.let { previousPart ->
                        { switchToPart(previousPart) }
                    }
            }
        }
}