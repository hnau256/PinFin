package org.hnau.pinfin.model.utils.analytics

import org.hnau.commons.gen.fold.annotations.Fold
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo

/**
 * Ключ группы для [calcPeriod]: то, по чему разбиваются суммы на странице аналитики —
 * категория (в т.ч. «без категории»), счёт, либо [None] — без разбивки, единственная строка «итог».
 *
 * Не персистится, поэтому не `@Serializable`.
 */
@Fold
sealed interface GroupKey {

    data object None : GroupKey

    data class Category(
        val idWithCategory: KeyValue<CategoryId, CategoryInfo>?,
    ) : GroupKey

    data class Account(
        val idWithAccount: KeyValue<AccountId, AccountInfo>,
    ) : GroupKey
}
