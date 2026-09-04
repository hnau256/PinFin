package org.hnau.pinfin.model.utils.analytics.period

import kotlinx.serialization.Serializable
import org.hnau.commons.gen.enumvalues.annotations.EnumValues
import org.hnau.commons.gen.fold.annotations.Fold

@Fold
@EnumValues
@Serializable
enum class PeriodUnit { Day, Month, Year }
