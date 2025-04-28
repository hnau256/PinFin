package hnau.pinfin.model.utils.budget.state

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.plus
import hnau.common.kotlin.mapper.toMapper
import hnau.pinfin.data.UpdateType
import hnau.pinfin.model.utils.budget.upchain.Update
import kotlinx.serialization.json.Json

private val updateUpdateTypeMapper: Mapper<Update, UpdateType> =
    Mapper(Update::value, ::Update) + Json.toMapper(UpdateType.serializer())

val UpdateType.Companion.updateTypeMapper: Mapper<Update, UpdateType>
    get() = updateUpdateTypeMapper