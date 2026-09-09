package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import arrow.core.NonEmptyList
import kotlinx.coroutines.CoroutineScope
import org.hnau.commons.app.projector.fractal.DialogContentInfo
import org.hnau.commons.app.projector.fractal.SButton
import org.hnau.commons.app.projector.fractal.SContentWithActions
import org.hnau.commons.app.projector.fractal.SDialog
import org.hnau.commons.app.projector.fractal.SIcon
import org.hnau.commons.app.projector.fractal.SItem
import org.hnau.commons.app.projector.fractal.SLine
import org.hnau.commons.app.projector.fractal.SPanel
import org.hnau.commons.app.projector.fractal.SScreen
import org.hnau.commons.app.projector.fractal.SText
import org.hnau.commons.app.projector.fractal.context.FContext
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.table.STableHeader
import org.hnau.commons.app.projector.fractal.table.Subtable
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.fractal.table.lazy.item
import org.hnau.commons.app.projector.fractal.utils.Importance
import org.hnau.commons.app.projector.fractal.utils.Mood
import org.hnau.commons.app.projector.uikit.ItemsRow
import org.hnau.commons.app.projector.uikit.line.weight
import org.hnau.commons.app.projector.utils.Drawable
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.TitleOrIcon
import org.hnau.commons.gen.pipe.annotations.Pipe
import org.hnau.commons.kotlin.KeyValue
import org.hnau.commons.kotlin.coroutines.ActionOrElse
import org.hnau.commons.kotlin.coroutines.flow.state.mapState
import org.hnau.commons.kotlin.coroutines.instant
import org.hnau.commons.kotlin.it
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.Amount
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Record
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.data.records.FilteredRecords
import org.hnau.pinfin.data.sum
import org.hnau.pinfin.model.TransactionViewModel
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.resolvedDirection
import org.hnau.pinfin.model.utils.totalAmount
import org.hnau.pinfin.projector.utils.AccountContent
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.ArrowDirection
import org.hnau.pinfin.projector.utils.ArrowIcon
import org.hnau.pinfin.projector.utils.CategoryContent
import org.hnau.pinfin.projector.utils.ViewMode
import org.hnau.pinfin.projector.utils.formatter.AmountFormatter
import org.hnau.pinfin.projector.utils.formatter.datetime.DateTimeFormatter

class TransactionViewProjector(
    scope: CoroutineScope,
    private val model: TransactionViewModel,
    private val dependencies: Dependencies,
) {

    @Pipe
    interface Dependencies {

        val localization: Localization

        val dateTimeFormatter: DateTimeFormatter

        val amountFormatter: AmountFormatter
    }

    private val removeDialog = model
        .removeDialog
        .mapState(scope) { removeOrNull ->
            removeOrNull?.let { remove ->
                DialogContentInfo(
                    content = {
                        SText(dependencies.localization.removeTransaction)
                    },
                    actions = {
                        FContext(
                            update = {
                                copy(
                                    mood = Mood.Error,
                                )
                            }
                        ) {
                            Action(
                                actionOrElseOrDisabled = remove.remove.collectAsState().value,
                                titleOrIcon = TitleOrIcon.Both(
                                    title = dependencies.localization.yes,
                                    icon = Drawable.Vector(Icons.Default.Delete)
                                )
                            )
                        }
                        Action(
                            importanceToActivate = Importance.Tertiary,
                            actionOrElseOrDisabled = ActionOrElse.instant(remove.cancel),
                            titleOrIcon = TitleOrIcon.Both(
                                title = dependencies.localization.cancel,
                                icon = Drawable.Vector(Icons.Default.Cancel),
                            ),
                        )
                    },
                    cancel = remove.cancel,
                )
            }
        }

    @Composable
    fun Content(
        contentPadding: PaddingValues,
    ) {
        SScreen(
            contentPadding = contentPadding,
            title = { SText(dependencies.localization.transaction) },
            actions = {
                FContext(
                    update = {
                        copy(
                            mood = Mood.Error,
                        )
                    }
                ) {
                    Action(
                        actionOrElseOrDisabled = ActionOrElse.instant { model.remove() },
                        titleOrIcon = TitleOrIcon.Icon(Drawable.Vector(Icons.Default.Delete))
                    )
                }
            },
        ) {
            SContentWithActions(
                content = {
                    val transaction = model.transaction
                    val currency = model.currency.collectAsState().value
                    SLazyTable(
                        orientation = Orientation.Vertical,
                    ) {
                        cell(key = "base") {

                            val transferVariant: Transaction.Type.Transfer<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>>? =
                                transaction.type.foldRaw(
                                    ifEntry = { null },
                                    ifTransfer = ::it,
                                )

                            val total = amount(
                                transaction = transaction,
                                currency = currency,
                            )
                            val comment = transaction.comment.text.takeIf(String::isNotEmpty)
                            STable(
                                orientation = Orientation.Vertical,
                            ) {
                                Subtable {
                                    Subtable {
                                        SCell {
                                            SPanel {
                                                SText(dependencies.localization.date)
                                            }
                                        }
                                        comment?.let {
                                            SCell {
                                                SPanel {
                                                    SText(dependencies.localization.comment)
                                                }
                                            }
                                        }
                                        transferVariant?.let {
                                            SCell {
                                                SPanel {
                                                    SText(dependencies.localization.transfer)
                                                }
                                            }
                                        }
                                        SCell {
                                            SPanel {
                                                SText(dependencies.localization.amount)
                                            }
                                        }
                                    }
                                    Subtable(
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        SCell {
                                            SPanel {
                                                SText(
                                                    dependencies
                                                        .dateTimeFormatter
                                                        .formatDate(transaction.timestamp),
                                                )
                                            }
                                        }
                                        comment?.let { commentNotNull ->
                                            SCell {
                                                SPanel {
                                                    SText(commentNotNull)
                                                }
                                            }
                                        }
                                        transferVariant?.let { transfer ->
                                            SCell {
                                                SPanel {
                                                    TransferContent(transfer = transfer)
                                                }
                                            }
                                        }
                                        SCell {
                                            SPanel {
                                                AmountContent(
                                                    value = total,
                                                    amountFormatter = dependencies.amountFormatter,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        transaction.type.foldRaw(
                            ifTransfer = {},
                            ifEntry = { entry ->
                                EntrySections(
                                    entry = entry,
                                    currency = currency,
                                )
                            },
                        )
                    }
                },
                actions = {
                    SButton(
                        actionOrElseOrDisabled = ActionOrElse.instant { model.edit() },
                        titleOrIcon = TitleOrIcon.Both(
                            title = dependencies.localization.edit,
                            icon = Drawable.Vector(Icons.Default.Edit),
                        )
                    )
                },
            )
            SDialog(
                info = removeDialog,
            )
        }
    }

    private fun SLazyTableScope.EntrySections(
        entry: Transaction.Type.Entry<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, FilteredRecords<KeyValue<CategoryId, CategoryInfo>>>,
        currency: Currency,
    ) {
        val additional = entry.records.additional
        val additionalPresent = additional.isNotEmpty()
        RecordsSection(
            sectionKey = "main",
            title = dependencies.localization.records,
            headerAmount = additionalPresent.takeIf { it }?.let {
                entry.records.main.sumAmount(currency)
            },
            records = entry.records.main,
            currency = currency,
        )
        if (additionalPresent) {
            RecordsSection(
                sectionKey = "additional",
                title = dependencies.localization.additionalRecords,
                headerAmount = additional.sumAmount(currency),
                records = additional,
                currency = currency,
            )
        }
    }

    private fun SLazyTableScope.RecordsSection(
        sectionKey: String,
        title: String,
        headerAmount: KeyValue<AmountDirection, Amount>?,
        records: List<Record<KeyValue<CategoryId, CategoryInfo>>>,
        currency: Currency,
    ) {
        item(key = "header_$sectionKey") {
            STableHeader {
                SItem(
                    content = {
                        SText(title)
                    },
                    endAccessory = headerAmount?.let { amount ->
                        {
                            AmountContent(
                                value = amount,
                                amountFormatter = dependencies.amountFormatter,
                            )
                        }
                    },
                )
            }
        }
        cells(
            items = records,
            key = { record -> record.category.key.id + records.indexOf(record) },
        ) { record ->
            SPanel {
                SItem(
                    content = record
                        .comment
                        .text
                        .takeIf(String::isNotEmpty)
                        ?.let { comment ->
                            { SText(comment) }
                        },
                    endAccessory = {
                        SLine(
                            orientation = Orientation.Horizontal,
                            separation = LocalDistance.current.units.padding.along.small,
                            acrossOrientation = Alignment.CenterHorizontally,
                        ) {
                            CategoryContent(
                                info = record.category,
                                localization = dependencies.localization,
                                viewMode = ViewMode.Full,
                            )
                            SIcon(
                                Drawable.Vector(
                                    ArrowIcon[
                                        record.category.key.direction.fold(
                                            ifCredit = { ArrowDirection.EndToStart },
                                            ifDebit = { ArrowDirection.StartToEnd },
                                        )
                                    ]
                                ),
                            )
                            AmountContent(
                                value = KeyValue(
                                    key = record.resolvedDirection,
                                    value = record.amount.toAmount(currency.scale),
                                ),
                                amountFormatter = dependencies.amountFormatter,
                            )
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun TransferContent(
        transfer: Transaction.Type.Transfer<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>>,
    ) {
        ItemsRow {
            AccountContent(
                info = transfer.from.value,
                localization = dependencies.localization,
                viewMode = ViewMode.Full,
            )
            Icon(
                icon = ArrowIcon[ArrowDirection.StartToEnd],
            )
            AccountContent(
                info = transfer.to.value,
                localization = dependencies.localization,
                viewMode = ViewMode.Full,
            )
        }
    }

    private fun amount(
        transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, FilteredRecords<KeyValue<CategoryId, CategoryInfo>>>,
        currency: Currency,
    ): KeyValue<AmountDirection, Amount> = transaction.type.fold(
        ifTransfer = { _, _, amount ->
            KeyValue(
                key = AmountDirection.Credit,
                value = amount.toAmount(currency.scale),
            )
        },
        ifEntry = { _, records ->
            records.totalAmount(currency)
        },
    )

    private fun List<Record<KeyValue<CategoryId, CategoryInfo>>>.sumAmount(
        currency: Currency,
    ): KeyValue<AmountDirection, Amount> = fold(
        initial = KeyValue(
            key = AmountDirection.Credit,
            value = Amount.zero,
        ),
    ) { acc, record ->
        acc + KeyValue(
            key = record.resolvedDirection,
            value = record.amount.toAmount(currency.scale),
        )
    }
}