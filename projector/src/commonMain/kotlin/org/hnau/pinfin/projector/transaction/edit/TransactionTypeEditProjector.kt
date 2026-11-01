package org.hnau.pinfin.projector.transaction.edit

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.STabs
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.gen.sealup.annotations.SealUp
import org.hnau.commons.gen.sealup.annotations.Variant
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.ZipList
import org.hnau.commons.kotlin.coroutines.flow.state.flatMapWithScope
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.pinfin.data.TransactionType
import org.hnau.pinfin.data.nonEmptyEntries
import org.hnau.pinfin.model.transaction.edit.TransactionTypeEditModel
import org.hnau.pinfin.model.transaction.edit.fold
import org.hnau.pinfin.model.transaction.utils.RecordId
import org.hnau.pinfin.projector.Localization
import org.hnau.pinfin.projector.transaction.edit.utils.TransactionSection
import org.hnau.pinfin.projector.utils.title

class TransactionTypeEditProjector(
    scope: CoroutineScope,
    private val model: TransactionTypeEditModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        fun entry(): TransactionEntryEditProjector.Dependencies

        fun transfer(): TransactionTransferEditProjector.Dependencies
    }

    @SealUp(
        variants = [
            Variant(
                type = TransactionEntryEditProjector::class,
                identifier = "entry",
            ),
            Variant(
                type = TransactionTransferEditProjector::class,
                identifier = "transfer",
            ),
        ],
        sealedInterfaceName = "TransactionTypeVariantProjector",
        wrappedValuePropertyName = "projector",
    )
    interface Variant {

        companion object
    }

    val section: TransactionSection<Either<KeyValue<ZipList<KeyValue<RecordId, TransactionRecordEditProjector>>, SLazyTableScope.(ZipList<KeyValue<RecordId, TransactionRecordEditProjector>>) -> Unit>, KeyValue<Unit, SLazyTableScope.(Unit) -> Unit>>> = TransactionSection(
        state = model
            .type
            .flatMapWithScope(scope) { scope, typeModel ->
                typeModel.fold(
                    ifEntry = { entryModel ->
                        TransactionTypeVariantProjector
                            .Entry(
                                projector = TransactionEntryEditProjector(
                                    scope = scope,
                                    model = entryModel,
                                    dependencies = dependencies.entry(),
                                ),
                            )
                            .projector
                            .section
                            .let { section ->
                                section.state.mapState(scope) {
                                    Either.Left(
                                        KeyValue(
                                            key = it,
                                            value = section.content,
                                        )
                                    )
                                }
                            }
                    },
                    ifTransfer = { transferModel ->
                        TransactionTypeVariantProjector
                            .Transfer(
                                projector = TransactionTransferEditProjector(
                                    scope = scope,
                                    model = transferModel,
                                    dependencies = dependencies.transfer(),
                                ),
                            )
                            .projector
                            .section
                            .let { section ->
                                section.state.mapState(scope) {
                                    Either.Right(
                                        KeyValue(
                                            key = it,
                                            value = section.content,
                                        )
                                    )
                                }
                            }
                    },
                )
            },
        content = { variant ->
            cell(key = "type") {
                SPanel {
                    val variant by model.typeVariant.collectAsState()
                    SItem(
                        endAccessory = {
                            STabs(
                                items = TransactionType.nonEmptyEntries,
                                getSelection = { variant },
                                onSelectionChanged = model::setType,
                            ) { type ->
                                SText(type.title(dependencies.localization))
                            }
                        }
                    ) {
                        SText(dependencies.localization.type)
                    }
                }
            }
            variant.fold(
                ifLeft = { (entryState, entryContent) ->
                    entryContent(entryState)
                },
                ifRight = { (transferState, transferContent) ->
                    transferContent(transferState)
                }
            )
        }
    )
}
