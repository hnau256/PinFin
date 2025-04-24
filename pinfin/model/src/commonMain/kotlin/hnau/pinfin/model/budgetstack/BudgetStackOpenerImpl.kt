package hnau.pinfin.model.budgetstack

import hnau.common.app.model.stack.NonEmptyStack
import hnau.common.app.model.stack.push
import hnau.pinfin.data.dto.TransactionType
import hnau.pinfin.data.repository.TransactionInfo
import hnau.pinfin.model.transaction.TransactionModel
import kotlinx.coroutines.flow.MutableStateFlow

class BudgetStackOpenerImpl(
    private val stack: MutableStateFlow<NonEmptyStack<BudgetStackElementModel.Skeleton>>,
) : BudgetStackOpener {

    private fun open(
        skeleton: BudgetStackElementModel.Skeleton,
    ) {
        stack.push(skeleton)
    }

    override fun openNewTransaction(
        transactionType: TransactionType,
    ) {
        open(
            BudgetStackElementModel.Skeleton.Transaction(
                skeleton = TransactionModel.Skeleton.createForNew(
                    transactionType = transactionType,
                )
            )
        )
    }

    override fun openEditTransaction(
        info: TransactionInfo,
    ) {
        open(
            BudgetStackElementModel.Skeleton.Transaction(
                skeleton = TransactionModel.Skeleton.createForEdit(
                    info = info,
                )
            )
        )
    }
}