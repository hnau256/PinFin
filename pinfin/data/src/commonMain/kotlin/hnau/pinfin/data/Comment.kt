package hnau.pinfin.data

import hnau.common.kotlin.mapper.Mapper
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Comment(
    val text: String,
) {

    companion object {

        val stringMapper: Mapper<String, Comment> = Mapper(
            direct = ::Comment,
            reverse = Comment::text,
        )
    }
}