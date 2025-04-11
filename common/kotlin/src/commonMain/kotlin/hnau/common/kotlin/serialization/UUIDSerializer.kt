package hnau.common.kotlin.serialization

import hnau.common.kotlin.mapper.Mapper
import hnau.common.kotlin.mapper.stringToUUID
import kotlinx.serialization.builtins.serializer
import java.util.UUID

object UUIDSerializer: MappingKSerializer<String, UUID>(
    base = String.serializer(),
    mapper = Mapper.stringToUUID,
)