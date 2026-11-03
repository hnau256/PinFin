package org.hnau.pinfin.projector.transaction.edit.utils.chooseorcreate

import org.hnau.pinfin.model.transaction.edit.utils.chooseorcreate.BaseOrAdditionalValues

data class ChooseOrCreateMessages(
    val sections: BaseOrAdditionalValues<Section>,
    val entityTypeName: String,
) {

    data class Section(
        val title: String,
        val noVariantsNoInput: String,
        val noVariantsWithInput: (input: String) -> String,
    )
}