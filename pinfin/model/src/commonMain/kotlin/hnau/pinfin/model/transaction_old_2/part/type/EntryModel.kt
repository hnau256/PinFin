package hnau.pinfin.model.transaction_old_2.part.type

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.kotlin.coroutines.combineState
import hnau.common.kotlin.coroutines.flatMapState
import hnau.common.kotlin.coroutines.mapState
import hnau.common.kotlin.coroutines.mapWithScope
import hnau.common.kotlin.coroutines.scopedInState
import hnau.common.kotlin.coroutines.toMutableStateFlowAsInitial
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.getOrInit
import hnau.common.kotlin.toAccessor
import hnau.pinfin.model.transaction_old_2.page.type.EntryPageModel
import hnau.pinfin.model.transaction_old_2.page.type.TypePageModel
import hnau.pinfin.model.transaction_old_2.page.type.entry.EntryPagePageModel
import hnau.pinfin.model.transaction_old_2.part.type.entry.AccountPartModel
import hnau.pinfin.model.transaction_old_2.part.type.entry.EntryPart
import hnau.pinfin.model.transaction_old_2.part.type.entry.record.RecordsPartModel
import hnau.pinfin.model.utils.budget.state.TransactionInfo
import hnau.pinfin.model.utils.flatMapWithScope
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class EntryModel(
    private val scope: CoroutineScope,
    private val dependencies: Dependencies,
    private val skeleton: Skeleton,
    private val requestFocus: () -> Unit,
    private val isFocused: StateFlow<Boolean>,
) {

    @Pipe
    interface Dependencies {

        fun page(): EntryPageModel.Dependencies

        fun records(): RecordsPartModel.Dependencies

        fun account(): AccountPartModel.Dependencies
    }

    @Serializable
    data class Skeleton(
        var page: EntryPageModel.Skeleton? = null,
        val selectedPart: MutableStateFlow<EntryPart> =
            EntryPart.default.toMutableStateFlowAsInitial(),
        val records: RecordsPartModel.Skeleton,
        val account: AccountPartModel.Skeleton,
    ) {

        companion object {

            fun createForNew(): Skeleton = Skeleton(
                records = RecordsPartModel.Skeleton.createForNew(),
                account = AccountPartModel.Skeleton.createForNew(),
            )

            fun createForEdit(
                type: TransactionInfo.Type.Entry,
            ): Skeleton = Skeleton(
                records = RecordsPartModel.Skeleton.createForEdit(
                    records = type.records,
                ),
                account = AccountPartModel.Skeleton.createForEdit(
                    account = type.account,
                ),
            )
        }
    }

    private fun switchToPart(
        part: EntryPart,
    ) {
        skeleton.selectedPart.value = part
    }

    private fun createRequestFocus(
        part: EntryPart,
    ): () -> Unit = {
        switchToPart(part)
        requestFocus()
    }

    private fun isFocused(
        part: EntryPart,
    ): StateFlow<Boolean> = combineState(
        scope = scope,
        a = isFocused,
        b = skeleton.selectedPart,
    ) { isFocused, selectedPart ->
        isFocused && part == selectedPart
    }

    val records = RecordsPartModel(
        scope = scope,
        dependencies = dependencies.records(),
        skeleton = skeleton.records,
        requestFocus = createRequestFocus(EntryPart.Records),
        isFocused = isFocused(EntryPart.Records),
    )

    val account = AccountPartModel(
        scope = scope,
        dependencies = dependencies.account(),
        skeleton = skeleton.account,
        requestFocus = createRequestFocus(EntryPart.Account),
        isFocused = isFocused(EntryPart.Account),
    )

    fun createPage(
        scope: CoroutineScope,
    ): TypePageModel = EntryPageModel(
        scope = scope,
        dependencies = dependencies.page(),
        skeleton = skeleton::page
            .toAccessor()
            .getOrInit { EntryPageModel.Skeleton() },
        page = skeleton
            .selectedPart
            .mapWithScope(scope) { pageScope, part ->
                when (part) {
                    EntryPart.Records -> records.createPage(
                        scope = pageScope,
                    )

                    EntryPart.Account -> account.createPage(
                        scope = pageScope,
                    )
                }
            },
    )

    private fun EntryPart.shift(
        offset: Int,
    ): EntryPart? = EntryPart
        .entries
        .getOrNull(ordinal + offset)

    val page: StateFlow<Pair<EntryPart, EntryPagePageModel>> = skeleton
        .selectedPart
        .mapWithScope(scope) { pageScope, part ->
            val model = when (part) {
                EntryPart.Records -> records.createPage(
                    scope = pageScope,
                )

                EntryPart.Account -> account.createPage(
                    scope = pageScope,
                )
            }
            part to model
        }

    val goBackHandler: GoBackHandler = page.flatMapWithScope(scope) { pageScope, partWithPage ->
            val (part, pageModel) = partWithPage
            pageModel.goBackHandler.flatMapWithScope(pageScope) { goBackScope, goBack ->
                    goBack.foldNullable(
                        ifNotNull = { it.toMutableStateFlowAsInitial() },
                        ifNull = {
                            page.mapState(goBackScope) { (part, _) ->
                                part
                                    .shift(-1)
                                    ?.let { previousPart ->
                                        { skeleton.selectedPart.value = previousPart }
                                    }
                            }
                        },
                    )
                }
        }
}