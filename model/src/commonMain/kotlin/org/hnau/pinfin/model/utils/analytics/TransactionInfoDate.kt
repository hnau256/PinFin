package org.hnau.pinfin.model.utils.analytics

import org.hnau.pinfin.model.utils.budget.state.TransactionInfo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val TransactionInfo.date: LocalDate
    get() = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date