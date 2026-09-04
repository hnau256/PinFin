package org.hnau.pinfin.model.utils.analytics

import kotlinx.datetime.LocalDate
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

val TransactionInfo.date: LocalDate
    get() = timestamp