package hnau.pinfin.model.transaction.page.type.entry

import hnau.common.app.model.goback.GoBackHandler
import hnau.common.app.model.goback.NeverGoBackHandler
import hnau.pinfin.model.transaction.utils.NavAction
import hnau.pipe.annotations.Pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

class RecordsPageModel(
    scope: CoroutineScope,
    dependencies: Dependencies,
    skeleton: Skeleton,
    navAction: NavAction,
    records: MutableStateFlow<List<Unit>>,
): EntryPageModel {

    @Pipe
    interface Dependencies

    @Serializable
    /*data*/ class Skeleton

    override val goBackHandler: GoBackHandler
        get() = NeverGoBackHandler //TODO
}