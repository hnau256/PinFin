package org.hnau.pinfin.projector.budget.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.util.fastForEach
import arrow.core.nonEmptySetOf
import arrow.core.toNonEmptyListOrNull
import org.hnau.commons.app.projector.fractal.table.STable
import org.hnau.commons.app.projector.fractal.utils.rememberFShape
import org.hnau.commons.app.projector.uikit.utils.Dimens
import org.hnau.commons.app.projector.utils.Icon
import org.hnau.commons.app.projector.utils.Orientation
import org.hnau.commons.app.projector.utils.horizontalDisplayPadding
import org.hnau.commons.kotlin.KeyValue
import org.hnau.pinfin.data.AccountId
import org.hnau.pinfin.data.AmountDirection
import org.hnau.pinfin.data.CategoryId
import org.hnau.pinfin.data.Currency
import org.hnau.pinfin.data.Transaction
import org.hnau.pinfin.data.fold
import org.hnau.pinfin.data.foldRaw
import org.hnau.pinfin.data.records.Records
import org.hnau.pinfin.data.sum
import org.hnau.pinfin.model.utils.amount
import org.hnau.pinfin.model.utils.budget.state.AccountInfo
import org.hnau.pinfin.model.utils.budget.state.CategoryInfo
import org.hnau.pinfin.model.utils.resolvedDirectionedAmount
import org.hnau.pinfin.projector.utils.AccountContent
import org.hnau.pinfin.projector.utils.AmountContent
import org.hnau.pinfin.projector.utils.ArrowDirection
import org.hnau.pinfin.projector.utils.ArrowIcon
import org.hnau.pinfin.projector.utils.CategoryContent

typealias ResolvedTransaction = Transaction<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, Records<KeyValue<CategoryId, CategoryInfo>>>

@Composable
fun ResolvedTransaction.Content(
    dependencies: TransactionsProjector.Dependencies,
    currency: Currency,
    onClick: () -> Unit,
) {
    STable(
        orientation = Orientation.Horizontal,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalDisplayPadding(),
    ) {
        SCell {
            CellContent(
                dependencies = dependencies,
                currency = currency,
                onClick = onClick,
                shape = rememberFShape(),
            )
        }
    }
}

@Composable
fun ResolvedTransaction.CellContent(
    modifier: Modifier = Modifier,
    shape: Shape,
    dependencies: TransactionsProjector.Dependencies,
    currency: Currency,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimens.separation,
                vertical = Dimens.smallSeparation,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
        ) {
            TimestampContent(
                dependencies = dependencies,
            )
            type.foldRaw(
                ifEntry = { variant ->
                    EntryContent(
                        entry = variant,
                        dependencies = dependencies,
                    )
                },
                ifTransfer = { variant ->
                    TransferContent(
                        transfer = variant,
                        dependencies = dependencies,
                    )
                },
            )
            CommentContent()
        }

        AmountContent(
            value = type.fold(
                ifEntry = { _, records ->
                    records
                        .records
                        .map { record ->
                            record.resolvedDirectionedAmount.map { expression ->
                                expression.toAmount(currency.scale)
                            }
                        }
                        .sum()
                },
                ifTransfer = { _, _, amount ->
                    KeyValue(
                        key = AmountDirection.Credit,
                        value = amount.toAmount(currency.scale),
                    )
                }
            ),
            amountFormatter = dependencies.amountFormatter,
        )
    }
}

@Composable
private fun ResolvedTransaction.TimestampContent(
    dependencies: TransactionsProjector.Dependencies,
) {
    val text = remember(timestamp) {
        dependencies.dateTimeFormatter.formatDate(timestamp)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun ResolvedTransaction.CommentContent() {
    val primary = comment.text.takeIf(String::isNotEmpty)
    val secondary = remember(type) {
        type.fold(
            ifTransfer = { _, _, _ -> null },
            ifEntry = { _, records ->
                records
                    .records
                    .mapNotNull { record ->
                        record.comment.text.takeIf(String::isNotEmpty)
                    }
                    .toNonEmptyListOrNull()
                    ?.joinToString(separator = ", ")
            },
        )
    }
    val comment = remember(primary, secondary) {
        listOfNotNull(
            primary,
            secondary,
        )
            .toNonEmptyListOrNull()
            ?.joinToString(separator = ": ")
    } ?: return
    Text(
        text = comment,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun EntryContent(
    dependencies: TransactionsProjector.Dependencies,
    entry: Transaction.Type.Entry<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>, Records<KeyValue<CategoryId, CategoryInfo>>>,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
    ) {
        val records = entry.records.records
        val categories = remember(records) {
            records
                .tail
                .fold(
                    initial = nonEmptySetOf(records.head.category),
                ) { acc, record ->
                    acc + record.category
                }
                .toNonEmptyList()
        }
        AccountContent(
            info = entry.account.value,
            localization = dependencies.localization,
        )
        Icon(
            tint = MaterialTheme.colorScheme.onSurface,
            icon = ArrowIcon[
                remember(records) {
                    val allDirection = records
                        .map {
                            it.category.key.direction
                        }
                        .let { directions ->
                            directions.tail.fold<AmountDirection, AmountDirection?>(
                                initial = directions.head,
                            ) { acc, direction ->
                                acc?.takeIf { it == direction }
                            }
                        }
                    when (allDirection) {
                        AmountDirection.Credit -> ArrowDirection.EndToStart
                        AmountDirection.Debit -> ArrowDirection.StartToEnd
                        null -> ArrowDirection.Both
                    }
                }
            ],
        )
        categories.fastForEach { idWithCategory ->
            CategoryContent(
                info = idWithCategory,
                localization = dependencies.localization,
            )
        }
    }
}

@Composable
private fun TransferContent(
    transfer: Transaction.Type.Transfer<KeyValue<AccountId, AccountInfo>, KeyValue<CategoryId, CategoryInfo>>,
    dependencies: TransactionsProjector.Dependencies,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccountContent(
            info = transfer.from.value,
            localization = dependencies.localization,
        )
        Icon(
            modifier = Modifier.padding(
                horizontal = Dimens.smallSeparation,
            ),
            icon = ArrowIcon[ArrowDirection.StartToEnd],
        )
        AccountContent(
            info = transfer.to.value,
            localization = dependencies.localization,
        )
    }
}