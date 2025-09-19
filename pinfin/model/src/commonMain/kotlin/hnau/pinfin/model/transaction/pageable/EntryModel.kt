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
import hnau.pinfin.data.Amount
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

class EntryModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val isFocused: StateFlow<Boolean>,
    private val requestFocus: () -> Unit,
    private val goForward: () -> Unit,
) {

    enum class Part {

        Records, Account;

        companion object {

            val default: Part
                get() = Records
        }
    }

    sealed interface PageType {

        val key: Int

        val goBackHandler: GoBackHandler

        data class Account(
            val model: ChooseOrCreateModel<AccountInfo>,
        ) : PageType {
            override val key: Int
                get() = 0

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }

        data class Records(
            val model: RecordsModel.Page,
        ) : PageType {
            override val key: Int
                get() = 1

            override val goBackHandler: GoBackHandler
                get() = model.goBackHandler
        }
    }

    @Pipe
    interface Dependencies {

        fun account(): AccountModel.Dependencies

        fun records(): RecordsModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        val part: MutableStateFlow<Part> = Part.default.toMutableStateFlowAsInitial(),
        val account: AccountModel.Skeleton,
        val records: RecordsModel.Skeleton,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                account = AccountModel.Skeleton.createForNew(),
                records = RecordsModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                entry: TransactionInfo.Type.Entry,
            ): Skeleton = Skeleton(
                account = AccountModel.Skeleton.createForEdit(
                    account = entry.account,
                ),
                records = RecordsModel.Skeleton.createForEdit(
                    records = entry.records,
                ),
            )
        }
    }

    private fun createIsFocused(
        part: Part,
    ): StateFlow<Boolean> = isFocused
        .flatMapWithScope(scope) { scope, isFocused ->
            isFocused.foldBoolean(
                ifFalse = { false.toMutableStateFlowAsInitial() },
                ifTrue = {
                    skeleton
                        .part
                        .mapState(scope) { it == part }
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

    val account = AccountModel(
        scope = scope,
        skeleton = skeleton.account,
        dependencies = dependencies.account(),
        isFocused = createIsFocused(Part.Account),
        requestFocus = createRequestFocus(Part.Account),
        goForward = createGoForward(Part.Account),
        useMostPopularAccountAsDefault = true,
    )

    val records = RecordsModel(
        scope = scope,
        skeleton = skeleton.records,
        dependencies = dependencies.records(),
        isFocused = createIsFocused(Part.Records),
        requestFocus = createRequestFocus(Part.Records),
        goForward = createGoForward(Part.Records),
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
            .mapWithScope(scope) { scope, part ->
                when (part) {
                    Part.Account -> PageType.Account(
                        model = account.createPage(
                            scope = scope,
                        ),
                    )

                    Part.Records -> PageType.Records(
                        model = records.createPage(
                            scope = scope,
                        ),
                    )
                }
            },
    )

    internal val entry: StateFlow<Editable<TransactionInfo.Type.Entry>> =
        records.records.combineEditableWith(
            scope = scope,
            other = account.accountEditable,
        ) { records, account ->
            TransactionInfo.Type.Entry(
                records = records,
                account = account,
            )
        }

    val amountOrZero: StateFlow<Amount>
        get() = records.amountOrZero

    private fun Part.shift(
        offset: Int,
    ): Part? = Part
        .entries
        .getOrNull(ordinal + offset)

    val goBackHandler: GoBackHandler = skeleton
        .part
        .flatMapWithScope(scope) { scope, part ->
            when (part) {
                Part.Account -> account.goBackHandler
                Part.Records -> records.goBackHandler
            }
        }
}