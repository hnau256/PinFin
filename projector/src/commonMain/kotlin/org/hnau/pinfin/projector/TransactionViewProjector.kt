package org.hnau.pinfin.projector

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
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
import org.hnau.commons.app.projector.fractal.context.LocalFContext
import org.hnau.commons.app.projector.fractal.context.color
import org.hnau.commons.app.projector.fractal.context.contentOverlay
import org.hnau.commons.app.projector.fractal.distance.LocalDistance
import org.hnau.commons.app.projector.fractal.padding.LocalContentPaddingBox
import org.hnau.commons.app.projector.fractal.size.SizeType
import org.hnau.commons.app.projector.fractal.size.units
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.table.STableScope
import org.hnau.commons.app.projector.fractal.table.Subtable
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTable
import org.hnau.commons.app.projector.fractal.table.lazy.SLazyTableScope
import org.hnau.commons.app.projector.fractal.table.lazy.cell
import org.hnau.commons.app.projector.fractal.table.lazy.cells
import org.hnau.commons.app.projector.fractal.table.lazy.separator
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
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.plus
import org.hnau.pinfin.data.records.FilteredRecord
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

                            STable(
                                orientation = Orientation.Vertical,
                            ) {
                                HeaderRow(
                                    label = dependencies.localization.date,
                                ) {
                                    SText(
                                        dependencies
                                            .dateTimeFormatter
                                            .formatDate(transaction.timestamp),
                                    )
                                }
                                transaction
                                    .comment
                                    .text
                                    .takeIf(String::isNotEmpty)
                                    ?.let {
                                        HeaderRow(
                                            label = dependencies.localization.comment,
                                        ) {
                                            SText(it)
                                        }
                                    }
                                transaction
                                    .type
                                    .foldRaw(
                                        ifEntry = { null },
                                        ifTransfer = ::it,
                                    )
                                    ?.let { transfer ->
                                        HeaderRow(
                                            label = dependencies.localization.transfer,
                                        ) {
                                            TransferContent(transfer = transfer)
                                        }
                                    }
                                transaction
                                    .type
                                    .foldRaw(
                                        ifEntry = { it.account },
                                        ifTransfer = { null },
                                    )
                                    ?.let { account ->
                                        HeaderRow(
                                            label = dependencies.localization.account,
                                        ) {
                                            AccountContent(
                                                info = account.value,
                                                localization = dependencies.localization,
                                                viewMode = ViewMode.Full,
                                            )
                                        }
                                    }
                                HeaderRow(
                                    label = dependencies.localization.amount,
                                ) {
                                    transaction.type.foldRaw(
                                        ifTransfer = {
                                            AmountContent(
                                                value = amount(
                                                    transaction = transaction,
                                                    currency = currency,
                                                ),
                                                amountFormatter = dependencies.amountFormatter,
                                            )
                                        },
                                        ifEntry = { entry ->
                                            EntryAmount(
                                                records = entry.records,
                                                currency = currency,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                        separator()
                        transaction.type.foldRaw(
                            ifTransfer = {},
                            ifEntry = { entry ->
                                RecordsSection(
                                    records = entry.records,
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

    @Composable
    private fun STableScope.HeaderRow(
        label: String,
        content: @Composable () -> Unit,
    ) {
        Subtable {
            SCell(modifier = Modifier.weight(1f)) {
                SPanel {
                    SText(label)
                }
            }
            SCell(modifier = Modifier.weight(1f)) {
                SPanel {
                    content()
                }
            }
        }
    }

    private fun SLazyTableScope.RecordsSection(
        records: List<FilteredRecord<KeyValue<CategoryId, CategoryInfo>>>,
        currency: Currency,
    ) {
        cells(
            count = records.size,
            key = { index -> records[index].record.category.key.id + index },
        ) { index ->
            val record = records[index]
            SPanel {
                SItem(
                    content = record
                        .record
                        .comment
                        .text
                        .takeIf(String::isNotEmpty)
                        ?.let { comment ->
                            {
                                CommentText(
                                    text = comment,
                                    strikethrough = !record.included,
                                )
                            }
                        },
                    endAccessory = {
                        SLine(
                            orientation = Orientation.Horizontal,
                            separation = LocalDistance.current.units.padding.along.small,
                            acrossOrientation = Alignment.CenterHorizontally,
                        ) {
                            CategoryContent(
                                info = record.record.category,
                                localization = dependencies.localization,
                                viewMode = ViewMode.Full,
                            )
                            SIcon(
                                Drawable.Vector(
                                    ArrowIcon[
                                        record.record.category.key.direction.fold(
                                            ifCredit = { ArrowDirection.EndToStart },
                                            ifDebit = { ArrowDirection.StartToEnd },
                                        )
                                    ]
                                ),
                            )
                            AmountContent(
                                value = KeyValue(
                                    key = record.record.resolvedDirection,
                                    value = record.record.amount.toAmount(currency.scale),
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
    private fun CommentText(
        text: String,
        strikethrough: Boolean,
    ) {
        if (!strikethrough) {
            SText(text)
            return
        }
        LocalContentPaddingBox {
            FContext(
                update = { contentOverlay() }
            ) {
                val fContext = LocalFContext.current
                BasicText(
                    text = text,
                    style = LocalDistance.current.units.textStyle[SizeType.default].merge(
                        color = fContext.color,
                        textDecoration = TextDecoration.LineThrough,
                    ),
                )
            }
        }
    }

    @Composable
    private fun EntryAmount(
        records: List<FilteredRecord<KeyValue<CategoryId, CategoryInfo>>>,
        currency: Currency,
    ) {
        val hasIncluded = records.any { it.included }
        val hasExcluded = records.any { !it.included }
        if (!hasIncluded || !hasExcluded) {
            AmountContent(
                value = records.sumAmount(currency),
                amountFormatter = dependencies.amountFormatter,
            )
            return
        }
        SLine(
            orientation = Orientation.Horizontal,
            separation = LocalDistance.current.units.padding.along.small,
            acrossOrientation = Alignment.CenterHorizontally,
        ) {
            AmountContent(
                value = records.filter { it.included }.sumAmount(currency),
                amountFormatter = dependencies.amountFormatter,
            )
            SText("+")
            AmountContent(
                value = records.filterNot { it.included }.sumAmount(currency),
                amountFormatter = dependencies.amountFormatter,
            )
            SText("=")
            AmountContent(
                value = records.sumAmount(currency),
                amountFormatter = dependencies.amountFormatter,
            )
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
        transaction: Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, FilteredRecord<KeyValue<CategoryId, CategoryInfo>>>,
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

    private fun List<FilteredRecord<KeyValue<CategoryId, CategoryInfo>>>.sumAmount(
        currency: Currency,
    ): KeyValue<AmountDirection, Amount> = fold(
        initial = KeyValue(
            key = AmountDirection.Credit,
            value = Amount.zero,
        ),
    ) { acc, record ->
        acc + KeyValue(
            key = record.record.resolvedDirection,
            value = record.record.amount.toAmount(currency.scale),
        )
    }
}