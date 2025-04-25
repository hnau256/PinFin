package hnau.pinfin.projector.bidget.transactions

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
import androidx.compose.ui.util.fastForEach
import arrow.core.Either
import arrow.core.nonEmptySetOf
import arrow.core.toNonEmptyListOrNull
import hnau.common.compose.uikit.table.Table
import hnau.common.compose.uikit.table.TableOrientation
import hnau.common.compose.uikit.table.cellShape
import hnau.common.compose.uikit.utils.Dimens
import hnau.common.compose.utils.Icon
import hnau.common.compose.utils.horizontalDisplayPadding
import hnau.pinfin.repository.TransactionInfo
import hnau.pinfin.repository.signedAmountOrAmount
import hnau.pinfin.projector.utils.AmountContent
import hnau.pinfin.projector.utils.ArrowDirection
import hnau.pinfin.projector.utils.ArrowIcon
import hnau.pinfin.projector.utils.SignedAmountContent
import hnau.pinfin.projector.utils.account.AccountContent
import hnau.pinfin.projector.utils.category.CategoryContent
import hnau.pinfin.projector.utils.color
import hnau.pinfin.repository.dto.CategoryDirection
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TransactionInfo.Content(
    dependencies: TransactionsProjector.Dependencies,
    onClick: () -> Unit,
) {
    Table(
        orientation = TableOrientation.Horizontal,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalDisplayPadding(),
    ) {
        Cell {
            CellContent(
                dependencies = dependencies,
                onClick = onClick,
                cellShape = cellShape,
            )
        }
    }
}

@Composable
fun TransactionInfo.CellContent(
    cellShape: Shape,
    dependencies: TransactionsProjector.Dependencies,
    onClick: () -> Unit,
) {
    val signedAmountOrAmount = signedAmountOrAmount
    Row(
        modifier = Modifier
            .clip(cellShape)
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
            when (val type = type) {
                is TransactionInfo.Type.Entry -> EntryContent(
                    entry = type,
                    dependencies = dependencies,
                )

                is TransactionInfo.Type.Transfer -> TransferContent(
                    transfer = type,
                )
            }
            CommentContent()
        }

        when (signedAmountOrAmount) {
            is Either.Left -> SignedAmountContent(
                amount = signedAmountOrAmount.value,
                amountFormatter = dependencies.amountFormatter,
            )

            is Either.Right -> AmountContent(
                amount = signedAmountOrAmount.value,
                amountFormatter = dependencies.amountFormatter,
            )
        }
    }
}

@Composable
private fun TransactionInfo.TimestampContent(
    dependencies: TransactionsProjector.Dependencies,
) {
    val timestamp = timestamp
    val text = remember(timestamp) {
        val localTimestamp = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
        listOf(
            localTimestamp.date.let(dependencies.dateTimeFormatter::formatDate),
            localTimestamp.time.let(dependencies.dateTimeFormatter::formatTime),
        ).joinToString(
            separator = " ",
        )
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun TransactionInfo.CommentContent() {
    val primary = comment.text.takeIf(String::isNotEmpty)
    val secondary = remember(type) {
        when (val type = type) {
            is TransactionInfo.Type.Transfer -> null
            is TransactionInfo.Type.Entry -> type
                .records
                .mapNotNull {record ->
                    record.comment.text.takeIf(String::isNotEmpty)
                }
                .toNonEmptyListOrNull()
                ?.joinToString(separator = ", ")
        }
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
    )
}

@Composable
private fun EntryContent(
    dependencies: TransactionsProjector.Dependencies,
    entry: TransactionInfo.Type.Entry,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.smallSeparation),
    ) {
        val records = entry.records
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
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.smallSeparation)
        ) {
            categories.fastForEach { categoryInfo ->
                CategoryContent(
                    info = categoryInfo,
                )
            }
        }
        val allDirectionOrNull = remember(categories) {
            val directions = categories
                .tail
                .fold(
                    initial = nonEmptySetOf(categories.head.id.direction)
                ) { acc, category ->
                    acc + category.id.direction
                }
            directions
                .takeIf { it.size == 1 }
                ?.head
        }
        val arrowDirection = when (allDirectionOrNull) {
            CategoryDirection.Credit -> ArrowDirection.StartToEnd
            CategoryDirection.Debit -> ArrowDirection.EndToStart
            null -> ArrowDirection.Both
        }
        Icon(
            tint = allDirectionOrNull
                ?.color
                ?: MaterialTheme.colorScheme.onSurface
        ) { ArrowIcon[arrowDirection] }
        AccountContent(
            info =  entry.account,
        )
    }
}

@Composable
private fun TransferContent(
    transfer: TransactionInfo.Type.Transfer,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccountContent(
            info = transfer.from,
        )
        Icon(
            modifier = Modifier.padding(
                horizontal = Dimens.smallSeparation,
            )
        ) { ArrowIcon[ArrowDirection.StartToEnd] }
        AccountContent(
            info = transfer.to,
        )
    }
}