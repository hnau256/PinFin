package org.hnau.pinfin.model.utils.analytics

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.hnau.pinfin.model.utils.budget.state.TransactionInfo

val TransactionInfo.date: LocalDate
    get() = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date