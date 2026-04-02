package org.hnau.pinfin.model.utils.budget.state

import kotlinx.serialization.json.Json
import org.hnau.commons.kotlin.mapper.Mapper
import org.hnau.commons.kotlin.mapper.plus
import org.hnau.commons.kotlin.mapper.toMapper
import org.hnau.pinfin.data.UpdateType
import org.hnau.upchain.core.Update

private val updateUpdateTypeMapper: Mapper<Update, UpdateType> =
    Mapper(Update::value, ::Update) + Json.toMapper(UpdateType.serializer())

val UpdateType.Companion.updateTypeMapper: Mapper<Update, UpdateType>
    get() = updateUpdateTypeMapper