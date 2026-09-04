@file:UseSerializers(
    MutableStateFlowSerializer::class,
)

package org.hnau.pinfin.model.budget.analytics.tab.periods.configure

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.hnau.commons.app.model.goback.GoBackHandler
import org.hnau.commons.app.model.utils.Editable
import org.hnau.commons.app.model.utils.editable
import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.coroutines.flow.state.derivedStateFlowOf
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.flow.state.mutable.toMutableStateFlowAsInitial
import org.hnau.commons.kotlin.foldBoolean
import org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer
import org.hnau.pinfin.model.budget.analytics.tab.periods.configure.period.PeriodConfigModel
import org.hnau.pinfin.model.filter.pageable.SelectAccountsModel
import org.hnau.pinfin.model.filter.pageable.SelectCategoriesModel
import org.hnau.pinfin.model.utils.analytics.AnalyticsConfig

/**
 * Экран настроек аналитики (docs/analytics-v2-plan.md, "Фаза 3", "Фаза 4"): собирает
 * [PeriodConfigModel] (период + якорь), [GroupByConfigModel], [OperationConfigModel], а также
 * ограничение по счетам/категориям ([SelectAccountsModel]/[SelectCategoriesModel], те же модели,
 * что использует `FilterModel`) в единый `Editable<AnalyticsConfig>`. FAB "Сохранить" активен
 * только когда результат корректен и отличается от исходного конфига (порт валид-и-изменён
 * гейта из старого `GraphConfigureModel`).
 */
class AnalyticsConfigureModel(
    private val scope: CoroutineScope,
    dependencies: Dependencies,
    private val skeleton: Skeleton,
    firstTransactionDate: LocalDate,
    lastTransactionDate: LocalDate,
    private val onReady: (AnalyticsConfig) -> Unit,
    private val onCancel: () -> Unit,
) {

    @Pipe
    interface Dependencies {

        fun accounts(): SelectAccountsModel.Dependencies

        fun categories(): SelectCategoriesModel.Dependencies
    }

    /** Какой из двух селекторов ограничения (счета / категории) сейчас развёрнут. */
    @Fold
    enum class SelectTab {
        Accounts,
        Categories,
    }

    @SealUp(
        variants = [
            Variant(
                type = SelectAccountsModel.Page::class,
                identifier = "accounts",
            ),
            Variant(
                type = SelectCategoriesModel.Page::class,
                identifier = "categories",
            ),
        ],
        wrappedValuePropertyName = "model",
        sealedInterfaceName = "SelectPageType",
    )
    interface SelectPage {

        companion object
    }

    @Serializable
    data class Skeleton(
        val initial: AnalyticsConfig,
        val period: PeriodConfigModel.Skeleton,
        val groupBy: GroupByConfigModel.Skeleton,
        val operation: OperationConfigModel.Skeleton,
        val accounts: SelectAccountsModel.Skeleton,
        val categories: SelectCategoriesModel.Skeleton,
        val selectedSelectTab: MutableStateFlow<SelectTab?> =
            null.toMutableStateFlowAsInitial(),
    ) {

        companion object {

            fun create(
                initial: AnalyticsConfig,
                firstTransactionDate: LocalDate,
                lastTransactionDate: LocalDate,
            ): Skeleton = Skeleton(
                initial = initial,
                period = PeriodConfigModel.Skeleton.create(
                    initial = initial.period,
                    firstTransactionDate = firstTransactionDate,
                    lastTransactionDate = lastTransactionDate,
                ),
                groupBy = GroupByConfigModel.Skeleton(
                    initial = initial.groupBy,
                ),
                operation = OperationConfigModel.Skeleton.create(
                    initial = initial.operation,
                ),
                accounts = SelectAccountsModel.Skeleton.create(
                    initialSelectedAccountsIds = initial.accounts,
                ),
                categories = SelectCategoriesModel.Skeleton.create(
                    initialSelectedCategoriesIds = initial.categories,
                ),
            )
        }
    }

    val period: PeriodConfigModel = PeriodConfigModel(
        scope = scope,
        skeleton = skeleton.period,
        firstTransactionDate = firstTransactionDate,
        lastTransactionDate = lastTransactionDate,
    )

    val groupBy: GroupByConfigModel = GroupByConfigModel(
        scope = scope,
        skeleton = skeleton.groupBy,
    )

    val operation: OperationConfigModel = OperationConfigModel(
        scope = scope,
        skeleton = skeleton.operation,
    )

    private fun createIsFocused(
        tab: SelectTab,
    ): StateFlow<Boolean> = skeleton
        .selectedSelectTab
        .mapState(scope) { it == tab }

    private fun createRequestFocus(
        tab: SelectTab,
    ): () -> Unit = {
        skeleton.selectedSelectTab.value = tab
    }

    val accounts: SelectAccountsModel = SelectAccountsModel(
        scope = scope,
        dependencies = dependencies.accounts(),
        skeleton = skeleton.accounts,
        isFocused = createIsFocused(SelectTab.Accounts),
        requestFocus = createRequestFocus(SelectTab.Accounts),
    )

    val categories: SelectCategoriesModel = SelectCategoriesModel(
        scope = scope,
        dependencies = dependencies.categories(),
        skeleton = skeleton.categories,
        isFocused = createIsFocused(SelectTab.Categories),
        requestFocus = createRequestFocus(SelectTab.Categories),
    )

    val selectedPage: StateFlow<SelectPageType?> = skeleton
        .selectedSelectTab
        .mapState(scope) { tabOrNull ->
            tabOrNull?.fold(
                ifAccounts = { SelectPage.accounts(accounts.createPage()) },
                ifCategories = { SelectPage.categories(categories.createPage()) },
            )
        }

    private val editableConfig: StateFlow<Editable<AnalyticsConfig>> = derivedStateFlowOf(scope) {
        editable {
            val period = period.editablePeriod.state.bind()
            val groupBy = groupBy.editableGroupBy.state.bind()
            val operation = operation.editableOperation.state.bind()
            val accountsSelection = accounts.selectedAccountsIds.state
            val categoriesSelection = categories.selectedCategoriesIds.state
            val accounts = Editable.Value(
                value = accountsSelection,
                changed = accountsSelection != skeleton.initial.accounts,
            ).bind()
            val categories = Editable.Value(
                value = categoriesSelection,
                changed = categoriesSelection != skeleton.initial.categories,
            ).bind()
            skeleton.initial.copy(
                period = period,
                groupBy = groupBy,
                operation = operation,
                accounts = accounts,
                categories = categories,
            )
        }
    }

    val save: StateFlow<(() -> Unit)?> = editableConfig.mapState(scope) { editableConfig ->
        when (editableConfig) {
            Editable.Incorrect -> null
            is Editable.Value -> editableConfig.changed.foldBoolean(
                ifTrue = { { onReady(editableConfig.value) } },
                ifFalse = { null },
            )
        }
    }

    val cancel: () -> Unit
        get() = onCancel

    val goBackHandler: GoBackHandler = skeleton
        .selectedSelectTab
        .mapState(scope) { tabOrNull ->
            tabOrNull?.let {
                { skeleton.selectedSelectTab.value = null }
            }
        }
}
