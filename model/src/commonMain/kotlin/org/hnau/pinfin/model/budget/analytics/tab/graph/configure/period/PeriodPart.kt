package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period

import org.hnau.commons.gen.enumvalues.annotations.EnumValues

@EnumValues(
    serializable = true,
    valuesClassName = "PeriodParts",
)
enum class PeriodPart { Years, Months, Days }