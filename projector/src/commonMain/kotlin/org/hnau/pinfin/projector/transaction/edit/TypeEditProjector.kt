package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import arrow.core.nonEmptyListOf
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.STabs
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.model.transaction.edit.TransactionTypeEditModel
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.utils.title

class TypeEditProjector(
    private val model: TransactionTypeEditModel,
    private val localization: Localization,
) {

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
    ) {
        val variant = model.typeVariant.collectAsState().value
        SItem(
            modifier = modifier,
            topAccessory = {
                SText(localization.type)
            },
        ) {
            STabs(
                items = nonEmptyListOf(
                    TransactionType.Entry,
                    TransactionType.Transfer,
                ),
                getSelection = { variant },
                onSelectionChanged = model::setType,
            ) { type ->
                SText(type.title(localization))
            }
        }
    }
}
