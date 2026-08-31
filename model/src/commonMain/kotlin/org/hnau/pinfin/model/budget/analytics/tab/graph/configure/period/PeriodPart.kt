package org.hnau.pinfin.model.budget.analytics.tab.graph.configure.period

import org.hnau.commons.gen.enumvalues.annotations.EnumValues
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
@EnumValues(
    serializable = true,
    valuesClassName = "PeriodParts",
)
enum class PeriodPart { Years, Months, Days }