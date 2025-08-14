package hnau.pinfin.model.transaction.part.type.entry.record

import hnau.common.app.model.EditingString
import hnau.pinfin.data.AmountDirection
import hnau.pinfin.model.AmountModel
import hnau.pinfin.model.utils.budget.state.CategoryInfo
import kotlinx.coroutines.flow.MutableStateFlow

data class RecordInfo(
    val amount: AmountModel,
    val category: MutableStateFlow<CategoryInfo?>,
    val direction: MutableStateFlow<AmountDirection>,
    val comment: MutableStateFlow<EditingString>,
)