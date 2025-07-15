package hnau.pinfin.data.utils

import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.serializers.FormattedInstantSerializer

data object InstantSerializer: FormattedInstantSerializer(
    name = "PinFinDataInstantSerializer",
    format = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET,
)